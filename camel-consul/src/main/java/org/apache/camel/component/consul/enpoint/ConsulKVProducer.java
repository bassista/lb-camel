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
package org.apache.camel.component.consul.enpoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.option.PutOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.consul.AbstractConsulEndpoint;
import org.apache.camel.component.consul.AbstractConsulProducer;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.component.consul.ConsulConstants;
import org.apache.camel.component.consul.ConsulMessageProcessor;
import org.apache.camel.component.consul.ConsulUtils;
import org.apache.camel.util.ObjectHelper;

public class ConsulKVProducer extends AbstractConsulProducer {
    private KeyValueClient client;
    private Map<ConsulKVAction, ConsulMessageProcessor> processors;

    ConsulKVProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint, configuration);

        // instantiated on start
        this.client = null;
        this.processors = new HashMap<>();

        ConsulUtils.forEachMethodAnnotation(
            this.getClass(),
            ConsulKVActionProcessor.class,
            (final ConsulKVActionProcessor annotation, final Method method) ->
                processors.put(annotation.value(), message -> method.invoke(this, message))
        );
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final ConsulKVAction action = getAction(exchange.getIn());

        ConsulMessageProcessor processor = processors.getOrDefault(action, this::onError);
        processor.process(exchange.getIn());
    }

    @Override
    protected void doStart() throws Exception {
        client = endpoint.getConsul().keyValueClient();
    }

    @Override
    protected void doStop() throws Exception {
        client = null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    private void onError(Message message) throws Exception {
        throw new IllegalStateException(
            "No processor registered for action " + getAction(message)
        );
    }

    @ConsulKVActionProcessor(ConsulKVAction.PUT)
    private void put(Message message) throws Exception {
        message.setHeader(
            ConsulConstants.CONSUL_RESULT,
            client.putValue(
                getKey(message),
                message.getBody(String.class),
                message.getHeader(ConsulConstants.CONSUL_FLAGS, 0L, Long.class),
                message.getHeader(ConsulConstants.CONSUL_OPTIONS, PutOptions.BLANK, PutOptions.class)
            )
        );
    }

    @ConsulKVActionProcessor(ConsulKVAction.GET_VALUE)
    private void getValue(Message message) throws Exception {
        Object result;

        if (valueAsString(message)) {
            result = client.getValueAsString(
                getKey(message)
            ).orNull();
        } else {
            result = client.getValue(
                getKey(message),
                message.getHeader(ConsulConstants.CONSUL_OPTIONS, QueryOptions.BLANK, QueryOptions.class)
            ).orNull();
        }

        message.setHeader(ConsulConstants.CONSUL_RESULT, result != null);
        message.setBody(result);
    }


    @ConsulKVActionProcessor(ConsulKVAction.GET_VALUES)
    private void getValues(Message message) throws Exception {
        Object result;

        if (valueAsString(message)) {
            result = client.getValuesAsString(
                getKey(message)
            );
        } else {
            result = client.getValues(
                getKey(message),
                message.getHeader(ConsulConstants.CONSUL_OPTIONS, QueryOptions.BLANK, QueryOptions.class)
            );
        }

        message.setHeader(ConsulConstants.CONSUL_RESULT, result != null);
        message.setBody(result);
    }

    @ConsulKVActionProcessor(ConsulKVAction.GET_KEYS)
    private void getKeys(Message message) throws Exception {
        message.setBody(client.getKeys(getKey(message)));
        message.setHeader(ConsulConstants.CONSUL_RESULT, message.getBody() != null);
    }

    @ConsulKVActionProcessor(ConsulKVAction.GET_SESSIONS)
    private void getSessions(Message message) throws Exception {
        message.setBody(client.getSession(getKey(message)));
        message.setHeader(ConsulConstants.CONSUL_RESULT, message.getBody() != null);
    }

    @ConsulKVActionProcessor(ConsulKVAction.DELETE_KEY)
    private void deleteKey(Message message) throws Exception {
        client.deleteKey(getKey(message));
        message.setHeader(ConsulConstants.CONSUL_RESULT, true);
    }

    @ConsulKVActionProcessor(ConsulKVAction.DELETE_KEYS)
    private void deleteKeys(Message message) throws Exception {
        client.deleteKeys(getKey(message));
        message.setHeader(ConsulConstants.CONSUL_RESULT, true);
    }

    // *************************************************************************
    //
    // *************************************************************************

    private ConsulKVAction getAction(Message message) throws Exception {
        return message.getHeader(
            ConsulConstants.CONSUL_ACTION,
            configuration.getAction(),
            ConsulKVAction.class);
    }

    private boolean valueAsString(Message message) throws Exception {
        return message.getHeader(
            ConsulConstants.CONSUL_VALUE_AS_STRING,
            configuration.isValueAsString(),
            Boolean.class);
    }

    private String getKey(Message message) throws Exception {
        return ObjectHelper.notNull(
            message.getHeader(
                ConsulConstants.CONSUL_KEY,
                String.class),
            ConsulConstants.CONSUL_KEY
        );
    }
}
