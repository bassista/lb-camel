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
import java.util.List;
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
        MockEndpoint mock = getMockEndpoint("mock:servicenow");
        mock.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        headers.put(ServiceNowConstants.RESOURCE, "table");
        headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE);
        headers.put(ServiceNowConstants.TABLE, "incident");
        headers.put(ServiceNowConstants.SYSPARM_LIMIT, "10");

        template().sendBodyAndHeaders("direct:servicenow", null, headers);

        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        Map<?, ?> items = exchange.getIn().getBody(Map.class);

        assertNotNull(items);
        assertNotNull(items.get("result"));

        List<?> incidents = (List<?>)items.get("result");
        assertTrue(incidents.size() <= 10);
    }
    
    @Test
    public void testRetrieve() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:servicenow");
        mock.expectedMessageCount(1);

        final String sysid = "9c573169c611228700193229fff72400";
        final String number = "INC0000001";

        Map<String, Object> headers = new HashMap<>();
        headers.put(ServiceNowConstants.RESOURCE, "table");
        headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE);
        headers.put(ServiceNowConstants.SYSPARM_ID, sysid);
        headers.put(ServiceNowConstants.TABLE, "incident");

        template().sendBodyAndHeaders("direct:servicenow", null, headers);

        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        Map<?, ?> items = exchange.getIn().getBody(Map.class);

        assertNotNull(items.size());
        assertNotNull(items.get("result"));

        Map<?, ?> incident = (Map<?, ?>)items.get("result");
        assertEquals(sysid, incident.get("sys_id"));
        assertEquals(number, incident.get("number"));
    }

    @Test
    public void testRetrieveWithQuery() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:servicenow");
        mock.expectedMessageCount(1);

        final String sysid = "9c573169c611228700193229fff72400";
        final String number = "INC0000001";

        Map<String, Object> headers = new HashMap<>();
        headers.put(ServiceNowConstants.RESOURCE, "table");
        headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE);
        headers.put(ServiceNowConstants.SYSPARM_QUERY, "number=" + number);
        headers.put(ServiceNowConstants.TABLE, "incident");

        template().sendBodyAndHeaders("direct:servicenow", null, headers);

        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        Map<?, ?> message = exchange.getIn().getBody(Map.class);

        assertNotNull(message);
        assertEquals(1, message.size());
        assertNotNull(message.get("result"));

        List<?> items = (List<?>)message.get("result");
        assertEquals(1, items.size());

        Map<?, ?> result = (Map<?, ?>)items.get(0);
        assertEquals(sysid,  result.get("sys_id"));
        assertEquals(number, result.get("number"));
    }

    @Test
    public void testCreateAndSearch() throws Exception {

        final Map<String, Object> headers = new HashMap<>();
        final Map<String, Object> fields = new HashMap<>();

        Map<?, ?> incident = null;
        MockEndpoint mock = getMockEndpoint("mock:servicenow");

        // ************************
        // Create incident
        // ************************

        {
            mock.expectedMessageCount(1);

            headers.clear();
            headers.put(ServiceNowConstants.RESOURCE, "table");
            headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_CREATE);
            headers.put(ServiceNowConstants.TABLE, "incident");

            fields.clear();
            fields.put("description", "my incident");
            fields.put("severity", 1);
            fields.put("impact", 1);
            fields.put("short_description", "An incident");

            template().sendBodyAndHeaders("direct:servicenow", fields, headers);

            mock.assertIsSatisfied();

            Exchange exchange = mock.getExchanges().get(0);
            Map<?, ?> items = exchange.getIn().getBody(Map.class);

            assertNotNull(items);
            assertEquals(1, items.size());
            assertNotNull(items.get("result"));

            incident = (Map<?, ?>) items.get("result");

            LOGGER.info("*** Incident created ***");
            LOGGER.info("sysid={}, number={}", incident.get("sys_id"), incident.get("number"));
            LOGGER.info("************************");
        }

        mock.reset();

        // ************************
        // Search for the incident
        // ************************

        {
            mock.expectedMessageCount(1);

            headers.clear();
            headers.put(ServiceNowConstants.RESOURCE, "table");
            headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE);
            headers.put(ServiceNowConstants.SYSPARM_QUERY, "number=" + incident.get("number"));
            headers.put(ServiceNowConstants.TABLE, "incident");

            template().sendBodyAndHeaders("direct:servicenow", null, headers);

            mock.assertIsSatisfied();

            Exchange exchange = mock.getExchanges().get(0);
            Map<?, ?> items = exchange.getIn().getBody(Map.class);

            assertNotNull(items);
            assertEquals(1, items.size());
            assertNotNull(items.get("result"));

            assertEquals(1, ((List<?>) items.get("result")).size());

            Map<?, ?> result = (Map<?, ?>) ((List<?>) items.get("result")).get(0);
            assertEquals(incident.get("number"), result.get("number"));
        }

        mock.reset();

        // *******
        // Modify the incident
        // *******

        {
            mock.expectedMessageCount(1);

            headers.clear();
            headers.put(ServiceNowConstants.RESOURCE, "table");
            headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_MODIFY);
            headers.put(ServiceNowConstants.SYSPARM_ID, incident.get("sys_id"));
            headers.put(ServiceNowConstants.TABLE, "incident");

            fields.clear();
            fields.put("severity", 2);
            fields.put("impact", 2);
            fields.put("short_description", "The incident");

            template().sendBodyAndHeaders("direct:servicenow", fields, headers);

            mock.assertIsSatisfied();

            Exchange exchange = mock.getExchanges().get(0);
            Map<?, ?> items = exchange.getIn().getBody(Map.class);

            assertNotNull(items);
            assertEquals(1, items.size());
            assertNotNull(items.get("result"));

            Map<?, ?> result = (Map<?, ?>)items.get("result");
            assertEquals(incident.get("number"), result.get("number"));
            assertEquals("2", result.get("severity"));
            assertEquals("2", result.get("impact"));
            assertEquals("The incident", result.get("short_description"));
        }

        mock.reset();

        // *******
        // Delete it
        // *******

        {
            mock.expectedMessageCount(1);

            headers.clear();
            headers.put(ServiceNowConstants.RESOURCE, "table");
            headers.put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_DELETE);
            headers.put(ServiceNowConstants.SYSPARM_ID, incident.get("sys_id"));
            headers.put(ServiceNowConstants.TABLE, "incident");

            template().sendBodyAndHeaders("direct:servicenow", null, headers);

            mock.assertIsSatisfied();
        }
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

                from("direct:servicenow")
                    .marshal(df)
                    .to("servicenow:{{env:SERVICENOW_INSTANCE}}"
                        + "?userName={{env:SERVICENOW_USERNAME}}"
                        + "&password={{env:SERVICENOW_PASSWORD}}")
                    .choice()
                        .when(body().isNotNull())
                            .unmarshal(df)
                            .to("direct:servicenow-reply")
                        .otherwise()
                            .to("direct:servicenow-reply");

                from("direct:servicenow-reply")
                    .to("log:org.apache.camel.component.servicenow?level=INFO&showAll=true")
                    .to("mock:servicenow");
            }
        };
    }
}
