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
package org.apache.camel.component.chronicle;

import java.util.ArrayList;
import java.util.List;

import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.pubsub.InvalidSubscriberException;
import net.openhft.chronicle.engine.api.pubsub.Subscriber;
import net.openhft.chronicle.engine.api.pubsub.TopicSubscriber;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import net.openhft.chronicle.engine.tree.TopologicalEvent;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.component.chronicle.ChronicleEngineHelper.fromMapEventTypeName;
import static org.apache.camel.component.chronicle.ChronicleEngineHelper.toMapEventTypeName;

/**
 * The Chronicle Engine consumer.
 */
class ChronicleEngineConsumer extends AbstractChronicleConsumer<ChronicleEngineEnpoint, ChronicleEngineConfiguration> {
    private AssetTree tree;

    ChronicleEngineConsumer(ChronicleEngineEnpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    protected void doStart() throws Exception {
        if (tree != null) {
            throw new IllegalStateException("AssetTree already configured");
        }

        ChronicleEngineConfiguration conf = getConfiguration();
        ObjectHelper.notNull(conf.getHostPortDescription(), "HostPortDescription");
        ObjectHelper.notNull(conf.getWireType(), "WireType");

        tree = new VanillaAssetTree()
            .forRemoteAccess(
                conf.getHostPortDescription().split(","),
                WireType.valueOf(conf.getWireType()),
                t -> { throw new RuntimeCamelException(t); }
        );

        if (conf.isSubscribeMapEvents()) {
            tree.registerSubscriber(
                getChronicleEnpoint().getPath(),
                MapEvent.class,
                new EngineMapEventListener(conf.getFilteredMapEvents()));
        }

        if (conf.isSubscribeTopologicalEvents()) {
            tree.registerSubscriber(
                getChronicleEnpoint().getPath(),
                TopologicalEvent.class,
                new EngineTopologicalEventListener());
        }

        if (conf.isSubscribeTopicEvents()) {
            tree.registerTopicSubscriber(
                getChronicleEnpoint().getPath(),
                Object.class,
                Object.class,
                new EngineTopicEventListener());
        }
    }

    @Override
    protected void doStop() throws Exception {
        if (tree != null) {
            tree.close();
            tree = null;
        }
    }

    // ****************************
    // MAP EVENT LISTENER
    // ****************************

    private class EngineMapEventListener implements Subscriber<MapEvent> {
        private List<Class<? extends MapEvent>> filteredEvents;

        EngineMapEventListener(String toBeFiltered) {
            this.filteredEvents = null;

            if (ObjectHelper.isNotEmpty(toBeFiltered)) {
                String[] events = toBeFiltered.split(",");
                filteredEvents = new ArrayList<>(events.length);

                for (int i=0; i<events.length; i++) {
                    filteredEvents.add(fromMapEventTypeName(events[i]));
                }
            }
        }

        @Override
        public void onMessage(MapEvent event) throws InvalidSubscriberException {
            if (filteredEvents != null && filteredEvents.contains(event.getClass())) {
                return;
            }

            Exchange exchange = getEndpoint().createExchange();
            exchange.getIn().setHeader(ChronicleEngineConstants.PATH, getChronicleEnpoint().getPath());
            exchange.getIn().setHeader(ChronicleEngineConstants.ASSET_NAME, event.assetName());
            exchange.getIn().setHeader(ChronicleEngineConstants.MAP_EVENT_TYPE, toMapEventTypeName(event));
            exchange.getIn().setHeader(ChronicleEngineConstants.MAP_KEY, event.getKey());
            exchange.getIn().setBody(event.getValue());

            if (event.oldValue() != null) {
                exchange.getIn().setHeader(ChronicleEngineConstants.MAP_OLD_VALUE, event.oldValue());
            }

            try {
                getProcessor().process(exchange);
            } catch (Exception e) {
                throw new RuntimeCamelException(e);
            }
        }
    }

    // ****************************
    // TOPOLOGICAL EVENT LISTENER
    // ****************************

    private class EngineTopologicalEventListener implements Subscriber<TopologicalEvent> {
        @Override
        public void onMessage(TopologicalEvent event) throws InvalidSubscriberException {
            Exchange exchange = getEndpoint().createExchange();
            exchange.getIn().setHeader(ChronicleEngineConstants.PATH, getChronicleEnpoint().getPath());
            exchange.getIn().setHeader(ChronicleEngineConstants.ASSET_NAME, event.assetName());
            exchange.getIn().setHeader(ChronicleEngineConstants.TOPOLOGICAL_EVENT_NAME, event.name());
            exchange.getIn().setHeader(ChronicleEngineConstants.TOPOLOGICAL_EVENT_FULL_NAME, event.fullName());
            exchange.getIn().setHeader(ChronicleEngineConstants.TOPOLOGICAL_EVENT_ADDED, Boolean.toString(event.added()));

            try {
                getProcessor().process(exchange);
            } catch (Exception e) {
                throw new RuntimeCamelException(e);
            }
        }
    }

    // ****************************
    // TOPIC EVENT LISTENER
    // ****************************

    private class EngineTopicEventListener implements TopicSubscriber<Object, Object> {
        @Override
        public void onMessage(Object topic, Object message) throws InvalidSubscriberException {
            Exchange exchange = getEndpoint().createExchange();
            exchange.getIn().setHeader(ChronicleEngineConstants.PATH, getChronicleEnpoint().getPath());
            exchange.getIn().setHeader(ChronicleEngineConstants.TOPIC, topic);
            exchange.getIn().setBody(message);

            try {
                getProcessor().process(exchange);
            } catch (Exception e) {
                throw new RuntimeCamelException(e);
            }
        }
    }
}
