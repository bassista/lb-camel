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
package org.apache.camel.component.servicenow.model;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.servicenow.ServiceNowConfiguration;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.component.servicenow.ServiceNowEndpoint;
import org.apache.camel.component.servicenow.ServiceNowHelper;
import org.apache.camel.component.servicenow.ServiceNowProcessor;
import org.apache.camel.component.servicenow.ServiceNowProcessorSupplier;
import org.apache.camel.util.ObjectHelper;

public class ServiceNowTableProcessor implements ServiceNowProcessor {

    public static final ServiceNowProcessorSupplier SUPPLIER = new ServiceNowProcessorSupplier() {
        @Override
        public ServiceNowProcessor get(ServiceNowEndpoint endpoint) throws Exception {
            return new ServiceNowTableProcessor(endpoint);
        }
    };

    private final ServiceNowEndpoint endpoint;
    private final ServiceNowConfiguration config;
    private final ServiceNowTable client;

    public ServiceNowTableProcessor(ServiceNowEndpoint endpoint) throws Exception {
        this.endpoint = endpoint;
        this.config = endpoint.getConfiguration();
        this.client = endpoint.createClient(ServiceNowTable.class);
    }

    @Override
    public void process(
        Exchange exchange,
        String tableName,
        String sysId,
        String action) throws Exception {

        final Message in = exchange.getIn();
        final Class<?> model = in.getHeader(ServiceNowConstants.MODEL, config.getModel(tableName, Map.class), Class.class);
        final ObjectMapper mapper = config.getMapper();

        ObjectHelper.notNull(tableName, "tableName");
        ObjectHelper.notNull(mapper, "objectMapper");

        if (ObjectHelper.equal(ServiceNowConstants.ACTION_RETRIEVE, action, true)) {
            retrieveRecord(config, client, in, model, mapper, tableName, sysId);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_CREATE, action, true)) {
            createRecord(config, client, in, model, mapper, tableName);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_MODIFY, action, true)) {
            modifyRecord(config, client, in, model, mapper, tableName, sysId);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_DELETE, action, true)) {
            deleteRecord(config, client, in, model, mapper, tableName, sysId);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_UPDATE, action, true)) {
            updateRecord(config, client, in, model, mapper, tableName, sysId);
        } else {
            throw new IllegalArgumentException("Unknown action " + action);
        }
    }

    /*
     * GET https://instance.service-now.com/api/now/table/{tableName}
     * GET https://instance.service-now.com/api/now/table/{tableName}/{sys_id}
     */
    private void retrieveRecord(
        ServiceNowConfiguration config,
        ServiceNowTable client,
        Message in,
        Class<?> model,
        ObjectMapper mapper,
        String tableName,
        String sysId) throws Exception {

        JsonNode node;
        if (sysId == null) {
            node = client.retrieveRecord(
                tableName,
                in.getHeader(ServiceNowConstants.SYSPARM_QUERY, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_LIMIT, Integer.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class)
            );
        } else {
            ObjectHelper.notNull(sysId, "sysId");

            node = client.retrieveRecordById(
                tableName,
                sysId,
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class)
            );
        }

        in.setBody(ServiceNowHelper.extractResult(mapper, model, node));
    }

    /*
     * POST https://instance.service-now.com/api/now/table/{tableName}
     */
    private void createRecord(
        ServiceNowConfiguration config,
        ServiceNowTable client,
        Message in,
        Class<?> model,
        ObjectMapper mapper,
        String tableName) throws Exception {

        ServiceNowHelper.validateBody(in, model);

        Object result = ServiceNowHelper.extractResult(
            mapper,
            model,
            client.createRecord(
                tableName,
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_INPUT_DISPLAY_VALUE, config.getInputDisplayValue(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_SUPPRESS_AUTO_SYS_FIELD, config.getSuppressAutoSysField(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class),
                mapper.writeValueAsString(in.getBody())
            )
        );

        in.setBody(result);
    }

    /*
     * PUT https://instance.service-now.com/api/now/table/{tableName}/{sys_id}
     */
    private void modifyRecord(
        ServiceNowConfiguration config,
        ServiceNowTable client,
        Message in,
        Class<?> model,
        ObjectMapper mapper,
        String tableName,
        String sysId) throws Exception {

        ObjectHelper.notNull(sysId, "sysId");

        ServiceNowHelper.validateBody(in, model);

        Object result = ServiceNowHelper.extractResult(
            mapper,
            model,
            client.modifyRecord(
                tableName,
                sysId,
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_INPUT_DISPLAY_VALUE, config.getInputDisplayValue(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_SUPPRESS_AUTO_SYS_FIELD, config.getSuppressAutoSysField(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class),
                mapper.writeValueAsString(in.getBody())
            )
        );

        in.setBody(result);
    }

    /*
     * DELETE https://instance.service-now.com/api/now/table/{tableName}/{sys_id}
     */
    private void deleteRecord(
        ServiceNowConfiguration config,
        ServiceNowTable client,
        Message in,
        Class<?> model,
        ObjectMapper mapper,
        String tableName,
        String sysId) throws Exception {

        ObjectHelper.notNull(sysId, "sysId");

        Object result = ServiceNowHelper.extractResult(
            mapper,
            model,
            client.deleteRecord(
                tableName,
                sysId)
        );

        in.setBody(result);
    }

    /*
     * PATCH instance://dev21005.service-now.com/api/now/table/{tableName}/{sys_id}
     */
    private void updateRecord(
        ServiceNowConfiguration config,
        ServiceNowTable client,
        Message in,
        Class<?> model,
        ObjectMapper mapper,
        String tableName,
        String sysId) throws Exception {

        ObjectHelper.notNull(sysId, "sysId");

        ServiceNowHelper.validateBody(in, model);

        Object result = ServiceNowHelper.extractResult(
            mapper,
            model,
            client.updateRecord(
                tableName,
                sysId,
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_INPUT_DISPLAY_VALUE, config.getInputDisplayValue(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_SUPPRESS_AUTO_SYS_FIELD, config.getSuppressAutoSysField(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class),
                mapper.writeValueAsString(in.getBody())
            )
        );

        in.setBody(result);
    }
}
