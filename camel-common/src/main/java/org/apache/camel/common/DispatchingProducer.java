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
import java.util.function.Function;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DispatchingProducer extends DefaultProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatchingProducer.class);

    private final String header;
    private final String defaultHeaderValue;
    private Map<String, Processor> processors;
    private Object target;

    public DispatchingProducer(Endpoint endpoint, String header) {
        this(endpoint, header, null);
    }

    public DispatchingProducer(Endpoint endpoint, String header, String defaultHeaderValue) {
        super(endpoint);

        this.header = header;
        this.defaultHeaderValue = defaultHeaderValue;
        this.processors = new HashMap<>();
        this.target = null;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        final String action = getMandatoryHeader(exchange.getIn(), header, defaultHeaderValue, String.class);
        final Processor processor = processors.getOrDefault(action, this::onMissingProcessor);

        processor.process(exchange);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (processors.isEmpty()) {
            doSetup();

            for (final Method method : this.getClass().getDeclaredMethods()) {
                Handlers annotation = method.getAnnotation(Handlers.class);
                if (annotation != null) {
                    for (Handler processor : annotation.value()) {
                        bind(processor, method);
                    }
                }

                bind(method.getAnnotation(Handler.class), method);
            }

            processors = Collections.unmodifiableMap(processors);
        }
    }

    protected void doSetup() throws Exception {
    }

    private void bind(Handler exchangeProcessor, Method method) {
        if (exchangeProcessor != null) {

            Object targetObject = target != null ? target : this;
            if (!method.isAccessible()) {
                LOGGER.debug("Method {}::{} is not accessible, force it",
                    targetObject.getClass().getName(), method.getName());

                method.setAccessible(true);
            }

            Function<Exchange, Object> converter = null;
            if (method.getParameterCount() == 1) {
                Class<?> type = method.getParameterTypes()[0];
                if (Message.class.isAssignableFrom(type)) {
                    converter = e -> e.getIn();
                } else if (Exchange.class.isAssignableFrom(type)) {
                    converter = null;
                }

                LOGGER.debug("bind key={}, class={}, method={}, type={}",
                    exchangeProcessor.value(), this.getClass(), method.getName(), type);

                bind(exchangeProcessor.value(), new HandlerInvoker(targetObject, method, converter));
            }
        }
    }

    protected final void bind(String key, Processor processor) {
        if (processors.containsKey(key)) {
            LOGGER.warn("A processor was already set for action {}", key);
        }

        this.processors.put(key, processor);
    }

    protected final void setTarget(Object target) {
        this.target = target;
    }

    protected <D> D getHeader(Exchange exchange, String header, D defaultValue, Class<D> type) {
        return getHeader(exchange.getIn(), header, defaultValue, type);
    }

    protected <D> D getHeader(Message message, String header, D defaultValue, Class<D> type) {
        return message.getHeader(header, defaultValue, type);
    }

    protected <D> D getHeader(Exchange exchange, String header, Class<D> type) {
        return getHeader(exchange.getIn(), header, null, type);
    }

    protected <D> D getHeader(Message message, String header, Class<D> type) {
        return message.getHeader(header, null, type);
    }

    protected <D> D getMandatoryHeader(Exchange exchange, String header, Class<D> type) throws Exception {
        return getMandatoryHeader(exchange.getIn(), header, type);
    }

    protected <D> D getMandatoryHeader(Exchange exchange, String header, D defaultValue, Class<D> type) throws Exception {
        return getMandatoryHeader(exchange.getIn(), header, defaultValue, type);
    }

    protected <D> D getMandatoryHeader(Message message, String header, Class<D> type) throws Exception {
        return getMandatoryHeader(message, header, null, type);
    }

    protected <D> D getMandatoryHeader(Message message, String header, D defaultValue, Class<D> type) throws Exception {
        D value = getHeader(message, header, defaultValue, type);
        if (value == null) {
            throw new NoSuchHeaderException(message.getExchange(), header, type);
        }

        return value;
    }

    protected Message getResultMessage(Exchange exchange) {
        Message message;
        if (ExchangeHelper.isOutCapable(exchange)) {
            message = exchange.getOut();
            message.copyFrom(exchange.getIn());
        } else {
            message = exchange.getIn();
        }

        return message;
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

    private class HandlerInvoker implements Processor {
        private final Object target;
        private final Method method;
        private final Function<Exchange, Object> converter;

        public HandlerInvoker(Object target, Method method) {
            this(target, method, null);
        }

        public HandlerInvoker(Object target, Method method, Function<Exchange, Object> converter) {
            this.target = target;
            this.method = method;
            this.converter = converter;
        }

        @Override
        public void process(Exchange exchange) throws Exception {
            method.invoke(target, converter == null ? exchange : converter.apply(exchange));
        }
    }
}
