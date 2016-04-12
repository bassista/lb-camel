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
package org.apache.camel.component.consul.policy;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.consul.ConsulTestSupport;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ConsulRoutePolicyTest1 extends ConsulTestSupport {

    @Test
    public void testRoutePolicy() throws Exception {
        while(true) {
            Thread.sleep(1000);
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                ConsulRoutePolicy policy = new ConsulRoutePolicy();
                policy.setServiceName("camel-consul-service");
                policy.setTtl(18);

                from("timer://consul-timer?fixedRate=true&period=1000")
                    .routeId("SERVICE-1")
                    .routePolicy(policy)
                    .setHeader("ConsulServiceID", constant("SERVICE-1"))
                    .to("log:org.apache.camel.component.consul?level=INFO&showAll=true");
            }
        };
    }
}
