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
import java.util.concurrent.atomic.AtomicReference;

import com.orbitz.consul.EventClient;
import com.orbitz.consul.async.EventResponseCallback;
import com.orbitz.consul.model.EventResponse;
import com.orbitz.consul.model.event.Event;
import com.orbitz.consul.option.QueryOptions;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.consul.AbstractConsulConsumer;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.component.consul.ConsulConstants;
import org.apache.camel.util.ObjectHelper;

public class ConsulEventConsumer extends AbstractConsulConsumer {
    private final String key;
    private final AtomicReference<BigInteger> index;

    private Runnable watcher;
    private EventClient client;

    protected ConsulEventConsumer(ConsulEventEndpoint endpoint, ConsulConfiguration configuration, Processor processor) {
        super(endpoint, configuration, processor);

        this.key = ObjectHelper.notNull(configuration.getKey(), ConsulConstants.CONSUL_KEY);
        this.index = new AtomicReference<>(BigInteger.valueOf(configuration.getFirstIndex()));
        this.client = null;
        this.watcher = null;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        client = endpoint.getConsul().eventClient();
        watcher = new WatchHandler();

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

    private void onEvent(Event event) {
        final Exchange exchange = endpoint.createExchange();
        final Message message = exchange.getIn();

        message.setHeader(ConsulConstants.CONSUL_KEY, key);
        message.setHeader(ConsulConstants.CONSUL_RESULT, true);
        message.setHeader(ConsulConstants.CONSUL_EVENT_ID, event.getId());
        message.setHeader(ConsulConstants.CONSUL_EVENT_NAME, event.getName());
        message.setHeader(ConsulConstants.CONSUL_EVENT_LTIME, event.getLTime());
        message.setHeader(ConsulConstants.CONSUL_NODE_FILTER, event.getNodeFilter());
        message.setHeader(ConsulConstants.CONSUL_SERVICE_FILTER, event.getServiceFilter());
        message.setHeader(ConsulConstants.CONSUL_TAG_FILTER, event.getTagFilter());
        message.setHeader(ConsulConstants.CONSUL_VERSION, event.getVersion());
        message.setBody(event.getPayload().orNull());

        try {
            getProcessor().process(exchange);
        } catch (Exception e) {
            getExceptionHandler().handleException("Error processing exchange", exchange, e);
        }
    }

    private void onResponse(EventResponse response) {
        index.set(response.getIndex());
    }

    // *************************************************************************
    //
    // *************************************************************************

    private class WatchHandler implements Runnable, EventResponseCallback {
        @Override
        public void run() {
            client.listEvents(
                key,
                QueryOptions.blockSeconds(configuration.getBlockSeconds(), index.get()).build(),
                this
            );
        }

        @Override
        public void onComplete(EventResponse eventResponse) {
            if (!isRunAllowed()) {
                return;
            }

            eventResponse.getEvents().forEach(ConsulEventConsumer.this::onEvent);
            onResponse(eventResponse);
        }

        @Override
        public void onFailure(Throwable throwable) {
            ConsulEventConsumer.this.onFailure(throwable);
        }
    }
}
