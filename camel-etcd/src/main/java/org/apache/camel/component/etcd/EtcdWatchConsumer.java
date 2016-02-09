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

package org.apache.camel.component.etcd;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import mousio.client.promises.ResponsePromise;
import mousio.etcd4j.requests.EtcdKeyGetRequest;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtcdWatchConsumer extends AbstractEtcdConsumer implements ResponsePromise.IsSimplePromiseResponseHandler<EtcdKeysResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtcdWatchConsumer.class);

    private final AtomicBoolean running;
    private final EtcdWatchEndpoint endpoint;
    private final EtcdWatchConfiguration configuration;
    private final String key;

    EtcdWatchConsumer(EtcdWatchEndpoint etcdEndpoint, Processor processor, EtcdWatchConfiguration etcdConfiguration, EtcdActionNamespace etcdActionNamespace, String path) {
        super(etcdEndpoint, processor, etcdConfiguration, etcdActionNamespace, path);

        this.running = new AtomicBoolean(false);
        this.endpoint = etcdEndpoint;
        this.configuration = etcdConfiguration;

        String key = endpoint.getPath().substring(EtcdActionNamespace.WATCH.name().length() + 1);
        if (ObjectHelper.isEmpty(key)) {
            key = configuration.getPath();
        }

        if (ObjectHelper.isEmpty(key)) {
            throw new IllegalArgumentException("No key to watch");
        }

        this.key = key;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        running.set(true);
        watch();
    }

    @Override
    protected void doStop() throws Exception {
        running.set(false);

        super.doStop();
    }

    @Override
    public void onResponse(ResponsePromise<EtcdKeysResponse> promise) {
        if (!running.get()) {
            return;
        }

        try {
            EtcdKeysResponse response = promise.get();

            Exchange exchange = endpoint.createExchange();
            exchange.getOut().setHeader(EtcdConstants.ETCD_ACTION_PATH, response.node.key);
            exchange.getOut().setBody(response);

            getProcessor().process(exchange);

            watch();
        } catch (TimeoutException e) {
            LOGGER.debug("Timeout watching for " + key);

            if (configuration.isSendEmptyExchangeOnTimeout()) {
                try {
                    Exchange exchange = endpoint.createExchange();
                    exchange.getOut().setHeader(EtcdConstants.ETCD_TIMEOUT, true);
                    exchange.getOut().setHeader(EtcdConstants.ETCD_ACTION_PATH, key);
                    exchange.getOut().setBody(null);

                    getProcessor().process(exchange);
                } catch (Exception e1) {
                    throw new IllegalArgumentException(e);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void watch() {
        if (!running.get()) {
            return;
        }

        EtcdKeyGetRequest request = endpoint.getClient().get(key).waitForChange();
        if (configuration.isRecursive()) {
            request.recursive();
        }
        if (configuration.hasTimeout()) {
            request.timeout(configuration.getTimeout(), TimeUnit.MILLISECONDS);
        }

        try {
            request.send().addListener(this);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
