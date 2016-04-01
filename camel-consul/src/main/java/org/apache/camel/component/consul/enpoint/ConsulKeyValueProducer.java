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

import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.option.PutOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.camel.Message;
import org.apache.camel.component.consul.AbstractConsulEndpoint;
import org.apache.camel.component.consul.AbstractConsulProducer;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.component.consul.ConsulConstants;

public class ConsulKeyValueProducer extends AbstractConsulProducer {
    private KeyValueClient client;

    ConsulKeyValueProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint, configuration);

        this.client = null;
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

    @ConsulActionProcessor(ConsulKeyValueActions.PUT)
    private void put(Message message) throws Exception {
        message.setHeader(
            ConsulConstants.CONSUL_RESULT,
            client.putValue(
                getMandatoryKey(message),
                message.getBody(String.class),
                message.getHeader(ConsulConstants.CONSUL_FLAGS, 0L, Long.class),
                getOption(message, PutOptions.BLANK, PutOptions.class)
            )
        );
    }

    @ConsulActionProcessor(ConsulKeyValueActions.GET_VALUE)
    private void getValue(Message message) throws Exception {
        Object result;

        if (isValueAsString(message)) {
            result = client.getValueAsString(
                getMandatoryKey(message)
            ).orNull();
        } else {
            result = client.getValue(
                getMandatoryKey(message),
                getOption(message, QueryOptions.BLANK, QueryOptions.class)
            ).orNull();
        }

        setBodyAndResult(message, result);
    }

    @ConsulActionProcessor(ConsulKeyValueActions.GET_VALUES)
    private void getValues(Message message) throws Exception {
        Object result;

        if (isValueAsString(message)) {
            result = client.getValuesAsString(
                getMandatoryKey(message)
            );
        } else {
            result = client.getValues(
                getMandatoryKey(message),
                getOption(message, QueryOptions.BLANK, QueryOptions.class)
            );
        }

        setBodyAndResult(message, result);
    }

    @ConsulActionProcessor(ConsulKeyValueActions.GET_KEYS)
    private void getKeys(Message message) throws Exception {
        setBodyAndResult(message,client.getKeys(getMandatoryKey(message)));
    }

    @ConsulActionProcessor(ConsulKeyValueActions.GET_SESSIONS)
    private void getSessions(Message message) throws Exception {
        setBodyAndResult(message, client.getSession(getMandatoryKey(message)));
    }

    @ConsulActionProcessor(ConsulKeyValueActions.DELETE_KEY)
    private void deleteKey(Message message) throws Exception {
        client.deleteKey(getMandatoryKey(message));
        message.setHeader(ConsulConstants.CONSUL_RESULT, true);
    }

    @ConsulActionProcessor(ConsulKeyValueActions.DELETE_KEYS)
    private void deleteKeys(Message message) throws Exception {
        client.deleteKeys(getMandatoryKey(message));
        message.setHeader(ConsulConstants.CONSUL_RESULT, true);
    }

    @ConsulActionProcessor(ConsulKeyValueActions.LOCK)
    private void lock(Message message) throws Exception {
        message.setHeader(ConsulConstants.CONSUL_RESULT,
            client.acquireLock(
                getMandatoryKey(message),
                getBody(message, null, String.class),
                message.getHeader(ConsulConstants.CONSUL_SESSION, "", String.class)
            )
        );
    }

    @ConsulActionProcessor(ConsulKeyValueActions.UNLOCK)
    private void unlock(Message message) throws Exception {
        message.setHeader(ConsulConstants.CONSUL_RESULT,
            client.releaseLock(
                getMandatoryKey(message),
                getMandatoryHeader(message, ConsulConstants.CONSUL_SESSION, String.class)
            )
        );
    }
}
