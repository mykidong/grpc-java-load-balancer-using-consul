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
public class HelloWorldServerRunner {

    private static Logger log;

    @Before
    public void init() throws Exception {
        // load log4j.
        java.net.URL url = new HelloWorldServerRunner().getClass().getResource("/log4j-test.xml");

        DOMConfigurator.configure(url);

        log = LoggerFactory.getLogger(HelloWorldServerRunner.class);
    }

    @Test
    public void runServer() throws Exception {
        final HelloWorldServer server = new HelloWorldServer();
        server.start();
        server.blockUntilShutdown();
    }

}
