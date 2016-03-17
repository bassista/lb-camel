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

import java.lang.ref.WeakReference;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.servicenow.model.ServiceNowAggregateProcessor;
import org.apache.camel.component.servicenow.model.ServiceNowImportSetProcessor;
import org.apache.camel.component.servicenow.model.ServiceNowTableProcessor;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;

/**
 * The ServiceNow producer.
 */
public class ServiceNowProducer extends DefaultProducer {

    private final ServiceNowEndpoint endpoint;
    private final ServiceNowConfiguration configuration;
    private final ThreadLocal<WeakReference<ServiceNowProcessor>> tableCache;
    private final ThreadLocal<WeakReference<ServiceNowProcessor>> aggregateCache;
    private final ThreadLocal<WeakReference<ServiceNowProcessor>> importSetCache;

    public ServiceNowProducer(ServiceNowEndpoint endpoint) {
        super(endpoint);

        this.endpoint = endpoint;
        this.configuration = endpoint.getConfiguration();
        this.tableCache = new ThreadLocal<>();
        this.aggregateCache = new ThreadLocal<>();
        this.importSetCache = new ThreadLocal<>();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final String resource = exchange.getIn().getHeader(ServiceNowConstants.RESOURCE, String.class);
        final Message in = exchange.getIn();

        ServiceNowProcessor processor;
        if (ObjectHelper.equal(ServiceNowConstants.RESOURCE_TABLE, resource, true)) {
            processor = getProcessor(tableCache, ServiceNowTableProcessor.SUPPLIER);
        } else if (ObjectHelper.equal(ServiceNowConstants.RESOURCE_AGGREGATE, resource, true)) {
            processor = getProcessor(aggregateCache, ServiceNowAggregateProcessor.SUPPLIER);
        } else if (ObjectHelper.equal(ServiceNowConstants.RESOURCE_IMPORT, resource, true)) {
            processor = getProcessor(importSetCache, ServiceNowImportSetProcessor.SUPPLIER);
        } else {
            throw new IllegalArgumentException("Unknown resource type: " + resource);
        }

        processor.process(
            exchange,
            in.getHeader(ServiceNowConstants.TABLE, configuration.getTable(), String.class),
            in.getHeader(ServiceNowConstants.SYSPARM_ID, String.class),
            in.getHeader(ServiceNowConstants.ACTION, String.class)
        );
    }

    // *************************************************************************
    // Thread-Local processor instances as CXF client proxies are not thread
    // safe. To be refactored once moved to Java 8
    // *************************************************************************

    private ServiceNowProcessor getProcessor(
        ThreadLocal<WeakReference<ServiceNowProcessor>> cache,
        ServiceNowProcessorSupplier supplier) throws Exception {

        ServiceNowProcessor processor = null;
        WeakReference<ServiceNowProcessor> ref = cache.get();
        if (ref != null) {
            processor = ref.get();
        }

        if (processor == null) {
            processor = supplier.get(endpoint);
            cache.set(new WeakReference<>(processor));
        }

        return processor;
    }
}
