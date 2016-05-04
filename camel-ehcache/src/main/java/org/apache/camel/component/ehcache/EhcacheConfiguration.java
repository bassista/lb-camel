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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;
import org.apache.camel.util.EndpointHelper;
import org.apache.camel.util.IntrospectionSupport;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;

import static org.apache.camel.util.ResourceHelper.resolveMandatoryResourceAsUrl;

@UriParams
public class EhcacheConfiguration {
    public static final String PREFIX_CACHE = "cache.";
    public static final String PREFIX_POOL = "pool.";

    private final CamelContext context;

    @UriParam
    private String configUri;

    @UriParam(defaultValue = "true")
    private boolean permitCacheCreation = true;

    @UriParam
    private CacheManager cacheManager;
    @UriParam
    private CacheConfiguration<?,?> defaultCacheConfiguration;
    @UriParam
    private ResourcePools defaultCacheResourcePools;

    @UriParam(prefix = PREFIX_CACHE, multiValue = true, javaType = "java.lang.String")
    private Map<String, CacheConfiguration> cacheConfigurations;
    @UriParam(prefix = PREFIX_POOL, multiValue = true, javaType = "java.lang.String")
    private Map<String, ResourcePools> cacheResourcePools;


    EhcacheConfiguration(CamelContext context) {
        this.context = context;
    }

    public String getConfigUri() {
        return configUri;
    }

    public void setConfigUri(String configUri) {
        this.configUri = configUri;
    }

    public boolean isPermitCacheCreation() {
        return permitCacheCreation;
    }

    public void setPermitCacheCreation(boolean permitCacheCreation) {
        this.permitCacheCreation = permitCacheCreation;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // ****************************
    // Cache Configuration
    // ****************************

    public void setDefaultCacheConfiguration(CacheConfiguration<?, ?> defaultCacheConfiguration) {
        this.defaultCacheConfiguration = defaultCacheConfiguration;
    }

    public CacheConfiguration<?,?> getDefaultCacheConfiguration() {
        return defaultCacheConfiguration;
    }

    public void addCacheConfiguration(String cacheName, CacheConfiguration cacheConfiguration) {
        if (cacheConfigurations == null) {
            cacheConfigurations = new HashMap<>();
        }

        cacheConfigurations.put(cacheName, cacheConfiguration);
    }

    public void addCacheConfigurationFromParameters(Map<String, Object> parameters) {
        Map<String, Object> models = IntrospectionSupport.extractProperties(parameters, PREFIX_CACHE);
        for (Map.Entry<String, Object> entry : models.entrySet()) {
            addCacheConfiguration(
                entry.getKey(),
                EndpointHelper.resolveParameter(
                    context,
                    (String)entry.getValue(),
                    CacheConfiguration.class
                )
            );
        }
    }

    public CacheConfiguration getCacheConfiguration(String cacheName) {
        return cacheConfigurations != null
            ? cacheConfigurations.getOrDefault(cacheName, defaultCacheConfiguration)
            : defaultCacheConfiguration;
    }

    // ****************************
    // Cache Resource Pools
    // ****************************

    public ResourcePools getDefaultCacheResourcePools() {
        return defaultCacheResourcePools;
    }

    public void setDefaultCacheResourcePools(ResourcePools defaultCacheResourcePools) {
        this.defaultCacheResourcePools = defaultCacheResourcePools;
    }

    public void addResourcePools(String cacheName, ResourcePools resourcePools) {
        if (cacheResourcePools == null) {
            cacheResourcePools = new HashMap<>();
        }

        cacheResourcePools.put(cacheName, resourcePools);
    }

    public void addResourcePoolsFromParameters(Map<String, Object> parameters) {
        Map<String, Object> models = IntrospectionSupport.extractProperties(parameters, PREFIX_POOL);
        for (Map.Entry<String, Object> entry : models.entrySet()) {
            addResourcePools(
                entry.getKey(),
                EndpointHelper.resolveParameter(
                    context,
                    (String)entry.getValue(),
                    ResourcePools.class
                )
            );
        }
    }

    public ResourcePools getResourcePools(String cacheName) {
        return cacheResourcePools != null
            ? cacheResourcePools.getOrDefault(cacheName, defaultCacheResourcePools)
            : defaultCacheResourcePools;
    }


    // ****************************
    // Helpers
    // ****************************

    public CacheManager createCacheManager() throws IOException {
        CacheManager manager;

        if (cacheManager != null) {
            manager = cacheManager;
        } else if (configUri != null) {
            XmlConfiguration config = new XmlConfiguration(
                resolveMandatoryResourceAsUrl(context.getClassResolver(), configUri)
            );

            manager = CacheManagerBuilder.newCacheManager(config);
        } else {
            CacheManagerBuilder builder = CacheManagerBuilder.newCacheManagerBuilder();
            if (cacheConfigurations != null) {
                cacheConfigurations.forEach(builder::withCache);
            }

            manager = builder.build();
        }

        return manager;
    }
}
