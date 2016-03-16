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

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.servicenow.ServiceNowConfiguration;
import org.apache.camel.component.servicenow.ServiceNowConstants;
import org.apache.camel.util.ObjectHelper;

public class ServiceNowTableHelper  {

    public static void process(
        ServiceNowConfiguration config, ServiceNowTable table, Exchange exchange, String tableName, String sysId, String action) throws Exception {

        ObjectHelper.notNull(tableName, "tableName");

        if (ObjectHelper.equal(ServiceNowConstants.ACTION_RETRIEVE, action, true)) {
            retrieveRecord(config, table, exchange.getIn(), tableName, sysId);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_CREATE, action, true)) {
            createRecord(config, table, exchange.getIn(), tableName);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_MODIFY, action, true)) {
            modifyRecord(config, table, exchange.getIn(), tableName, sysId);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_DELETE, action, true)) {
            deleteRecord(config, table, exchange.getIn(), tableName, sysId);
        } else if (ObjectHelper.equal(ServiceNowConstants.ACTION_UPDATE, action, true)) {
            updateRecord(config, table, exchange.getIn(), tableName, sysId);
        } else {
            throw new IllegalArgumentException("Unknown action " + action);
        }
    }

    public static void retrieveRecord(
        ServiceNowConfiguration config, ServiceNowTable table, Message in, String tableName, String sysId) throws Exception {

        ObjectHelper.notNull(tableName, "tableName");

        if (sysId == null) {
            in.setBody(
                table.retrieveRecord(
                    tableName,
                    in.getHeader(ServiceNowConstants.SYSPARM_QUERY, String.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_LIMIT, Integer.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class)
                )
            );
        } else {
            in.setBody(
                table.retrieveRecordById(
                    tableName,
                    sysId,
                    in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                    in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class)
                )
            );
        }
    }

    public static void createRecord(
        ServiceNowConfiguration config, ServiceNowTable table, Message in, String tableName) throws Exception {

        ObjectHelper.notNull(tableName, "tableName");

        in.setBody(
            table.createRecord(
                tableName,
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_INPUT_DISPLAY_VALUE, config.getInputDisplayValue(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_SUPPRESS_AUTO_SYS_FIELD, config.getSuppressAutoSysField(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class),
                in.getBody(String.class)
            )
        );
    }

    public static void modifyRecord(
        ServiceNowConfiguration config, ServiceNowTable table, Message in, String tableName, String sysId) throws Exception {

        ObjectHelper.notNull(tableName, "tableName");
        ObjectHelper.notNull(sysId, "sysId");

        in.setBody(
            table.modifyRecord(
                tableName,
                sysId,
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_INPUT_DISPLAY_VALUE, config.getInputDisplayValue(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_SUPPRESS_AUTO_SYS_FIELD, config.getSuppressAutoSysField(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class),
                in.getBody(String.class)
            )
        );
    }

    public static void deleteRecord(
        ServiceNowConfiguration config, ServiceNowTable table, Message in, String tableName, String sysId) throws Exception {

        ObjectHelper.notNull(tableName, "tableName");
        ObjectHelper.notNull(sysId, "sysId");

        in.setBody(
            table.deleteRecord(
                tableName,
                sysId)
        );
    }

    public static void updateRecord(
        ServiceNowConfiguration config, ServiceNowTable table, Message in, String tableName, String sysId) throws Exception {

        ObjectHelper.notNull(tableName, "tableName");
        ObjectHelper.notNull(sysId, "sysId");

        in.setBody(
            table.updateRecord(
                tableName,
                sysId,
                in.getHeader(ServiceNowConstants.SYSPARM_DISPLAY_VALUE, config.getDisplayValue(), String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_EXCLUDE_REFERENCE_LINK, config.getExcludeReferenceLink(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_FIELDS, String.class),
                in.getHeader(ServiceNowConstants.SYSPARM_INPUT_DISPLAY_VALUE, config.getInputDisplayValue(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_SUPPRESS_AUTO_SYS_FIELD, config.getSuppressAutoSysField(), Boolean.class),
                in.getHeader(ServiceNowConstants.SYSPARM_VIEW, String.class),
                in.getBody(String.class)
            )
        );
    }
}
