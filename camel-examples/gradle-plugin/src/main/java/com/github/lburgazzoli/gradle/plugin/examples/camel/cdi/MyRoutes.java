package com.github.lburgazzoli.gradle.plugin.examples.camel.cdi;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.cdi.Uri;

import javax.inject.Inject;

/**
 * Configures all our Camel routes, components, endpoints and beans
 */
@ContextName
public class MyRoutes extends RouteBuilder {

    @Inject
    @Uri("timer:foo?period=5000")
    private Endpoint inputEndpoint;

    @Inject
    @Uri("log:output")
    private Endpoint resultEndpoint;

    @Override
    public void configure() throws Exception {
        // you can configure the route rule with Java DSL here

        from(inputEndpoint)
                .to("bean:counterBean")
                .to(resultEndpoint);
    }

}