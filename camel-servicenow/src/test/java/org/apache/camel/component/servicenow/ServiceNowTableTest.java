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

import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;

import org.apache.camel.CamelExecutionException;
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


        template().sendBodyAndHeaders(
            "direct:servicenow",
            null,
            new KVBuilder()
                .put(ServiceNowConstants.RESOURCE, "table")
                .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE)
                .put(ServiceNowConstants.SYSPARM_LIMIT, "10")
                .put(ServiceNowConstants.TABLE, "incident")
                .build()
        );

        mock.assertIsSatisfied();

        Exchange exchange = mock.getExchanges().get(0);
        Map<?, ?> items = exchange.getIn().getBody(Map.class);

        assertNotNull(items);
        assertNotNull(items.get("result"));

        List<?> incidents = (List<?>)items.get("result");
        assertTrue(incidents.size() <= 10);
    }

    @Test
    public void testIncidentWorkflow() throws Exception {

        Map<?, ?> incident;
        MockEndpoint mock = getMockEndpoint("mock:servicenow");

        // ************************
        // Create incident
        // ************************

        {
            mock.reset();
            mock.expectedMessageCount(1);

            template().sendBodyAndHeaders(
                "direct:servicenow",
                new KVBuilder()
                    .put("description", "my incident")
                    .put("severity", 1)
                    .put("impact", 1)
                    .put("short_description", "An incident")
                    .build(),
                new KVBuilder()
                    .put(ServiceNowConstants.RESOURCE, "table")
                    .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_CREATE)
                    .put(ServiceNowConstants.TABLE, "incident")
                    .build()
            );

            mock.assertIsSatisfied();

            incident = extractResult(mock.getExchanges().get(0));

            LOGGER.info("****************************************************");
            LOGGER.info("* Incident created");
            LOGGER.info("*  sysid  = {}", incident.get("sys_id"));
            LOGGER.info("*  number = {}", incident.get("number"));
            LOGGER.info("****************************************************");
        }

        // ************************
        // Search for the incident
        // ************************

        {
            mock.reset();
            mock.expectedMessageCount(1);

            template().sendBodyAndHeaders(
                "direct:servicenow",
                null,
                new KVBuilder()
                    .put(ServiceNowConstants.RESOURCE, "table")
                    .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE)
                    .put(ServiceNowConstants.TABLE, "incident")
                    .put(ServiceNowConstants.SYSPARM_QUERY, "number=" + incident.get("number"))
                    .build()
            );

            mock.assertIsSatisfied();

            Map<?, ?> result = extractResult(mock.getExchanges().get(0));
            assertEquals(incident.get("number"), result.get("number"));
        }

        // ************************
        // Modify the incident
        // ************************

        {
            mock.reset();
            mock.expectedMessageCount(1);

            template().sendBodyAndHeaders(
                "direct:servicenow",
                new KVBuilder()
                    .put("severity", 2)
                    .put("impact", 2)
                    .put("short_description", "The incident")
                    .build(),
                new KVBuilder()
                    .put(ServiceNowConstants.RESOURCE, "table")
                    .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_MODIFY)
                    .put(ServiceNowConstants.TABLE, "incident")
                    .put(ServiceNowConstants.SYSPARM_ID, incident.get("sys_id"))
                    .build()
            );

            mock.assertIsSatisfied();

            Map<?, ?> result = extractResult(mock.getExchanges().get(0));
            assertEquals(incident.get("number"), result.get("number"));
            assertEquals("2", result.get("severity"));
            assertEquals("2", result.get("impact"));
            assertEquals("The incident", result.get("short_description"));
        }

        // ************************
        // Retrieve it via query
        // ************************

        {
            mock.reset();
            mock.expectedMessageCount(1);

            template().sendBodyAndHeaders(
                "direct:servicenow",
                null,
                new KVBuilder()
                    .put(ServiceNowConstants.RESOURCE, "table")
                    .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE)
                    .put(ServiceNowConstants.TABLE, "incident")
                    .put(ServiceNowConstants.SYSPARM_QUERY, "number=" + incident.get("number"))
                    .build()
            );

            mock.assertIsSatisfied();

            Map<?, ?> result = extractResult(mock.getExchanges().get(0));
            assertEquals("2", result.get("severity"));
            assertEquals("2", result.get("impact"));
            assertEquals("The incident", result.get("short_description"));
            assertEquals(incident.get("sys_id"), result.get("sys_id"));
        }

        // ************************
        // Retrieve by sys id
        // ************************

        {
            mock.reset();
            mock.expectedMessageCount(1);

            template().sendBodyAndHeaders(
                "direct:servicenow",
                null,
                new KVBuilder()
                    .put(ServiceNowConstants.RESOURCE, "table")
                    .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE)
                    .put(ServiceNowConstants.TABLE, "incident")
                    .put(ServiceNowConstants.SYSPARM_ID, incident.get("sys_id"))
                    .build()
            );

            mock.assertIsSatisfied();

            Map<?, ?> result = extractResult(mock.getExchanges().get(0));
            assertEquals("2", result.get("severity"));
            assertEquals("2", result.get("impact"));
            assertEquals("The incident", result.get("short_description"));
            assertEquals(incident.get("sys_id"), result.get("sys_id"));
        }

        // ************************
        // Delete it
        // ************************

        {
            mock.reset();
            mock.expectedMessageCount(1);

            template().sendBodyAndHeaders(
                "direct:servicenow",
                null,
                new KVBuilder()
                    .put(ServiceNowConstants.RESOURCE, "table")
                    .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_DELETE)
                    .put(ServiceNowConstants.TABLE, "incident")
                    .put(ServiceNowConstants.SYSPARM_ID, incident.get("sys_id"))
                    .build()
            );

            mock.assertIsSatisfied();
        }

        // ************************
        // Retrieve it via query, should fail
        // ************************

        {
            try {
                template().sendBodyAndHeaders(
                    "direct:servicenow",
                    null,
                    new KVBuilder()
                        .put(ServiceNowConstants.RESOURCE, "table")
                        .put(ServiceNowConstants.ACTION, ServiceNowConstants.ACTION_RETRIEVE)
                        .put(ServiceNowConstants.SYSPARM_QUERY, "number=" + incident.get("number"))
                        .put(ServiceNowConstants.TABLE, "incident")
                        .build()
                );

                fail("Record +" + incident.get("number") + " should have been deleted");
            } catch (CamelExecutionException e) {
                assertTrue(e.getCause() instanceof NotFoundException);
                // we are good
            }
        }
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected Map<?, ?> extractResult(Exchange exchange) {
        Map<?, ?> items = exchange.getIn().getBody(Map.class);

        assertNotNull(items);
        assertEquals(1, items.size());
        assertNotNull(items.get("result"));

        Object data = items.get("result");
        if (data instanceof List) {
            return (Map<?, ?>)((List<?>)items.get("result")).get(0);
        }

        if (data instanceof Map) {
            return (Map<?, ?>)items.get("result");
        }

        throw new IllegalStateException("Unknown result type :" + data.getClass());
    }

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
