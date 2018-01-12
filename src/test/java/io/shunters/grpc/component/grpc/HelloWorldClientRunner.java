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
public class HelloWorldClientRunner {

    private static Logger log;

    private HelloWorldClient client;

    @Before
    public void init() throws Exception {
        // load log4j.
        java.net.URL url = new HelloWorldClientRunner().getClass().getResource("/log4j-test.xml");

        DOMConfigurator.configure(url);

        log = LoggerFactory.getLogger(HelloWorldClientRunner.class);

        // init. client.
        client = new HelloWorldClient(Arrays.asList("localhost:50051"));
    }

    @Test
    public void searchKeyword() throws Exception {
        client.sayHello();
    }

}
