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

import com.google.common.base.Optional;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.consul.enpoint.ConsulKeyValueActions;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ConsulKVWatchTest extends ConsulTestSupport {
    private String key;

    @Before
    public void doPreSetup() {
        key = generateKey();
    }

    @Test
    public void testConsul() throws Exception {
        String val1 = generateRandomString();
        String val2 = generateRandomString();

        MockEndpoint mock = getMockEndpoint("mock:kv");
        mock.expectedMinimumMessageCount(2);
        mock.expectedBodiesReceived(val1, val2);
        mock.expectedHeaderReceived(ConsulConstants.CONSUL_RESULT, true);

        MockEndpoint mockw = getMockEndpoint("mock:kv-watch");
        mockw.expectedMinimumMessageCount(2);
        mockw.expectedBodiesReceived(val1, val2);
        mockw.expectedHeaderReceived(ConsulConstants.CONSUL_RESULT, true);

        template().sendBodyAndHeaders(
            "direct:kv",
            val1,
            new KVBuilder()
                .put(ConsulConstants.CONSUL_ACTION, ConsulKeyValueActions.PUT)
                .put(ConsulConstants.CONSUL_KEY, key)
                .build()
        );
        template().sendBodyAndHeaders(
            "direct:kv",
            val2,
            new KVBuilder()
                .put(ConsulConstants.CONSUL_ACTION, ConsulKeyValueActions.PUT)
                .put(ConsulConstants.CONSUL_KEY, key)
                .build()
        );

        mock.assertIsSatisfied();
        mockw.assertIsSatisfied();

        Optional<String> keyVal = getConsul().keyValueClient().getValueAsString(key);

        assertTrue(keyVal.isPresent());
        assertEquals(val2, keyVal.get());
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct:kv")
                    .to("consul:kv")
                        .to("mock:kv");
                from("consul:kv?key=" + key)
                    .to("mock:kv-watch");
            }
        };
    }
}
