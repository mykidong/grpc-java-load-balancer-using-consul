package io.shunters.grpc.component.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created by mykidong on 2018-01-11.
 */
public class GrpcClient<R, B, A> {

    private final ManagedChannel channel;
    private B blockingStub;
    private A asyncStub;

    private String host;
    private int port;
    private Class<R> rpcClass;

    public GrpcClient(String host, int port, Class<R> rpcClass) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build(), rpcClass);

        this.host = host;
        this.port = port;
        this.rpcClass = rpcClass;
    }

    private GrpcClient(ManagedChannel channel, Class<R> rpcClass) {
        this.channel = channel;

        try {
            Method blockingStubMethod = rpcClass.getMethod("newBlockingStub", Channel.class);
            blockingStub = (B) blockingStubMethod.invoke(null, channel);

            Method asyncStubMethod = rpcClass.getMethod("newStub", Channel.class);
            asyncStub = (A) asyncStubMethod.invoke(null, channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public B getBlockingStub() {
        return this.blockingStub;
    }

    public A getAsyncStub() {
        return this.asyncStub;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Class<R> getRpcClass() {
        return rpcClass;
    }
}
