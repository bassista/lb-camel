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

public class ServiceNowAggregateHelper extends ServiceNowHelper {

    public static void process(
        ServiceNowConfiguration config, ServiceNowAggregate aggregate, Exchange exchange, String tableName, String sysId, String action) throws Exception {

        if (ObjectHelper.equal(ServiceNowConstants.ACTION_RETRIEVE, action, true)) {
            retrieveStats(config, aggregate, exchange.getIn(), tableName);
        } else {
            throw new IllegalArgumentException("Unknown action " + action);
        }
    }

    public static void retrieveStats(
        ServiceNowConfiguration config, ServiceNowAggregate aggregate, Message in, String tableName) throws Exception {

        final Class<?> model = in.getHeader(ServiceNowConstants.MODEL, config.getModel(tableName, Map.class), Class.class);
        final ObjectMapper mapper = config.getMapper();

        ObjectHelper.notNull(tableName, "tableName");
        ObjectHelper.notNull(mapper, "objectMapper");

        Object result = extractResult(
            mapper,
            model,
            aggregate.retrieveStats(
                tableName,
                in.getHeader(ServiceNowConstants.SYSPARM_QUERY, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_AVG_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_COUNT, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_MIN_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_MAX_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_SUM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_GROUP_BY, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_ORDER_BY, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_HAVING, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class)
            )
        );

        in.setBody(result);
    }
}
