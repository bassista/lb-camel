/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.lburgazzoli.camel.samples.spring.boot;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CamelSpringApplicationMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelSpringApplicationMain.class);

    @Bean
    public RouteBuilder builder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:hello?period={{timer.period}}")
                    .to("log:com.github.lburgazzoli.camel.samples.spring.boot?level=INFO");
            }
        };
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx =
            new SpringApplicationBuilder(CamelSpringApplicationMain.class)
                .listeners(CamelSpringApplicationMain::onEvent)
                .run(args);

        // If Camel's ApplicationControlled is not configured, run it manually
        //ctx.getBean(CamelSpringBootApplicationController.class).run();
    }

    public static void onEvent(ApplicationEvent event) {
        LOGGER.info("ApplicationEvent: {}", event);
    }
}
