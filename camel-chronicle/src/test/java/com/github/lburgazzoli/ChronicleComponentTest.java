package com.github.lburgazzoli;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ChronicleComponentTest extends CamelTestSupport {

    @Test
    public void testChronicle() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("chronicle://foo")
                  .to("chronicle://bar")
                  .to("mock:result");
            }
        };
    }
}
