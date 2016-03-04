package org.apache.camel.component.servicenow;

import org.apache.camel.util.component.AbstractApiProducer;

import org.apache.camel.component.servicenow.internal.ServiceNowApiName;
import org.apache.camel.component.servicenow.internal.ServiceNowPropertiesHelper;

/**
 * The ServiceNow producer.
 */
public class ServiceNowProducer extends AbstractApiProducer<ServiceNowApiName, ServiceNowConfiguration> {

    public ServiceNowProducer(ServiceNowEndpoint endpoint) {
        super(endpoint, ServiceNowPropertiesHelper.getHelper());
    }
}
