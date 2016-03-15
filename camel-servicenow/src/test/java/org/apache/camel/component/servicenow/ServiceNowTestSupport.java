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
package org.apache.camel.component.servicenow;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ServiceNowTestSupport extends CamelTestSupport {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ServiceNowTestSupport.class);
    protected static final String TEST_OPTIONS_PROPERTIES = "/test-options.properties";
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected CamelContext createCamelContext() throws Exception {
        return super.createCamelContext();
    }

    protected static class KVBuilder {
        private final Map<String, Object> headers;

        public KVBuilder() {
            this(new HashMap<>());
        }

        private KVBuilder(Map<String, Object> headers) {
            this.headers = headers;
        }

        public KVBuilder on(Map<String, Object> headers) {
            return new KVBuilder(headers);
        }

        public KVBuilder put(String key, Object val) {
            headers.put(key, val);
            return this;
        }

        public Map<String, Object> build() {
            return Collections.unmodifiableMap(this.headers);
        }
    }
}
