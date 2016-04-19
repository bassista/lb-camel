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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CamelSpringApplicationMain {

    @Bean
    public RouteBuilder builder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("timer:hello?period={{timer.period}}")
                    .to("log:com.github.lburgazzoli.camel.samples.infinispan.boot?level=INFO");
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(CamelSpringApplicationMain.class, args);
    }
}
