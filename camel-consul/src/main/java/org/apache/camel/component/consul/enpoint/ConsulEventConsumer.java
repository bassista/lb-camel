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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsulEventConsumer extends AbstractConsulConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulEventConsumer.class);

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
        if (isRunAllowed()) {
            getExceptionHandler().handleException("Error watching for event " + this.key, throwable);
        }
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
            if (isRunAllowed()) {
                List<Event> events = filterEvents(eventResponse.getEvents(), index.get());
                events.forEach(ConsulEventConsumer.this::onEvent);

                onResponse(eventResponse);

                run();
            }
        }

        @Override
        public void onFailure(Throwable throwable) {
            ConsulEventConsumer.this.onFailure(throwable);
        }

        /**
         * from spring-cloud-consul (https://github.com/spring-cloud/spring-cloud-consul):
         *     spring-cloud-consul-bus/src/main/java/org/springframework/cloud/consul/bus/EventService.java
         */
        private List<Event> filterEvents(List<Event> toFilter, BigInteger lastIndex) {
            List<Event> events = toFilter;
            if (lastIndex != null) {
                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    BigInteger eventIndex = getEventIndexFromId(event);
                    if (eventIndex.equals(lastIndex)) {
                        events = events.subList(i + 1, events.size());
                        break;
                    }
                }
            }
            return events;
        }

        // TODO: To be refactored
        private BigInteger getEventIndexFromId(Event event) {
            String eventId = event.getId();
            String lower = eventId.substring(0, 8) + eventId.substring(9, 13) + eventId.substring(14, 18);
            String upper = eventId.substring(19, 23) + eventId.substring(24, 36);

            BigInteger lowVal = new BigInteger(lower, 16);
            BigInteger highVal = new BigInteger(upper, 16);

            return lowVal.xor(highVal);
        }
    }
}
