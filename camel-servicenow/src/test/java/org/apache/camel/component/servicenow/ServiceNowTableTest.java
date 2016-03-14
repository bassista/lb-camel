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
package org.apache.camel.component.servicenow;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.junit.Test;

public class ServiceNowTableTest extends ServiceNowTestSupport {

    @Test
    public void testRetrieveAll() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:retrieve");
        mock.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ServiceNowConstants.RESOURCE, "table");
        headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE);
        headers.put(ServiceNowConstants.TABLE, "incident");
        headers.put(ServiceNowConstants.SYSPARM_LIMIT, "2");

        template().sendBodyAndHeaders("direct:retrieve", null, headers);

        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        Map<?, ?> message = exchange.getIn().getBody(Map.class);

        assertNotNull(message.size());
        assertNotNull(message.get("result"));
    }
    
    @Test
    public void testRetrieve() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:retrieve");
        mock.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ServiceNowConstants.RESOURCE, "table");
        headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE);
        headers.put(ServiceNowConstants.SYSPARM_ID, "9c573169c611228700193229fff72400");
        headers.put(ServiceNowConstants.TABLE, "incident");
        headers.put(ServiceNowConstants.SYSPARM_LIMIT, "2");

        template().sendBodyAndHeaders("direct:retrieve", null, headers);

        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        Map<?, ?> message = exchange.getIn().getBody(Map.class);

        assertNotNull(message.size());
        assertNotNull(message.get("result"));

        Map<?, ?> result = (Map<?, ?>)message.get("result");
        assertEquals("9c573169c611228700193229fff72400", result.get("sys_id"));
        assertEquals("INC0000001", result.get("number"));

    }

    // *************************************************************************
    //
    // *************************************************************************

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                JsonDataFormat df = new JsonDataFormat(JsonLibrary.Jackson);
                df.setUnmarshalType(Map.class);

                from("direct:retrieve")
                    .to("servicenow:https://dev21005.service-now.com/api/now"
                        + "?userName={{env:SERVICENOW_USERNAME}}"
                        + "&password={{env:SERVICENOW_PASSWORD}}")
                    .unmarshal(df)
                    //.to("log:org.apache.camel.component.servicenow?level=INFO&showAll=true")
                    .to("mock:retrieve");
            }
        };
    }
}
