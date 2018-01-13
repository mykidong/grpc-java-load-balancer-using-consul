package io.shunters.grpc.component.grpc;

import org.apache.log4j.xml.DOMConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

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

        String serviceName = System.getProperty("serviceName", "service-name");
        String consulHost = System.getProperty("consulHost", "localhost");
        int consulPort = Integer.valueOf(System.getProperty("consulPort", "8500"));
        boolean ignoreConsul = Boolean.valueOf(System.getProperty("ignoreConsul", "true"));
        String hostPorts = System.getProperty("hostPorts", "localhost:50051");

        List<String> hostPortsList = null;
        if(ignoreConsul)
        {
            String[] tokens = hostPorts.split(",");
            hostPortsList = Arrays.asList(tokens);
        }

        // init. client.
        client = new HelloWorldClientWithNameResolver(serviceName, consulHost, consulPort, ignoreConsul, hostPortsList);
    }

    @Test
    public void searchKeyword() throws Exception {
        client.sayHello();
    }

}
