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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.requests.EtcdKeyPutRequest;
import org.apache.camel.Exchange;
import org.apache.camel.util.ObjectHelper;


class EtcdKeysProducer extends AbstractEtcdProducer {
    private final EtcdKeysEndpoint endpoint;
    private final EtcdKeysConfiguration configuration;
    private final String defaultPath;

    EtcdKeysProducer(EtcdKeysEndpoint etcdEndpoint, EtcdKeysConfiguration etcdConfiguration, EtcdActionNamespace etcdActionNamespace, String path) {
        super(etcdEndpoint, etcdConfiguration, etcdActionNamespace, path);

        this.endpoint = etcdEndpoint;
        this.configuration = etcdConfiguration;
        this.defaultPath = etcdEndpoint.getRemainingPath(configuration.getPath());
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        String action = exchange.getIn().getHeader(EtcdConstants.ETCD_ACTION, String.class);
        String path = exchange.getIn().getHeader(EtcdConstants.ETCD_PATH, String.class);
        if (path == null) {
            path = defaultPath;
        }

        ObjectHelper.notEmpty(path, EtcdConstants.ETCD_PATH);
        ObjectHelper.notEmpty(action, EtcdConstants.ETCD_ACTION);

        switch(action) {
            case EtcdConstants.ETCD_KEYS_ACTION_SET:
                processSet(endpoint.getClient(), path, exchange);
                break;
            case EtcdConstants.ETCD_KEYS_ACTION_GET:
                processGet(endpoint.getClient(), path, exchange);
                break;
            case EtcdConstants.ETCD_KEYS_ACTION_DELETE:
                processDel(endpoint.getClient(), path, exchange);
                break;
            default:
                throw new IllegalArgumentException("Unknown action " + action);
        }
    }

    // *************************************************************************
    // Processors
    // *************************************************************************

    private void processSet(EtcdClient client, String path, Exchange exchange) throws Exception {
        EtcdKeyPutRequest request = client.put(path, exchange.getIn().getBody(String.class));
        if (configuration.hasTimeToLive()) {
            request.ttl(configuration.getTimeToLive());
        }
        if (configuration.hasTimeout()) {
            request.timeout(configuration.getTimeout(), TimeUnit.MILLISECONDS);
        }

        try {
            exchange.getIn().setBody(request.send().get());
        } catch (TimeoutException e) {
            exchange.getIn().setHeader(EtcdConstants.ETCD_TIMEOUT, true);
        }
    }

    private void processGet(EtcdClient client, String path, Exchange exchange) throws Exception {
    }

    private void processDel(EtcdClient client, String path, Exchange exchange) throws Exception {
    }
}
