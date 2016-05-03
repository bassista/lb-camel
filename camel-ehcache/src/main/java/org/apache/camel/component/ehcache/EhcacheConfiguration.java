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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;

@UriParams
public class EhcacheConfiguration {
    @UriParam(javaType = "java.lang.String", defaultValue = "java.lang.Object")
    private Class<?> keyType = Object.class;
    @UriParam(javaType = "java.lang.String", defaultValue = "java.lang.Object")
    private Class<?> valueType = Object.class;

    @UriParam(label = "advanced,resources")
    private ResourcePools resourcePools;

    @UriParam(label = "advanced")
    private CacheManager cacheManager;

    @UriParam(label = "advanced")
    private CacheConfiguration<?,?> cacheConfiguration;

    @UriParam(prefix = "cache.", multiValue = true, javaType = "java.lang.String")
    private Map<String, CacheConfiguration> caches;
    @UriParam(prefix = "pool.", multiValue = true, javaType = "java.lang.String")
    private Map<String, ResourcePools> pools;



    public Class<?> getKeyType() {
        return keyType;
    }

    public void setKeyType(Class<?> keyType) {
        this.keyType = keyType;
    }

    public Class<?> getValueType() {
        return valueType;
    }

    public void setValueType(Class<?> valueType) {
        this.valueType = valueType;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setCacheConfiguration(CacheConfiguration<?, ?> cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    public ResourcePools getResourcePools() {
        return resourcePools;
    }

    public void setResourcePools(ResourcePools resourcePools) {
        this.resourcePools = resourcePools;
    }

    public void addCacheConfiguration(String cacheName, CacheConfiguration<?,?> builder) {
        if (caches == null) {
            caches = new HashMap<>();
        }

        caches.put(cacheName, builder);

    }

    public CacheConfigurationBuilder getCacheConfiguration() {
        return null;
    }
}
