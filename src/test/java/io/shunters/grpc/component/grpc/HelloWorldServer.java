package io.shunters.grpc.component.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.shunters.grpc.component.proto.helloworld.GreeterGrpc;
import io.shunters.grpc.component.proto.helloworld.HelloReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by mykidong on 2018-01-10.
 */
public class HelloWorldServer {
    private static Logger log = LoggerFactory.getLogger(HelloWorldServer.class);

    private Server server;

    public void start() throws IOException {
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new HelloWorldService())
                .build()
                .start();
        log.info("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                HelloWorldServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws Exception {
        // start server.
        final HelloWorldServer server = new HelloWorldServer();
        server.start();
        server.blockUntilShutdown();
    }

    private static class HelloWorldService extends GreeterGrpc.GreeterImplBase {
        @Override
        public void sayHello(io.shunters.grpc.component.proto.helloworld.HelloRequest request,
                             io.grpc.stub.StreamObserver<io.shunters.grpc.component.proto.helloworld.HelloReply> responseObserver) {
            String name = request.getName();

            HelloReply response = HelloReply.newBuilder().setMessage("hello " + name).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}
