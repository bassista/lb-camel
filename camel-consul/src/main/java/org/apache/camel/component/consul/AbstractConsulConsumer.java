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

import com.orbitz.consul.Consul;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.ObjectHelper;

/**
 * @author lburgazzoli
 */
public abstract class AbstractConsulConsumer<T> extends DefaultConsumer {
    protected final AbstractConsulEndpoint endpoint;
    protected final ConsulConfiguration configuration;
    protected final String key;
    protected final AtomicReference<BigInteger> index;

    private T client;
    private Runnable watcher;

    protected AbstractConsulConsumer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration, Processor processor) {
        super(endpoint, processor);

        this.endpoint = endpoint;
        this.configuration = configuration;
        this.key = ObjectHelper.notNull(configuration.getKey(), ConsulConstants.CONSUL_KEY);
        this.index = new AtomicReference<>(BigInteger.valueOf(configuration.getFirstIndex()));
        this.client = null;
        this.watcher = null;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        client = createClient(endpoint.getConsul());
        watcher = createWatcher(client);

        watcher.run();
    }

    @Override
    protected void doStop() throws Exception {
        client = null;
        watcher = null;

        super.doStop();
    }

    // *************************************************************************
    // Details
    // *************************************************************************

    protected abstract T createClient(Consul consul) throws Exception;
    protected abstract Runnable createWatcher(T client) throws Exception;

    // *************************************************************************
    // Handlers
    // *************************************************************************

    protected abstract class AbstractWatcher implements Runnable {
        protected final T client;

        public AbstractWatcher(T client) {
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
