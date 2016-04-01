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
package org.apache.camel.component.consul;

import com.orbitz.consul.KeyValueClient;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ConsulKeyValueWatchTest extends ConsulTestSupport {
    private String key;
    private KeyValueClient client;

    @Override
    public void doPreSetup() {
        key = generateKey();
        client = getConsul().keyValueClient();
    }

    @Test
    public void testWatchKey() throws Exception {
        String val1 = generateRandomString();
        String val2 = generateRandomString();

        MockEndpoint mock = getMockEndpoint("mock:kv-watch");
        mock.expectedMinimumMessageCount(2);
        mock.expectedBodiesReceived(val1, val2);
        mock.expectedHeaderReceived(ConsulConstants.CONSUL_RESULT, true);

        client.putValue(key, val1);
        client.putValue(key, val2);

        mock.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                fromF("consul:kv?key=%s&valueAsString=true", key)
                    .to("log:org.apache.camel.component.consul?level=INFO&showAll=true")
                    .to("mock:kv-watch");
            }
        };
    }
}
