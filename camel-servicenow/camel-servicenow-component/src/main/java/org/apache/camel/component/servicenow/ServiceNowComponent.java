package org.apache.camel.component.servicenow;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.util.component.AbstractApiComponent;

import org.apache.camel.component.servicenow.internal.ServiceNowApiCollection;
import org.apache.camel.component.servicenow.internal.ServiceNowApiName;

/**
 * Represents the component that manages {@link ServiceNowEndpoint}.
 */
public class ServiceNowComponent extends AbstractApiComponent<ServiceNowApiName, ServiceNowConfiguration, ServiceNowApiCollection> {

    public ServiceNowComponent() {
        super(ServiceNowEndpoint.class, ServiceNowApiName.class, ServiceNowApiCollection.getCollection());
    }

    public ServiceNowComponent(CamelContext context) {
        super(context, ServiceNowEndpoint.class, ServiceNowApiName.class, ServiceNowApiCollection.getCollection());
    }

    @Override
    protected ServiceNowApiName getApiName(String apiNameStr) throws IllegalArgumentException {
        return ServiceNowApiName.fromValue(apiNameStr);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String methodName, ServiceNowApiName apiName,
                                      ServiceNowConfiguration endpointConfiguration) {
        ServiceNowEndpoint endpoint = new ServiceNowEndpoint(uri, this, apiName, methodName, endpointConfiguration);
        endpoint.setName(methodName);
        return endpoint;
    }

    /**
     * To use the shared configuration
     */
    @Override
    public void setConfiguration(ServiceNowConfiguration configuration) {
        super.setConfiguration(configuration);
    }

}
