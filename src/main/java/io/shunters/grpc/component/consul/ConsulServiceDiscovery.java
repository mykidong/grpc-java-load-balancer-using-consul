package io.shunters.grpc.component.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.session.SessionClient;
import com.ecwid.consul.v1.session.SessionConsulClient;
import com.ecwid.consul.v1.session.model.NewSession;
import io.shunters.grpc.api.component.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConsulServiceDiscovery implements ServiceDiscovery {

    private static Logger log = LoggerFactory.getLogger(ConsulServiceDiscovery.class);

    private ConsulClient client;

    private SessionClient sessionClient;

    private static final Object lock = new Object();

    private static ServiceDiscovery serviceDiscovery;

    public static ServiceDiscovery singleton(String agentHost, int agentPort) {
        if (serviceDiscovery == null) {
            synchronized (lock) {
                if (serviceDiscovery == null) {
                    serviceDiscovery = new ConsulServiceDiscovery(agentHost, agentPort);
                }
            }
        }
        return serviceDiscovery;
    }


    private ConsulServiceDiscovery(String agentHost, int agentPort) {
        client = new ConsulClient(agentHost, agentPort);
        sessionClient = new SessionConsulClient(agentHost, agentPort);

        log.info("consul client info: " + client.toString());
    }

    /**
     * @param serviceName
     * @param id
     * @param tags
     * @param address
     * @param port
     * @param script
     * @param tcp         "localhost:9911"
     * @param interval    "10s"
     * @param timeout     "1s"
     */
    @Override
    public void createService(String serviceName, String id, List<String> tags, String address, int port, String script, String tcp, String interval, String timeout) {
        // register new service with associated health check
        NewService newService = new NewService();
        newService.setName(serviceName);
        newService.setId(id);
        newService.setAddress(address);
        newService.setPort(port);
        if (tags != null) newService.setTags(tags);

        NewService.Check serviceCheck = new NewService.Check();
        if (script != null) serviceCheck.setScript(script);
        if (tcp != null) serviceCheck.setTcp(tcp);
        serviceCheck.setInterval(interval);
        if (timeout != null) serviceCheck.setTimeout(timeout);
        newService.setCheck(serviceCheck);

        client.agentServiceRegister(newService);
    }


    @Override
    public List<ServiceNode> getHealthServices(String serviceName) {

        List<ServiceNode> list = new ArrayList<>();

        Response<List<HealthService>> healthServiceResponse = client.getHealthServices(serviceName, true, QueryParams.DEFAULT);

        List<HealthService> healthServices = healthServiceResponse.getValue();
        if (healthServices == null) {
            return null;
        }

        for (HealthService healthService : healthServices) {
            HealthService.Service service = healthService.getService();

            String id = service.getId();

            String address = healthService.getNode().getAddress();

            int port = service.getPort();

            list.add(new ServiceNode(id, address, port));
        }

        return list;
    }

    @Override
    public Map<String, String> getKVValues(String keyPath) {

        Response<List<GetValue>> valueResponse = client.getKVValues(keyPath);

        List<GetValue> getValues = valueResponse.getValue();

        if (getValues == null) {
            return null;
        }

        Map<String, String> map = new HashMap<>();

        for (GetValue v : getValues) {
            if (v == null || v.getValue() == null) {
                continue;
            }

            String key = v.getKey();

            String value = v.getDecodedValue();

            map.put(key, value);
        }

        return map;
    }

    @Override
    public String getKVValue(String key) {
        Response<GetValue> valueResponse = client.getKVValue(key);

        GetValue getValue = valueResponse.getValue();
        if (getValue == null) {
            return null;
        }

        return getValue.getDecodedValue();
    }

    @Override
    public Set<String> getKVKeysOnly(String keyPath) {
        Response<List<String>> valueResponse = client.getKVKeysOnly(keyPath);

        List<String> getValues = valueResponse.getValue();

        if (getValues == null) {
            return null;
        }

        Set<String> set = new HashSet<>();

        for (String key : getValues) {
            if (key != null) {
                set.add(key);
            }
        }

        return set;
    }

    @Override
    public void deleteKVValue(String key) {
        client.deleteKVValue(key);
    }

    @Override
    public void deleteKVValuesRecursively(String key) {
        client.deleteKVValues(key);
    }

    @Override
    public Map<String, String> getLeader(String keyPath) {
        Response<List<GetValue>> valueResponse = client.getKVValues(keyPath);

        List<GetValue> getValues = valueResponse.getValue();

        if (getValues == null) {
            return null;
        }

        Map<String, String> map = null;

        int count = 0;
        for (GetValue v : getValues) {
            if (v == null || v.getValue() == null || v.getSession() == null) {
                continue;
            }

            if (count == 0) {
                map = new HashMap<>();
            }

            String key = v.getKey();

            String value = v.getDecodedValue();

            map.put(key, value);

            count++;
        }

        return map;
    }


    @Override
    public void setKVValue(String key, String value) {

        client.setKVValue(key, value);
    }

    @Override
    public String createSession(String name, String node, String ttl, long lockDelay) {
        NewSession newSession = new NewSession();
        newSession.setName(name);
        newSession.setNode(node);
        newSession.setTtl(ttl);
        newSession.setLockDelay(lockDelay);

        Response<String> response = this.sessionClient.sessionCreate(newSession, QueryParams.DEFAULT);

        return response.getValue();
    }

    @Override
    public void destroySession(String session) {
        this.sessionClient.sessionDestroy(session, QueryParams.DEFAULT);
    }

    @Override
    public void renewSession(String session) {
        this.sessionClient.renewSession(session, QueryParams.DEFAULT);
    }

    @Override
    public boolean acquireLock(String key, String value, String session) {

        PutParams putParams = new PutParams();
        putParams.setAcquireSession(session);

        Response<Boolean> response = client.setKVValue(key, value, putParams);

        return response.getValue();
    }
}
