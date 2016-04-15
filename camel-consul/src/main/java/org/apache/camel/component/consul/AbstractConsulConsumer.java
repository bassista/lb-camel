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
package org.apache.camel.component.consul;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.orbitz.consul.Consul;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.ObjectHelper;

/**
 * @author lburgazzoli
 */
public abstract class AbstractConsulConsumer<C> extends DefaultConsumer {
    protected final AbstractConsulEndpoint endpoint;
    protected final ConsulConfiguration configuration;
    protected final String key;
    protected final AtomicReference<BigInteger> index;

    private final Function<Consul, C> clientSupplier;
    private Runnable watcher;

    protected AbstractConsulConsumer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration, Processor processor, Function<Consul, C> clientSupplier) {
        super(endpoint, processor);

        this.endpoint = endpoint;
        this.configuration = configuration;
        this.key = ObjectHelper.notNull(configuration.getKey(), ConsulConstants.CONSUL_KEY);
        this.index = new AtomicReference<>(BigInteger.valueOf(configuration.getFirstIndex()));
        this.clientSupplier = clientSupplier;
        this.watcher = null;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        watcher = createWatcher(clientSupplier.apply(endpoint.getConsul()));
        watcher.run();
    }

    @Override
    protected void doStop() throws Exception {
        watcher = null;

        super.doStop();
    }

    // *************************************************************************
    //
    // *************************************************************************

    protected abstract Runnable createWatcher(C client) throws Exception;

    // *************************************************************************
    // Handlers
    // *************************************************************************

    protected abstract class AbstractWatcher implements Runnable {
        protected final C client;

        public AbstractWatcher(C client) {
            this.client = client;
        }

        protected void onError(Throwable throwable) {
            if (isRunAllowed()) {
                getExceptionHandler().handleException("Error watching for event " + key, throwable);
            }
        }

        protected void setIndex(BigInteger responseIndex) {
            index.set(responseIndex);
        }

        @Override
        public void run() {
            if (isRunAllowed()) {
                watch();
            }
        }

        protected abstract void watch();
    }
}
