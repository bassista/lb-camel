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

import java.util.HashMap;
import java.util.Map;

import com.orbitz.consul.Consul;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;


public abstract class AbstractConsulProducer extends DefaultProducer {
    private final AbstractConsulEndpoint endpoint;
    private final ConsulConfiguration configuration;
    private Map<String, MessageProcessor> processors;

    protected AbstractConsulProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint);

        this.endpoint = endpoint;
        this.configuration = configuration;
        this.processors = new HashMap<>();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final Message in = exchange.getIn();
        final String action = getMandatoryAction(in);

        processors.getOrDefault(action, this::onError).process(in);
    }

    // *************************************************************************
    //
    // *************************************************************************


    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (processors.isEmpty()) {
            bindActionProcessors(processors);
        }
    }

    protected void bindActionProcessors(Map<String, MessageProcessor> processors) {
    }

    protected void onError(Message message) throws Exception {
        throw new IllegalStateException(
            "No processor registered for action " + message.getHeader(ConsulConstants.CONSUL_ACTION)
        );
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected Consul getConsul() throws Exception {
        return endpoint.getConsul();
    }

    protected ConsulConfiguration getConfiguration() {
        return getConfiguration();
    }

    protected String getAction(Message message) throws Exception {
        return message.getHeader(
            ConsulConstants.CONSUL_ACTION,
            configuration.getAction(),
            String.class);
    }

    protected String getMandatoryAction(Message message) throws Exception {
        return getMandatoryHeader(
            message,
            ConsulConstants.CONSUL_ACTION,
            configuration.getAction(),
            String.class);
    }

    protected String getKey(Message message) throws Exception {
        return message.getHeader(
            ConsulConstants.CONSUL_KEY,
            configuration.getKey(),
            String.class);
    }

    protected String getMandatoryKey(Message message) throws Exception {
        return getMandatoryHeader(
            message,
            ConsulConstants.CONSUL_KEY,
            configuration.getKey(),
            String.class);
    }

    protected <T> T getMandatoryHeader(Message message, String header, Class<T> type) throws Exception {
        return ObjectHelper.notNull(
            message.getHeader(header, type),
            header
        );
    }

    protected <T> T getMandatoryHeader(Message message, String header, T defaultValue, Class<T> type) throws Exception {
        return ObjectHelper.notNull(
            message.getHeader(header, defaultValue, type),
            header);
    }

    protected <T> T getOption(Message message, T defaultValue, Class<T> type) throws Exception {
        return message.getHeader(ConsulConstants.CONSUL_OPTIONS, defaultValue, type);
    }

    protected boolean isValueAsString(Message message) throws Exception {
        return message.getHeader(
            ConsulConstants.CONSUL_VALUE_AS_STRING,
            configuration.isValueAsString(),
            Boolean.class);
    }

    protected <T> T getBody(Message message, T defaultValue, Class<T> type) throws Exception {
        T body = message.getBody(type);
        if (body == null) {
            body = defaultValue;
        }

        return  body;
    }

    protected void setBodyAndResult(Message message, Object body) throws Exception {
        setBodyAndResult(message, body, body != null);
    }

    protected void setBodyAndResult(Message message, Object body, boolean result) throws Exception {
        message.setHeader(ConsulConstants.CONSUL_RESULT, result);
        message.setBody(body);
    }
}
