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
package org.apache.camel.component.ehcache;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.ehcache.Cache;
import org.junit.Test;

public class EhcacheTest extends EhcacheTestSupport {

    @Test
    public void testCacheClear() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived((Object)null);
        mock.expectedHeaderReceived(EhcacheConstants.ACTION_HAS_RESULT, false);
        mock.expectedHeaderReceived(EhcacheConstants.ACTION_SUCCEEDED, true);

        fluentTemplate()
            .withHeader(EhcacheConstants.ACTION, EhcacheConstants.ACTION_CLEAR)
            .to("direct://start")
            .send();
        
        assertMockEndpointsSatisfied();
    }

    @Test
    public void testCachePut() throws Exception {
        final String key = generateRandomString();
        final String val = generateRandomString();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived(val);
        mock.expectedHeaderReceived(EhcacheConstants.ACTION_HAS_RESULT, false);
        mock.expectedHeaderReceived(EhcacheConstants.ACTION_SUCCEEDED, true);

        fluentTemplate()
            .withHeader(EhcacheConstants.ACTION, EhcacheConstants.ACTION_PUT)
            .withHeader(EhcacheConstants.KEY, key)
            .withBody(val)
            .to("direct://start")
            .send();

        assertMockEndpointsSatisfied();

        Cache<Object, Object> cache = getCache("mycache");
        assertTrue(cache.containsKey(key));
        assertEquals(val, cache.get(key));
    }

    @Test
    public void testCacheGet() throws Exception {
        final String key = generateRandomString();
        final String val = generateRandomString();

        Cache<Object, Object> cache = getCache("mycache");
        cache.put(key, val);

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);
        mock.expectedBodiesReceived(val);
        mock.expectedHeaderReceived(EhcacheConstants.ACTION_HAS_RESULT, true);
        mock.expectedHeaderReceived(EhcacheConstants.ACTION_SUCCEEDED, true);

        fluentTemplate()
            .withHeader(EhcacheConstants.ACTION, EhcacheConstants.ACTION_GET)
            .withHeader(EhcacheConstants.KEY, key)
            .withBody(val)
            .to("direct://start")
            .send();

        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("direct://start")
                  .to("ehcache://mycache?cacheManager=#cacheManager")
                    .to("log:org.apache.camel.component.ehcache?level=INFO&showAll=true&multiline=true")
                    .to("mock:result");
            }
        };
    }
}
