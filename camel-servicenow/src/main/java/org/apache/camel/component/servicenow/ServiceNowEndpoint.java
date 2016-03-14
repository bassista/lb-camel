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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 * Represents a ServiceNow endpoint.
 */
@UriEndpoint(scheme = "servicenow", title = "ServiceNow", syntax="servicenow:basePath", consumerClass = ServiceNowConsumer.class, label = "ServiceNow")
public class ServiceNowEndpoint extends DefaultEndpoint {

    @UriPath(description = "The ServiceNow REST gateway base path")
    @Metadata(required = "true")
    private final String basePath;

    private final ServiceNowConfiguration configuration;
    private final Map<Class<?>, Object> clients;

    public ServiceNowEndpoint(String uri, ServiceNowComponent component, ServiceNowConfiguration configuration, String basePath) throws Exception{
        super(uri, component);

        this.configuration = configuration;
        this.basePath = component.getCamelContext().resolvePropertyPlaceholders(basePath);
        this.clients = new HashMap<>();
    }

    @Override
    public Producer createProducer() throws Exception {
        return new ServiceNowProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public ServiceNowComponent getComponent() {
        return (ServiceNowComponent)super.getComponent();
    }

    public ServiceNowConfiguration getConfiguration() {
        return configuration;
    }

    public String getBasePath() {
        return basePath;
    }

    synchronized <T> T getClient(Class<T> type) throws Exception {
        T client = type.cast(clients.get(type));
        if (client == null) {
            client = JAXRSClientFactory.create(this.basePath, type);
            ClientConfiguration config = WebClient.getConfig(client);

            if (configuration.hasBasicAuthentication()) {
                AuthorizationPolicy authorization = new AuthorizationPolicy();
                authorization.setUserName(
                    getCamelContext().resolvePropertyPlaceholders(configuration.getUserName()));
                authorization.setPassword(
                    getCamelContext().resolvePropertyPlaceholders(configuration.getPassword()));

                config.getHttpConduit().setAuthorization(authorization);
            }

            clients.put(type, client);
        }

        return client;
    }
}
