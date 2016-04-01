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

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Optional;
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
import org.apache.camel.util.ObjectHelper;

public class ConsulKeyValueConsumer extends AbstractConsulConsumer {
    private final String key;
    private final AtomicReference<BigInteger> index;

    private Runnable watcher;
    private KeyValueClient client;

    protected ConsulKeyValueConsumer(ConsulKeyValueEndpoint endpoint, ConsulConfiguration configuration, Processor processor) {
        super(endpoint, configuration, processor);

        this.key = ObjectHelper.notNull(configuration.getKey(), ConsulConstants.CONSUL_KEY);
        this.index = new AtomicReference<>(BigInteger.valueOf(configuration.getFirstIndex()));
        this.client = null;
        this.watcher = null;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        client = endpoint.getConsul().keyValueClient();
        watcher = configuration.isRecursive() ? new RecursiveWatchHandler() : new WatchHandler();

        watcher.run();
    }

    @Override
    protected void doStop() throws Exception {
        client = null;
        watcher = null;

        super.doStop();
    }

    // *************************************************************************
    // Handlers
    // *************************************************************************

    private void onFailure(Throwable throwable) {
        if (!isRunAllowed()) {
            return;
        }

        getExceptionHandler().handleException("Error watching for key " + this.key, throwable);
    }

    private void onValue(Value value) {
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

    private void onResponse(ConsulResponse<?> response) {
        index.set(response.getIndex());
    }

    // *************************************************************************
    //
    // *************************************************************************

    private class WatchHandler implements Runnable, ConsulResponseCallback<Optional<Value>> {
        @Override
        public void run() {
            client.getValue(
                key,
                QueryOptions.blockSeconds(configuration.getBlockSeconds(), index.get()).build(),
                this
            );
        }

        @Override
        public void onComplete(ConsulResponse<Optional<Value>> consulResponse) {
            if (!isRunAllowed()) {
                return;
            }

            Optional<Value> response = consulResponse.getResponse();
            if (response.isPresent()) {
                ConsulKeyValueConsumer.this.onValue(response.get());
            }

            onResponse(consulResponse);

            run();
        }

        @Override
        public void onFailure(Throwable throwable) {
            ConsulKeyValueConsumer.this.onFailure(throwable);
        }
    }

    private class RecursiveWatchHandler implements Runnable, ConsulResponseCallback<List<Value>> {
        @Override
        public void run() {
            client.getValues(
                key,
                QueryOptions.blockSeconds(configuration.getBlockSeconds(), index.get()).build(),
                this
            );
        }

        @Override
        public void onComplete(ConsulResponse<List<Value>> consulResponse) {
            if (!isRunAllowed()) {
                return;
            }

            consulResponse.getResponse().forEach(ConsulKeyValueConsumer.this::onValue);
            onResponse(consulResponse);

            run();
        }

        @Override
        public void onFailure(Throwable throwable) {
            ConsulKeyValueConsumer.this.onFailure(throwable);
        }
    }
}
