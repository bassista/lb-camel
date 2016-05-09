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
package org.apache.camel.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatchingProducer extends DefaultProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchingProducer.class);

    private final String header;
    private final String defaultHeaderValue;
    private Map<String, Processor> handlers;
    private final Object target;

    public DispatchingProducer(Endpoint endpoint, String header) {
        this(endpoint, header, null);
    }

    public DispatchingProducer(Endpoint endpoint, String header, String defaultHeaderValue) {
        super(endpoint);

        this.header = header;
        this.defaultHeaderValue = defaultHeaderValue;
        this.handlers = new HashMap<>();
        this.target = this;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final String action = exchange.getIn().getHeader(header, defaultHeaderValue, String.class);
        if (action == null) {
            throw new NoSuchHeaderException(exchange, header, String.class);
        }

        final Processor processor = handlers.getOrDefault(action, this::onMissingProcessor);
        processor.process(exchange);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (handlers.isEmpty()) {
            doSetup();

            for (final Method method : this.getClass().getDeclaredMethods()) {
                Handlers annotation = method.getAnnotation(Handlers.class);
                if (annotation != null) {
                    for (Handler processor : annotation.value()) {
                        bind(processor, method);
                    }
                } else {
                    bind(method.getAnnotation(Handler.class), method);
                }
            }

            handlers = Collections.unmodifiableMap(handlers);
        }
    }

    protected void doSetup() throws Exception {
    }

    private void bind(Handler handler, final Method method) {
        if (handler == null) {
            return;
        }

        ObjectHelper.notNull(method, "method");

        if (method.getParameterCount() == 1) {
            method.setAccessible(true);

            final Class<?> type = method.getParameterTypes()[0];

            LOGGER.debug("bind key={}, class={}, method={}, type={}",
                handler.value(), this.getClass(), method.getName(), type);

            if (Message.class.isAssignableFrom(type)) {
                bind(handler.value(), e -> method.invoke(target, e.getIn()));
            } else {
                bind(handler.value(), e -> method.invoke(target, e));
            }
        }
    }

    protected final void bind(String key, Processor processor) {
        if (handlers.containsKey(key)) {
            LOGGER.warn("A processor was already set for action {}", key);
        }

        this.handlers.put(key, processor);
    }

    private void onMissingProcessor(Exchange exchange) throws Exception {
        throw new IllegalStateException(
            "Unsupported operation " + exchange.getIn().getHeader(header)
        );
    }

    @Repeatable(Handlers.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Handler {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public  @interface Handlers {
        Handler[] value();
    }
}
