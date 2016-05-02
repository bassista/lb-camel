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
package org.apache.camel.component.consul;

import com.orbitz.consul.Consul;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

public abstract class AbstractConsulEndpoint extends DefaultEndpoint {

    @UriPath(description = "The consul configuration")
    @Metadata(required = "true")
    private final ConsulConfiguration configuration;

    @UriParam(description = "The API endpoint")
    @Metadata(required = "true")
    private final String apiEndpoint;

    private Consul consul;

    protected AbstractConsulEndpoint(String apiEndpoint, String uri, ConsulComponent component, ConsulConfiguration configuration) {
        super(uri, component);

        this.configuration = configuration;
        this.apiEndpoint = apiEndpoint;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    // *************************************************************************
    //
    // *************************************************************************

    public ConsulConfiguration getConfiguration() {
        return this.configuration;
    }

    public String getApiEndpoint() {
        return this.apiEndpoint;
    }

    public synchronized Consul getConsul() throws Exception {
        if (consul == null) {
            Consul.Builder builder = Consul.builder();

            if (configuration.getUrl() != null) {
                builder.withUrl(configuration.getUrl());
            }

            if (configuration.getSslContextParameters() != null) {
                builder.withSslContext(configuration.getSslContextParameters().createSSLContext());
            }

            if (configuration.getObjectMapper() != null) {
                builder.withObjectMapper(configuration.getObjectMapper());
            }

            consul = builder.build();
        }

        return consul;
    }
}
