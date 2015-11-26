package com.github.lburgazzoli.gradle.plugin.examples.camel;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lburgazzoli
 */
public class MySpringRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MySpringRouteBuilder.class);

    @Override
    public void configure() {
        from("timer://foo?fixedRate=true&period=1000")
            .process(exchange -> LOGGER.info("Exchange is : {}", exchange));
    }
}
