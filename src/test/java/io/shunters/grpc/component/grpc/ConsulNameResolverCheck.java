package io.shunters.grpc.component.grpc;

import org.junit.Test;

public class ConsulNameResolverCheck {

    @Test
    public void checkConsulNameResolver()
    {
        HelloWorldClientWithNameResolver client = new HelloWorldClientWithNameResolver("emb-collection-handler", "localhost", 8500);
    }
}
