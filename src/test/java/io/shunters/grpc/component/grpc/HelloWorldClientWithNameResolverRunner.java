package io.shunters.grpc.component.grpc;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Created by mykidong on 2018-01-10.
 */
public class HelloWorldClientWithNameResolverRunner {

    private static Logger log;

    private HelloWorldClientWithNameResolver client;

    @Before
    public void init() throws Exception {
        // load log4j.
        java.net.URL url = new HelloWorldClientWithNameResolverRunner().getClass().getResource("/log4j-test.xml");

        DOMConfigurator.configure(url);

        log = LoggerFactory.getLogger(HelloWorldClientWithNameResolverRunner.class);

        String serviceName = System.getProperty("serviceName");
        String consulHost = System.getProperty("consulHost", "localhost");
        int consulPort = Integer.valueOf(System.getProperty("consulPort", "8500"));

        // init. client.
        client = new HelloWorldClientWithNameResolver(serviceName, consulHost, consulPort);
    }

    @Test
    public void searchKeyword() throws Exception {
        client.sayHello();
    }

}
