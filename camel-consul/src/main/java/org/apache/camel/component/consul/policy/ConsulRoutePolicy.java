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
package org.apache.camel.component.consul.policy;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Optional;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.SessionClient;
import com.orbitz.consul.model.session.ImmutableSession;
import org.apache.camel.Exchange;
import org.apache.camel.NonManagedService;
import org.apache.camel.Route;
import org.apache.camel.support.RoutePolicySupport;

public class ConsulRoutePolicy extends RoutePolicySupport implements NonManagedService {
    private final Consul consul;
    private final SessionClient sessionClient;
    private final KeyValueClient keyValueClient;
    private final AtomicBoolean leader;
    private final AtomicBoolean shouldProcessExchanges ;
    private final Set<Route> suspendedRoutes;
    private final Lock lock;

    private String serviceName;
    private String servicePath;
    private long ttl;
    private ScheduledExecutorService executorService;
    private boolean shouldStopConsumer;

    private String sessionId;

    public ConsulRoutePolicy(Consul consul) {
        this.consul = consul;
        this.sessionClient = consul.sessionClient();
        this.keyValueClient = consul.keyValueClient();
        this.suspendedRoutes =  new CopyOnWriteArraySet<>();
        this.leader = new AtomicBoolean(false);
        this.shouldProcessExchanges = new AtomicBoolean();
        this.lock = new ReentrantLock();

        this.serviceName = null;
        this.servicePath = null;
        this.ttl = 60;
        this.executorService = null;
        this.shouldStopConsumer = true;

        this.sessionId = null;
    }

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {
        if (leader.get()) {
            if (shouldStopConsumer) {
                startConsumer(route);
            }
        } else {
            if (shouldStopConsumer) {
                stopConsumer(route);
            }

            exchange.setException( new IllegalStateException(
                "Consul based route policy prohibits processing exchanges, stopping route and failing the exchange")
            );
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (sessionId == null) {
            sessionId = sessionClient.createSession(
                ImmutableSession.builder()
                    .name(this.serviceName)
                    .ttl(this.ttl + "s")
                    .build()
                ).getId();
        }

        if (executorService == null) {
            executorService = Executors.newScheduledThreadPool(1);
        }

        executorService.scheduleAtFixedRate(this::poll, 0, ttl / 3, TimeUnit.SECONDS);
    }

    @Override
    protected void doStop() throws Exception {
        if (sessionId != null) {
            sessionClient.destroySession(sessionId);
            sessionId = null;
        }

        if (executorService != null) {
            executorService.shutdown();
            executorService.awaitTermination(ttl / 3, TimeUnit.SECONDS);
        }

        super.doStop();
    }

    // *************************************************************************
    //
    // *************************************************************************

    private void poll() {
        if (leader.get()) {
            Optional<String> sid = keyValueClient.getSession(servicePath);
            if (sid.isPresent() && Objects.equals(sessionId, sid.get())) {
                sessionClient.renewSession(sessionId);
            } else {
                leader.set(false);
            }
        }

        if (!leader.get()){
            if (keyValueClient.acquireLock(servicePath, sessionId)) {
                leader.set(true);
                startAllStoppedConsumers();
            }
        }
    }

    private void startConsumer(Route route) {
        try {
            lock.lock();
            if (suspendedRoutes.contains(route)) {
                startConsumer(route.getConsumer());
                suspendedRoutes.remove(route);
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    private void stopConsumer(Route route) {
        try {
            lock.lock();
            // check that we should still suspend once the lock is acquired
            if (!suspendedRoutes.contains(route) && !shouldProcessExchanges.get()) {
                stopConsumer(route.getConsumer());
                suspendedRoutes.add(route);
            }
        } catch (Exception e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    private void startAllStoppedConsumers() {
        try {
            lock.lock();
            if (!suspendedRoutes.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("{} have been stopped previously by policy, restarting.", suspendedRoutes.size());
                }
                for (Route suspended : suspendedRoutes) {
                    startConsumer(suspended.getConsumer());
                }
                suspendedRoutes.clear();
            }

        } catch (Exception e) {
            handleException(e);
        } finally {
            lock.unlock();
        }
    }

    // *************************************************************************
    // Getter/Setters
    // *************************************************************************

    public Consul getConsul() {
        return consul;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        this.servicePath = String.format("/service/%s/leader", serviceName);
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl > 10 ? ttl : 10;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public boolean isShouldStopConsumer() {
        return shouldStopConsumer;
    }

    public void setShouldStopConsumer(boolean shouldStopConsumer) {
        this.shouldStopConsumer = shouldStopConsumer;
    }
}
