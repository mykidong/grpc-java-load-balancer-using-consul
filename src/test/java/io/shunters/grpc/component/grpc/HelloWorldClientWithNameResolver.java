package io.shunters.grpc.component.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.util.RoundRobinLoadBalancerFactory;
import io.shunters.grpc.component.proto.helloworld.GreeterGrpc;
import io.shunters.grpc.component.proto.helloworld.HelloReply;
import io.shunters.grpc.component.proto.helloworld.HelloRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mykidong on 2018-01-10.
 */
public class HelloWorldClientWithNameResolver {
    private static Logger log = LoggerFactory.getLogger(HelloWorldClientWithNameResolver.class);

    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    /**
     * using consul service discovery.
     *
     * @param serviceName
     * @param consulHost
     * @param consulPort
     */
    public HelloWorldClientWithNameResolver(String serviceName, String consulHost, int consulPort) {

        String consulAddr = "consul://" + consulHost + ":" + consulPort;

        channel = ManagedChannelBuilder
                .forTarget(consulAddr)
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .nameResolverFactory(new ConsulNameResolver.ConsulNameResolverProvider(serviceName, 5))
                .usePlaintext(true)
                .build();

        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }


    public void sayHello() {
        try {
            HelloRequest request = HelloRequest.newBuilder()
                    .setName("grpc load balancer")
                    .build();

            HelloReply response = blockingStub.sayHello(request);
            String message = response.getMessage();
            log.info("message: [{}]", message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
