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

import java.lang.ref.WeakReference;
import java.util.Map;

import net.openhft.chronicle.engine.api.map.MapView;
import net.openhft.chronicle.engine.api.pubsub.TopicPublisher;
import net.openhft.chronicle.engine.api.tree.AssetTree;
import org.apache.camel.InvokeOnHeader;
import org.apache.camel.Message;
import org.apache.camel.impl.HeaderSelectorProducer;
import org.apache.camel.util.ObjectHelper;

import static org.apache.camel.component.chronicle.ChronicleEngineHelper.mandatoryBody;
import static org.apache.camel.component.chronicle.ChronicleEngineHelper.mandatoryKey;


public class ChronicleEngineProducer extends HeaderSelectorProducer {
    private final String path;
    private WeakReference<TopicPublisher<Object, Object>> topicPublisher;
    private WeakReference<MapView<Object, Object>> mapView;
    private AssetTree client;

    public ChronicleEngineProducer(ChronicleEngineEnpoint endpoint) {
        super(endpoint, ChronicleEngineConstants.ACTION, endpoint.getConfiguration().getAction());

        this.path = endpoint.getPath();
        this.topicPublisher = null;
        this.mapView = null;
    }

    private synchronized TopicPublisher<Object, Object> topicPublisher() {
        TopicPublisher<Object, Object> rv = topicPublisher.get();
        if (rv == null) {
            topicPublisher = new WeakReference<>(
                rv = client.acquireTopicPublisher(path, Object.class, Object.class)
            );
        }

        return rv;
    }

    private synchronized MapView<Object, Object> mapView() {
        MapView<Object, Object> rv = mapView.get();
        if (rv == null) {
            mapView = new WeakReference<>(
                rv = client.acquireMap(path, Object.class, Object.class)
            );
        }

        return rv;
    }

    // ***************************
    // AssetTree
    // ***************************

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (client != null) {
            throw new IllegalStateException("AssetTree already configured");
        }

        client = ((ChronicleEngineEnpoint)getEndpoint()).createRemoteAssetTree();
    }

    @Override
    protected void doStop() throws Exception {
        if (client != null) {
            client.close();
            client = null;
        }

        super.doStop();
    }

    // ***************************
    // Actions / Topic
    // ***************************

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_PUBLISH)
    public void onPublish(Message message) {
        topicPublisher().publish(mandatoryKey(message), mandatoryBody(message));
    }

    // ***************************
    // Actions / Map / Put
    // ***************************

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_PUT)
    public void onPut(Message message) {
        message.setHeader(
            ChronicleEngineConstants.OLD_VALUE,
            mapView().put(mandatoryKey(message), mandatoryBody(message))
        );
    }

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_GET_AND_PUT)
    public void onGetAndPut(Message message) {
        message.setBody(
            mapView().getAndPut(mandatoryKey(message), mandatoryBody(message))
        );
    }

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_PUT_ALL)
    public void onPutAll(Message message) {
        mapView().putAll(
            ObjectHelper.notNull(message.getBody(Map.class), ChronicleEngineConstants.VALUE)
        );
    }

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_PUT_IF_ABSENT)
    public void onPutIfAbsent(Message message) {
        message.setHeader(
            ChronicleEngineConstants.RESULT,
            mapView().putIfAbsent(mandatoryKey(message), mandatoryBody(message))
        );
    }

    // ***************************
    // Actions / Map / Get
    // ***************************

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_GET)
    public void onGet(Message message) {
        message.setBody(
            mapView().getOrDefault(
                mandatoryKey(message),
                message.getHeader(ChronicleEngineConstants.DEFAULT_VALUE))
        );
    }

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_GET_AND_REMOVE)
    public void onGetAndRemove(Message message) {
        message.setBody(
            mapView().getAndRemove(mandatoryKey(message))
        );
    }

    // ***************************
    // Actions / Map
    // ***************************

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_REMOVE)
    public void onRemove(Message message) {
        Object oldValue = message.getHeader(ChronicleEngineConstants.OLD_VALUE);
        if (oldValue != null) {
            message.setHeader(
                ChronicleEngineConstants.RESULT,
                mapView().remove(mandatoryKey(message),oldValue)
            );
        } else {
            message.setHeader(
                ChronicleEngineConstants.OLD_VALUE,
                mapView().remove(mandatoryKey(message))
            );
        }
    }

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_IS_EMPTY)
    public void onIsEmpty(Message message) {
        message.setHeader(
            ChronicleEngineConstants.RESULT,
            mapView().isEmpty()
        );
    }

    @InvokeOnHeader(ChronicleEngineConstants.ACTION_IS_SIZE)
    public void onSize(Message message) {
        message.setHeader(
            ChronicleEngineConstants.RESULT,
            mapView().size()
        );
    }

}
