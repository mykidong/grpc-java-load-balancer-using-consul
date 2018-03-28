package io.shunters.grpc.component.grpc;

import io.shunters.grpc.api.component.ServiceDiscovery;
import io.shunters.grpc.component.consul.ConsulServiceDiscovery;
import io.shunters.grpc.util.RoundRobin;
import io.shunters.grpc.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mykidong on 2018-01-11.
 */
public class GrpcLoadBalancer<R, B, A> {

    private static Logger log = LoggerFactory.getLogger(GrpcLoadBalancer.class);

    private RoundRobin<GrpcClient<R, B, A>> roundRobin;
    private List<RoundRobin.Robin<GrpcClient<R, B, A>>> robinList;

    private static final int DEFAULT_PAUSE_IN_SECONDS = 5;

    ReentrantLock lock = new ReentrantLock();

    private String serviceName;
    private String consulHost;
    private int consulPort;
    private boolean ignoreConsul;
    private Class<R> rpcClass;
    private List<String> hostPorts;
    private int pauseInSeconds;

    private ConnectionCheckTimer<R, B, A> connectionCheckTimer;

    /**
     * using consul service discovery.
     *
     * @param serviceName
     * @param consulHost
     * @param consulPort
     * @param rpcClass
     */
    public GrpcLoadBalancer(String serviceName, String consulHost, int consulPort, Class<R> rpcClass) {
        this(serviceName, consulHost, consulPort, false, rpcClass, DEFAULT_PAUSE_IN_SECONDS, null);
    }

    public GrpcLoadBalancer(String serviceName, String consulHost, int consulPort, Class<R> rpcClass, int pauseInSeconds) {
        this(serviceName, consulHost, consulPort, false, rpcClass, pauseInSeconds, null);
    }

    /**
     * using static node list.
     *
     * @param hostPorts
     * @param rpcClass
     */
    public GrpcLoadBalancer(List<String> hostPorts, Class<R> rpcClass) {
        this(null, null, -1, true, rpcClass, DEFAULT_PAUSE_IN_SECONDS, hostPorts);
    }

    public GrpcLoadBalancer(List<String> hostPorts, Class<R> rpcClass, int pauseInSeconds) {
        this(null, null, -1, true, rpcClass, pauseInSeconds, hostPorts);
    }


    public GrpcLoadBalancer(String serviceName, String consulHost, int consulPort, boolean ignoreConsul, Class<R> rpcClass, List<String> hostPorts) {
        this(serviceName, consulHost, consulPort, ignoreConsul, rpcClass, DEFAULT_PAUSE_IN_SECONDS, hostPorts);
    }

    public GrpcLoadBalancer(String serviceName, String consulHost, int consulPort, boolean ignoreConsul, Class<R> rpcClass, int pauseInSeconds, List<String> hostPorts) {
        this.serviceName = serviceName;
        this.consulHost = consulHost;
        this.consulPort = consulPort;
        this.ignoreConsul = ignoreConsul;
        this.rpcClass = rpcClass;
        this.hostPorts = hostPorts;
        this.pauseInSeconds = pauseInSeconds;

        loadServiceNodes();

        // run connection check timer.
        this.connectionCheckTimer = new ConnectionCheckTimer(this, this.pauseInSeconds);
        this.connectionCheckTimer.runTimer();
    }

    public void loadServiceNodes() {
        lock.lock();
        try {
            robinList = new ArrayList<>();
            if (!ignoreConsul) {

                List<ServiceDiscovery.ServiceNode> nodes = null;
                while (true) {
                    nodes = getServiceNodes(serviceName, consulHost, consulPort);

                    if (nodes == null || nodes.size() == 0) {
                        log.info("there is no node info for serviceName: [{}]...", serviceName);

                        TimeUtils.sleep(pauseInSeconds * 1000);
                    } else {
                        break;
                    }
                }

                for (ServiceDiscovery.ServiceNode node : nodes) {
                    String host = node.getHost();
                    int port = node.getPort();

                    log.info("serviceName: [" + serviceName + "], host: [" + host + "], port: [" + port + "]");

                    GrpcClient<R, B, A> client = new GrpcClient<>(host, port, rpcClass);

                    robinList.add(new RoundRobin.Robin(client));
                }
            } else {
                for (String hostPort : hostPorts) {
                    String[] tokens = hostPort.split(":");

                    GrpcClient<R, B, A> client = new GrpcClient<>(tokens[0], Integer.valueOf(tokens[1]), rpcClass);
                    robinList.add(new RoundRobin.Robin(client));
                }
            }

            roundRobin = new RoundRobin(robinList);
        } finally {
            lock.unlock();
        }
    }

    private List<ServiceDiscovery.ServiceNode> getServiceNodes(String serviceName, String consulHost, int consulPort) {
        ServiceDiscovery serviceDiscovery = ConsulServiceDiscovery.singleton(consulHost, consulPort);

        return serviceDiscovery.getHealthServices(serviceName);
    }

    public B getBlockingStub() {
        lock.lock();
        try {
            return this.roundRobin.next().getBlockingStub();
        } finally {
            lock.unlock();
        }
    }

    public A getAsyncStub() {
        lock.lock();
        try {
            return this.roundRobin.next().getAsyncStub();
        } finally {
            lock.unlock();
        }
    }

    public List<RoundRobin.Robin<GrpcClient<R, B, A>>> getRobinList() {
        return this.robinList;
    }

    public void shutdown() {
        for (RoundRobin.Robin<GrpcClient<R, B, A>> robin : robinList) {
            try {
                robin.call().shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private static class ConnectionCheckTimer<R, B, A> {
        private ConnectionCheckTimerTask<R, B, A> timerTask;
        private int delay = 1000;
        private int pauseInSeconds;
        private Timer timer;
        private GrpcLoadBalancer<R, B, A> lb;

        public ConnectionCheckTimer(GrpcLoadBalancer lb, int pauseInSeconds) {
            this.lb = lb;
            this.pauseInSeconds = pauseInSeconds;

            this.timerTask = new ConnectionCheckTimerTask(this.lb);
            this.timer = new Timer();
        }

        public void runTimer() {
            this.timer.scheduleAtFixedRate(this.timerTask, delay, this.pauseInSeconds * 1000);
        }

        public void reset() {
            this.timerTask.cancel();
            this.timer.purge();
            this.timerTask = new ConnectionCheckTimerTask(this.lb);
        }
    }

    private static class ConnectionCheckTimerTask<R, B, A> extends TimerTask {
        private GrpcLoadBalancer<R, B, A> lb;

        public ConnectionCheckTimerTask(GrpcLoadBalancer lb) {
            this.lb = lb;
        }

        @Override
        public void run() {

            for (RoundRobin.Robin<GrpcClient<R, B, A>> robin : lb.getRobinList()) {
                String host = robin.call().getHost();
                int port = robin.call().getPort();
                try {
                    Socket socketClient = new Socket(host, port);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    log.info("service nodes being reloaded...");

                    this.lb.loadServiceNodes();
                    break;
                }
            }
        }
    }
}
