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

import java.util.Map;

import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.option.PutOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.camel.Message;
import org.apache.camel.component.consul.AbstractConsulEndpoint;
import org.apache.camel.component.consul.AbstractConsulProducer;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.component.consul.ConsulConstants;
import org.apache.camel.component.consul.MessageProcessor;

public class ConsulKeyValueProducer extends AbstractConsulProducer<KeyValueClient> {

    ConsulKeyValueProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint, configuration, c -> c.keyValueClient());
    }

    @Override
    protected void bindActionProcessors(Map<String, MessageProcessor> processors) {
        processors.put(ConsulKeyValueActions.PUT, this::put);
        processors.put(ConsulKeyValueActions.GET_VALUE, this::getValue);
        processors.put(ConsulKeyValueActions.GET_VALUES, this::getValues);
        processors.put(ConsulKeyValueActions.GET_KEYS, this::getKeys);
        processors.put(ConsulKeyValueActions.GET_SESSIONS, this::getSessions);
        processors.put(ConsulKeyValueActions.DELETE_KEY, this::deleteKey);
        processors.put(ConsulKeyValueActions.DELETE_KEYS, this::deleteKeys);
        processors.put(ConsulKeyValueActions.LOCK, this::lock);
        processors.put(ConsulKeyValueActions.UNLOCK, this::unlock);
    }

    // *************************************************************************
    //
    // *************************************************************************

    private void put(Message message) throws Exception {
        message.setHeader(
            ConsulConstants.CONSUL_RESULT,
            getClient().putValue(
                getMandatoryKey(message),
                message.getBody(String.class),
                message.getHeader(ConsulConstants.CONSUL_FLAGS, 0L, Long.class),
                getOption(message, PutOptions.BLANK, PutOptions.class)
            )
        );
    }

    private void getValue(Message message) throws Exception {
        Object result;

        if (isValueAsString(message)) {
            result = getClient().getValueAsString(
                getMandatoryKey(message)
            ).orNull();
        } else {
            result = getClient().getValue(
                getMandatoryKey(message),
                getOption(message, QueryOptions.BLANK, QueryOptions.class)
            ).orNull();
        }

        setBodyAndResult(message, result);
    }

    private void getValues(Message message) throws Exception {
        Object result;

        if (isValueAsString(message)) {
            result = getClient().getValuesAsString(
                getMandatoryKey(message)
            );
        } else {
            result = getClient().getValues(
                getMandatoryKey(message),
                getOption(message, QueryOptions.BLANK, QueryOptions.class)
            );
        }

        setBodyAndResult(message, result);
    }

    private void getKeys(Message message) throws Exception {
        setBodyAndResult(message,getClient().getKeys(getMandatoryKey(message)));
    }

    private void getSessions(Message message) throws Exception {
        setBodyAndResult(message, getClient().getSession(getMandatoryKey(message)));
    }

    private void deleteKey(Message message) throws Exception {
        getClient().deleteKey(getMandatoryKey(message));
        message.setHeader(ConsulConstants.CONSUL_RESULT, true);
    }

    private void deleteKeys(Message message) throws Exception {
        getClient().deleteKeys(getMandatoryKey(message));
        message.setHeader(ConsulConstants.CONSUL_RESULT, true);
    }

    private void lock(Message message) throws Exception {
        message.setHeader(ConsulConstants.CONSUL_RESULT,
            getClient().acquireLock(
                getMandatoryKey(message),
                getBody(message, null, String.class),
                message.getHeader(ConsulConstants.CONSUL_SESSION, "", String.class)
            )
        );
    }

    private void unlock(Message message) throws Exception {
        message.setHeader(ConsulConstants.CONSUL_RESULT,
            getClient().releaseLock(
                getMandatoryKey(message),
                getMandatoryHeader(message, ConsulConstants.CONSUL_SESSION, String.class)
            )
        );
    }
}
