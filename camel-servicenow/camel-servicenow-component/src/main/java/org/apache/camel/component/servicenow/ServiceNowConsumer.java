package org.apache.camel.component.servicenow;

import org.apache.camel.Processor;
import org.apache.camel.util.component.AbstractApiConsumer;

import org.apache.camel.component.servicenow.internal.ServiceNowApiName;

/**
 * The ServiceNow consumer.
 */
public class ServiceNowConsumer extends AbstractApiConsumer<ServiceNowApiName, ServiceNowConfiguration> {

    public ServiceNowConsumer(ServiceNowEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

}
