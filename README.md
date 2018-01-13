# gRPC Java Client Side Load Balancer using Consul

This is gRPC Java client-side load balancer, in particular for consul service discovery.
It can also be used for the static service node list.

# How to use

First, generate java classes using protoc:

    # run protoc command for linux:
    mvn protobuf:test-compile -DprotocExecutable="/usr/local/bin/protoc"
    mvn protobuf:test-compile-custom -DprotocExecutable="/usr/local/bin/protoc"
            
    # run protoc command for windows:
    mvn protobuf:test-compile -DprotocExecutable="F:/dev/protoc/protoc-3.5.1-win32/bin/protoc.exe"
    mvn protobuf:test-compile-custom -DprotocExecutable="F:/dev/protoc/protoc-3.5.1-win32/bin/protoc.exe"

The class of load balance GrpcLoadBalancer looks like this:

    GrpcLoadBalancer<R, B, A>
    
the generated classes are used to construct GrpcLoadBalancer instance.
First generic type of GrpcLoadBalancer is rpc class, the second is blocking stub class, and the third is async stub class.

In the test directory, the generated classes of hello world proto and test client can be seen.
Let's see GrpcLoadBalancer with the specific generated type classes in the HelloWorldClient class.

    private GrpcLoadBalancer<GreeterGrpc, GreeterGrpc.GreeterBlockingStub, GreeterGrpc.GreeterStub> lb;
    
To construct load balancer using consul service discovery, GrpcLoadBalancer can be constructed like this:
 
    // GrpcLoadBalancer(String serviceName, String consulHost, int consulPort, Class<R> rpcClass)
    lb = new GrpcLoadBalancer<>(serviceName, consulHost, consulPort, GreeterGrpc.class);
    
Or just using static node list:

    // Arrays.asList("host1:port1", "host2:port2")
    // GrpcLoadBalancer(List<String> hostPorts, Class<R> rpcClass)
    lb = new GrpcLoadBalancer<>(hostPorts, GreeterGrpc.class);
 

To get blocking stub to invoke rpc:

    lb.getBlockingStub()
    
Or to get async stub:

    lb.getAsyncStub()
   
Please, see HelloWorldClient class for the details of how to invoke rpc.


# Run Demo
This demo is used with static service node list.

Run hello world server:

    mvn -e -Dtest=HelloWorldServerRunner test;
    
In another console, run hello world client:

    mvn -e -Dtest=HelloWorldClientRunner test;
