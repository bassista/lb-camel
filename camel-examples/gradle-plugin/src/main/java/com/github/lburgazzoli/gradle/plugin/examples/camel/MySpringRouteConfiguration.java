package com.github.lburgazzoli.gradle.plugin.examples.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MySpringRouteConfiguration extends SingleRouteCamelConfiguration {
    @Bean
    public RouteBuilder route() {
        return new MySpringRouteBuilder();
    }
}