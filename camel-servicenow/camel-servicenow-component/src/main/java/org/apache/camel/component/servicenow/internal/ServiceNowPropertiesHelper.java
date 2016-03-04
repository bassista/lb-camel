package org.apache.camel.component.servicenow.internal;

import org.apache.camel.util.component.ApiMethodPropertiesHelper;

import org.apache.camel.component.servicenow.ServiceNowConfiguration;

/**
 * Singleton {@link ApiMethodPropertiesHelper} for ServiceNow component.
 */
public final class ServiceNowPropertiesHelper extends ApiMethodPropertiesHelper<ServiceNowConfiguration> {

    private static ServiceNowPropertiesHelper helper;

    private ServiceNowPropertiesHelper() {
        super(ServiceNowConfiguration.class, ServiceNowConstants.PROPERTY_PREFIX);
    }

    public static synchronized ServiceNowPropertiesHelper getHelper() {
        if (helper == null) {
            helper = new ServiceNowPropertiesHelper();
        }
        return helper;
    }
}
