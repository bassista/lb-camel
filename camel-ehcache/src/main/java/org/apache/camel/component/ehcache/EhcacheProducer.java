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

import org.apache.camel.Message;
import org.apache.camel.common.DispatchingProducer;
import org.apache.camel.common.ExchangeProcessor;
import org.apache.camel.common.ExchangeProcessorType;
import org.ehcache.Cache;

public class EhcacheProducer extends DispatchingProducer {
    private final EhcacheConfiguration configuration;
    private final EhcacheManager manager;
    private final Cache<Object, Object> cache;

    public EhcacheProducer(EhcacheEndpoint endpoint, EhcacheConfiguration configuration) throws Exception {
        super(endpoint, EhcacheConstants.ACTION, configuration.getAction(), ExchangeProcessorType.IN);

        this.configuration = configuration;
        this.manager = endpoint.getManager();
        this.cache = manager.getCache();
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_CLEAR)
    protected void onClear(Message message) throws Exception {
        cache.clear();

        setResult(message, true, null, null);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_PUT)
    protected void onPut(Message message) throws Exception {
        cache.put(getKey(message), getValue(message, Object.class));

        setResult(message, true, null, null);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_PUT_ALL)
    protected void onPutAll(Message message) throws Exception {
        cache.putAll(getValue(message, Map.class));

        setResult(message, true, null, null);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_PUT_IF_ABSENT)
    protected void onPutIfAbsent(Message message) throws Exception {
        Object oldValue = cache.putIfAbsent(getKey(message), getValue(message, Object.class));

        setResult(message, true, null, oldValue);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_GET)
    protected void onGet(Message message) throws Exception {
        Object result = cache.get(getKey(message));

        setResult(message, true, result, null);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_GET_ALL)
    protected void onGetAll(Message message) throws Exception {
        Object result = cache.getAll(getHeader(message, EhcacheConstants.KEYS, Set.class));

        setResult(message, true, result, null);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_REMOVE)
    protected void onRemove(Message message) throws Exception {

        boolean success = true;
        Object valueToReplace = message.getHeader(EhcacheConstants.OLD_VALUE);
        if (valueToReplace == null) {
            cache.remove(getKey(message));
        } else {
            success = cache.remove(getKey(message), valueToReplace);
        }

        setResult(message, success, null, null);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_REMOVE_ALL)
    protected void onRemoveAll(Message message) throws Exception {
        cache.removeAll(getHeader(message, EhcacheConstants.KEYS, Set.class));

        setResult(message, true, null, null);
    }

    @ExchangeProcessor(EhcacheConstants.ACTION_REPLACE)
    protected void onReplace(Message message) throws Exception {
        boolean success = true;
        Object oldValue = null;
        Object value = getValue(message, Object.class);
        Object valueToReplace = message.getHeader(EhcacheConstants.OLD_VALUE);
        if (valueToReplace == null) {
            oldValue = cache.replace(getKey(message), value);
        } else {
            success = cache.replace(getKey(message), valueToReplace, value);
        }

        setResult(message, success, null, oldValue);
    }

    // ****************************
    // Helpers
    // ****************************

    private String getKey(Message message) throws Exception {
        return getMandatoryHeader(
            message,
            EhcacheConstants.KEY,
            configuration.getKey(),
            String.class);
    }

    private <T> T getValue(Message message, Class<T> type)  throws Exception {
        return getMandatoryHeader(
            message,
            EhcacheConstants.VALUE,
            message.getBody(type),
            type);
    }

    private void setResult(Message message, boolean success, Object result, Object oldValue) {
        message.setHeader(EhcacheConstants.ACTION_SUCCEEDED, success);
        message.setHeader(EhcacheConstants.ACTION_HAS_RESULT, oldValue != null || result != null);

        if (oldValue != null) {
            message.setHeader(EhcacheConstants.OLD_VALUE, oldValue);
        }
        if (result != null) {
            message.setBody(result);
        }
    }
}
