package io.shunters.grpc.component.grpc;

import io.shunters.grpc.component.proto.helloworld.GreeterGrpc;
import io.shunters.grpc.component.proto.helloworld.HelloReply;
import io.shunters.grpc.component.proto.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by mykidong on 2018-01-10.
 */
public class HelloWorldClient {
    private static Logger log = LoggerFactory.getLogger(HelloWorldClient.class);

    private GrpcLoadBalancer<GreeterGrpc, GreeterGrpc.GreeterBlockingStub, GreeterGrpc.GreeterStub> lb;

    /**
     * using consul service discovery.
     *
     * @param serviceName
     * @param consulHost
     * @param consulPort
     */
    public HelloWorldClient(String serviceName, String consulHost, int consulPort) {
        lb = new GrpcLoadBalancer<>(serviceName, consulHost, consulPort, GreeterGrpc.class);
    }

    /**
     * using static node list.
     *
     * @param hostPorts, for instance, Arrays.asList("host1:port1", "host2:port2")
     */
    public HelloWorldClient(List<String> hostPorts) {
        lb = new GrpcLoadBalancer<>(hostPorts, GreeterGrpc.class);
    }


    public void sayHello() {
        try {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName("grpc load balancer")
                    .build();

            HelloReply response = lb.getBlockingStub().sayHello(request);
            String message = response.getMessage();
            log.info("message: [{}]", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
