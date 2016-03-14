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
package org.apache.camel.component.teiid;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;

import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.util.ObjectHelper;

/**
 * Represents the component that manages {@link AbstractChronicleEndpoint}.
 */
public class ChronicleComponent extends UriEndpointComponent {
    
    public ChronicleComponent() {
        super(AbstractChronicleEndpoint.class);
    }

    public ChronicleComponent(CamelContext context) {
        super(context, AbstractChronicleEndpoint.class);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        String name = ObjectHelper.before(remaining, "/");

        switch(ChronicleTypes.fromName(name)) {
        case ENGINE:
            ChronicleEngineConfiguration configuration = new ChronicleEngineConfiguration(getCamelContext());
            setProperties(configuration, parameters);
            return new ChronicleEngineEnpoint(uri, this, remaining, configuration);
        default:
            throw new IllegalArgumentException("Unknown type: " + remaining);
        }
    }
}