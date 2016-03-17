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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.servicenow.ServiceNowConfiguration;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.util.ObjectHelper;

public class ServiceNowImportSetHelper extends ServiceNowHelper {

    public static void process(
        ServiceNowConfiguration config, ServiceNowImportSet importSet, Exchange exchange, String tableName, String sysId, String action) throws Exception {

        if (ObjectHelper.equal(ServiceNowConstants.ACTION_RETRIEVE, action, true)) {
            retrieveRecord(config, importSet, exchange.getIn(), tableName, sysId);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_CREATE, action, true)) {
            createRecord(config, importSet, exchange.getIn(), tableName);
        } else {
            throw new IllegalArgumentException("Unknown action " + action);
        }
    }

    public static void retrieveRecord(
        ServiceNowConfiguration config, ServiceNowImportSet importSet, Message in, String tableName, String sysId) throws Exception {

        final Class<?> model = in.getHeader(ServiceNowConstants.MODEL, config.getModel(tableName, Map.class), Class.class);
        final ObjectMapper mapper = config.getMapper();

        ObjectHelper.notNull(tableName, "tableName");
        ObjectHelper.notNull(sysId, "sysId");
        ObjectHelper.notNull(mapper, "objectMapper");

        Object result = extractResult(
            mapper,
            model,
            importSet.retrieveRecordById(
                tableName,
                sysId)
        );

        in.setBody(result);
    }

    public static void createRecord(
        ServiceNowConfiguration config, ServiceNowImportSet importSet, Message in, String tableName) throws Exception {

        final Class<?> model = in.getHeader(ServiceNowConstants.MODEL, config.getModel(tableName, Map.class), Class.class);
        final ObjectMapper mapper = config.getMapper();

        ObjectHelper.notNull(tableName, "tableName");
        ObjectHelper.notNull(mapper, "objectMapper");

        Object result = extractResult(
            mapper,
            model,
            importSet.createRecord(
                tableName,
                in.getBody(String.class)
            )
        );

        in.setBody(result);
    }
}
