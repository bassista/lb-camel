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

import java.util.List;

import com.google.common.base.Optional;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.async.ConsulResponseCallback;
import com.orbitz.consul.model.ConsulResponse;
import com.orbitz.consul.model.kv.Value;
import com.orbitz.consul.option.QueryOptions;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.consul.AbstractConsulConsumer;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.component.consul.ConsulConstants;

public class ConsulKeyValueConsumer extends AbstractConsulConsumer<KeyValueClient> {

    protected ConsulKeyValueConsumer(ConsulKeyValueEndpoint endpoint, ConsulConfiguration configuration, Processor processor) {
        super(endpoint, configuration, processor);
    }

    @Override
    protected KeyValueClient createClient(Consul consul) throws Exception {
        return consul.keyValueClient();
    }

    @Override
    protected Runnable createWatcher(KeyValueClient client) throws Exception {
        return configuration.isRecursive() ? new RecursiveWatchHandler(client) : new WatchHandler(client);
    }

    // *************************************************************************
    // Handlers
    // *************************************************************************

    private abstract class KeyValueWatcher<T> extends AbstractWatcher implements ConsulResponseCallback<T> {
        public KeyValueWatcher(KeyValueClient client) {
            super(client);
        }

        @Override
        public void onComplete(ConsulResponse<T> consulResponse) {
            if (isRunAllowed()) {
                onResponse(consulResponse.getResponse());
                setIndex(consulResponse.getIndex());
                watch();
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            onError(throwable);
        }

        protected void onValue(Value value) {
            final Exchange exchange = endpoint.createExchange();
            final Message message = exchange.getIn();

            message.setHeader(ConsulConstants.CONSUL_KEY, value.getKey());
            message.setHeader(ConsulConstants.CONSUL_RESULT, true);
            message.setHeader(ConsulConstants.CONSUL_FLAGS, value.getFlags());
            message.setHeader(ConsulConstants.CONSUL_CREATE_INDEX, value.getCreateIndex());
            message.setHeader(ConsulConstants.CONSUL_LOCK_INDEX, value.getLockIndex());
            message.setHeader(ConsulConstants.CONSUL_MODIFY_INDEX, value.getModifyIndex());
            message.setHeader(ConsulConstants.CONSUL_SESSION, value.getSession().orNull());
            message.setBody(
                configuration.isValueAsString() ? value.getValueAsString().orNull() : value.getValue().orNull()
            );

            try {
                getProcessor().process(exchange);
            } catch (Exception e) {
                getExceptionHandler().handleException("Error processing exchange", exchange, e);
            }
        }

        protected abstract void onResponse(T consulResponse);
    }

    private class WatchHandler extends KeyValueWatcher<Optional<Value>> {
        public WatchHandler(KeyValueClient client) {
            super(client);
        }

        @Override
        public void watch() {
            client.getValue(
                key,
                QueryOptions.blockSeconds(configuration.getBlockSeconds(), index.get()).build(),
                this
            );
        }

        @Override
        public void onResponse(Optional<Value> value) {
            if (value.isPresent()) {
                onValue(value.get());
            }
        }
    }

    private class RecursiveWatchHandler extends KeyValueWatcher<List<Value>> {
        public RecursiveWatchHandler(KeyValueClient client) {
            super(client);
        }

        @Override
        public void watch() {
            client.getValues(
                key,
                QueryOptions.blockSeconds(configuration.getBlockSeconds(), index.get()).build(),
                this
            );
        }

        @Override
        public void onResponse(List<Value> values) {
            values.forEach(this::onValue);
        }
    }
}
