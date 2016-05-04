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
package org.apache.camel.component.ehcache;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.ehcache.Cache;

public class EhcacheProducer extends DefaultProducer {
    private final EhcacheConfiguration configuration;
    private final EhcacheManager manager;
    private final Cache<Object, Object> cache;

    public EhcacheProducer(EhcacheEndpoint endpoint, EhcacheConfiguration configuration) throws Exception {
        super(endpoint);

        this.configuration = configuration;
        this.manager = endpoint.getManager();
        this.cache = manager.getCache();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final String action = getAction(exchange);

        Object resultBody = null;
        Object oldValue = null;
        boolean success = true;

        switch (action) {
        case EhcacheConstants.ACTION_CLEAR:
            cache.clear();
            break;
        case EhcacheConstants.ACTION_PUT:
            cache.put(getKey(exchange), getValue(exchange));
            break;
        case EhcacheConstants.ACTION_PUT_ALL:
            cache.putAll(getValue(exchange, Map.class));
            break;
        case EhcacheConstants.ACTION_PUT_IF_ABSENT:
            oldValue = cache.putIfAbsent(getKey(exchange), getValue(exchange));
            break;
        case EhcacheConstants.ACTION_GET:
            resultBody = cache.get(getKey(exchange));
            break;
        case EhcacheConstants.ACTION_GET_ALL:
            resultBody = cache.getAll(getInHeaderValue(exchange, EhcacheConstants.KEYS, Set.class));
            break;
        case EhcacheConstants.ACTION_REMOVE: {
                Object valueToReplace = exchange.getIn().getHeader(EhcacheConstants.OLD_VALUE);
                if (valueToReplace == null) {
                    cache.remove(getKey(exchange));
                } else {
                    success = cache.remove(getKey(exchange), valueToReplace);
                }
            }
            break;
        case EhcacheConstants.ACTION_REMOVE_ALL:
            cache.removeAll(getInHeaderValue(exchange, EhcacheConstants.KEYS, Set.class));
            break;
        case EhcacheConstants.ACTION_REPLACE: {
                Object value = getValue(exchange);
                Object valueToReplace = exchange.getIn().getHeader(EhcacheConstants.OLD_VALUE);
                if (valueToReplace == null) {
                    oldValue = cache.replace(getKey(exchange), value);
                } else {
                    success = cache.replace(getKey(exchange), valueToReplace, value);
                }
            }

            break;
        default:
            throw new IllegalArgumentException("Unknown action " + action);
        }

        Message msg = getResultMessage(exchange);
        msg.setHeader(EhcacheConstants.SUCCESS, success);
        msg.setHeader(EhcacheConstants.HAS_RESULT, oldValue != null || resultBody != null);

        if (oldValue != null) {
            msg.setHeader(EhcacheConstants.OLD_VALUE, oldValue);
        }
        if (resultBody != null) {
            msg.setBody(resultBody);
        }
    }

    // **********************************
    // Header helpers
    // **********************************

    private <T> T getInHeaderValue(Exchange exchange, String header, Supplier<T> defaultValueSupplier, Class<T> type) {
        T value = exchange.getIn().getHeader(header, type);
        if (value == null && defaultValueSupplier != null) {
            value = defaultValueSupplier.get();
        }

        return ObjectHelper.notNull(value, header);
    }

    private <T> T getInHeaderValue(Exchange exchange, String header, Class<T> type) {
        return getInHeaderValue(exchange, header, null, type);
    }

    private String getAction(Exchange exchange) {
        return getInHeaderValue(
            exchange,
            EhcacheConstants.ACTION,
            configuration::getAction,
            String.class);
    }

    private String getKey(Exchange exchange) {
        return getInHeaderValue(
            exchange,
            EhcacheConstants.KEY,
            configuration::getKey,
            String.class);
    }

    private Object getValue(Exchange exchange) {
        return getInHeaderValue(
            exchange,
            EhcacheConstants.VALUE,
            exchange.getIn()::getBody,
            Object.class);
    }

    private <T> T getValue(Exchange exchange, Class<T> type) {
        return getInHeaderValue(
            exchange,
            EhcacheConstants.VALUE,
            () -> exchange.getIn().getBody(type),
            type);
    }

    // **********************************
    // Misc helpers
    // **********************************

    private Message getResultMessage(Exchange exchange) {
        Message message;
        if (exchange.getPattern().isOutCapable()) {
            message = exchange.getOut();
            message.copyFrom(exchange.getIn());
        } else {
            message = exchange.getIn();
        }

        return message;
    }
}
