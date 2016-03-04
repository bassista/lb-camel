package org.apache.camel.component.servicenow;

import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.component.AbstractApiEndpoint;
import org.apache.camel.util.component.ApiMethod;
import org.apache.camel.util.component.ApiMethodPropertiesHelper;

import org.apache.camel.component.servicenow.api.ServiceNowFileHello;
import org.apache.camel.component.servicenow.api.ServiceNow;
import org.apache.camel.component.servicenow.internal.ServiceNowApiCollection;
import org.apache.camel.component.servicenow.internal.ServiceNowApiName;
import org.apache.camel.component.servicenow.internal.ServiceNowConstants;
import org.apache.camel.component.servicenow.internal.ServiceNowPropertiesHelper;

/**
 * Represents a ServiceNow endpoint.
 */
@UriEndpoint(scheme = "servicenow", title = "ServiceNow", syntax="servicenow:name", consumerClass = ServiceNowConsumer.class, label = "ServiceNow")
public class ServiceNowEndpoint extends AbstractApiEndpoint<ServiceNowApiName, ServiceNowConfiguration> {

    @UriPath @Metadata(required = "true")
    private String name;

    // TODO create and manage API proxy
    private Object apiProxy;

    public ServiceNowEndpoint(String uri, ServiceNowComponent component,
                         ServiceNowApiName apiName, String methodName, ServiceNowConfiguration endpointConfiguration) {
        super(uri, component, apiName, methodName, ServiceNowApiCollection.getCollection().getHelper(apiName), endpointConfiguration);

    }

    public Producer createProducer() throws Exception {
        return new ServiceNowProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        // make sure inBody is not set for consumers
        if (inBody != null) {
            throw new IllegalArgumentException("Option inBody is not supported for consumer endpoint");
        }
        final ServiceNowConsumer consumer = new ServiceNowConsumer(this, processor);
        // also set consumer.* properties
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    protected ApiMethodPropertiesHelper<ServiceNowConfiguration> getPropertiesHelper() {
        return ServiceNowPropertiesHelper.getHelper();
    }

    protected String getThreadProfileName() {
        return ServiceNowConstants.THREAD_PROFILE_NAME;
    }

    @Override
    protected void afterConfigureProperties() {
        // TODO create API proxy, set connection properties, etc.
        switch (apiName) {
            case HELLO_FILE:
                apiProxy = new ServiceNowFileHello();
                break;
            case HELLO_JAVADOC:
                apiProxy = new ServiceNow();
                break;
            default:
                throw new IllegalArgumentException("Invalid API name " + apiName);
        }
    }

    @Override
    public Object getApiProxy(ApiMethod method, Map<String, Object> args) {
        return apiProxy;
    }

    /**
     * Some description of this option, and what it does
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}
