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

import org.apache.camel.Exchange;
import org.apache.camel.component.servicenow.model.ServiceNowTable;
import org.apache.camel.component.servicenow.model.ServiceNowTableHelper;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;

/**
 * The ServiceNow producer.
 */
public class ServiceNowProducer extends DefaultProducer {

    private final  ServiceNowEndpoint endpoint;

    public ServiceNowProducer(ServiceNowEndpoint endpoint) {
        super(endpoint);

        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String resource = exchange.getIn().getHeader(ServiceNowConstants.RESOURCE, String.class);

        if (ObjectHelper.equal(ServiceNowConstants.RESOURCE_TABLE, resource, true)) {
            ServiceNowTableHelper.process(
                endpoint.getClient(ServiceNowTable.class),
                exchange,
                exchange.getIn().getHeader(ServiceNowConstants.TABLE, String.class),
                exchange.getIn().getHeader(ServiceNowConstants.SYSPARM_ID, String.class),
                exchange.getIn().getHeader(ServiceNowConstants.ACTION, String.class)
            );
        } else if (ObjectHelper.equal(ServiceNowConstants.RESOURCE_AGGREGATE, resource, true)) {
        } else if (ObjectHelper.equal(ServiceNowConstants.RESOURCE_IMPORT, resource, true)) {
        } else {
            throw new IllegalArgumentException("Unknown resource type: " + resource);
        }
    }
}
