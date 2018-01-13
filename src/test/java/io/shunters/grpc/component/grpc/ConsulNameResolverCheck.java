package io.shunters.grpc.component.grpc;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;

public class ConsulNameResolverCheck {

    @Before
    public void init() throws Exception {
        // load log4j.
        java.net.URL url = new ConsulNameResolverCheck().getClass().getResource("/log4j-test.xml");
        DOMConfigurator.configure(url);
    }

    @Test
    public void checkConsulNameResolver() throws Exception {
        HelloWorldClientWithNameResolver client = new HelloWorldClientWithNameResolver("emb-collection-handler", "localhost", 8500);

        Thread.sleep(Long.MAX_VALUE);
    }
}
