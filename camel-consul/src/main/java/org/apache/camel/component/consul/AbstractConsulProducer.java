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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.consul.enpoint.ConsulActionProcessor;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;


public abstract class AbstractConsulProducer extends DefaultProducer {
    protected final AbstractConsulEndpoint endpoint;
    protected final ConsulConfiguration configuration;
    protected final Map<String, ConsulMessageProcessor> processors;

    protected AbstractConsulProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint);

        this.endpoint = endpoint;
        this.configuration = configuration;
        this.processors = new HashMap<>();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        ConsulMessageProcessor processor = processors.getOrDefault(
            ObjectHelper.notNull(
                getAction(exchange.getIn()),
                ConsulConstants.CONSUL_ACTION),
            this::onError
        );

        processor.process(exchange.getIn());
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected void onError(Message message) throws Exception {
        throw new IllegalStateException(
            "No processor registered for action " + message.getHeader(ConsulConstants.CONSUL_ACTION)
        );
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected String getAction(Message message) throws Exception {
        return message.getHeader(
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
        return ObjectHelper.notNull(
            message.getHeader(
                ConsulConstants.CONSUL_KEY,
                configuration.getKey(),
                String.class),
            ConsulConstants.CONSUL_KEY
        );
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

    protected <T> T getMandatoryHeader(Message message, String header, Class<T> type) throws Exception {
        return ObjectHelper.notNull(
            message.getHeader(header, type),
            header
        );
    }

    protected void setBodyAndResult(Message message, Object body) throws Exception {
        setBodyAndResult(message, body, body != null);
    }

    protected void setBodyAndResult(Message message, Object body, boolean result) throws Exception {
        message.setHeader(ConsulConstants.CONSUL_RESULT, result);
        message.setBody(body);
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected static void forEachMethodAnnotation(
        Object target, BiConsumer<ConsulActionProcessor, Method> consumer) {

        for (Method method : target.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ConsulActionProcessor.class)) {
                consumer.accept(method.getAnnotation(ConsulActionProcessor.class), method);
            }
        }
    }
}
