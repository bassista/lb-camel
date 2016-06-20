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

package org.apache.camel.component.chronicle.engine;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;

@UriParams
public class ChronicleEngineConfiguration implements CamelContextAware {

    @UriParam
    @Metadata(required = "true")
    private String hostPortDescription;

    @UriParam
    @Metadata(required = "true")
    private String wireType;

    @UriParam
    private boolean subscribeMapEvents;

    @UriParam
    private String filteredMapEvents;

    @UriParam
    private boolean subscribeTopologicalEvents;

    @UriParam
    private boolean subscribeTopicEvents;

    private CamelContext camelContext;

    // ****************************
    //
    // ****************************

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    // ****************************
    // CLIENT OPTIONS
    // ****************************

    public String getHostPortDescription() {
        return hostPortDescription;
    }

    /**
     * Description
     */
    public void setHostPortDescription(String hostPortDescription) {
        this.hostPortDescription = hostPortDescription;
    }

    public String getWireType() {
        return wireType;
    }

    /**
     * Description
     */
    public void setWireType(String wireType) {
        this.wireType = wireType;
    }

    // ****************************
    // MAP EVENTS OPTIONS
    // ****************************

    public boolean isSubscribeMapEvents() {
        return subscribeMapEvents;
    }

    /**
     * Description
     */
    public void setSubscribeMapEvents(boolean subscribeMapEvents) {
        this.subscribeMapEvents = subscribeMapEvents;
    }

    public String getFilteredMapEvents() {
        return filteredMapEvents;
    }

    /**
     * Description
     */
    public void setFilteredMapEvents(String filteredMapEvents) {
        this.filteredMapEvents = filteredMapEvents;
    }

    // ****************************
    // TOPOLOGICAL EVENTS OPTIONS
    // ****************************

    public boolean isSubscribeTopologicalEvents() {
        return subscribeTopologicalEvents;
    }

    /**
     * Description
     */
    public void setSubscribeTopologicalEvents(boolean subscribeTopologicalEvents) {
        this.subscribeTopologicalEvents = subscribeTopologicalEvents;
    }

    // ****************************
    // TOPIC EVENTS OPTIONS
    // ****************************

    public boolean isSubscribeTopicEvents() {
        return subscribeTopicEvents;
    }

    /**
     * Description
     */
    public void setSubscribeTopicEvents(boolean subscribeTopicEvents) {
        this.subscribeTopicEvents = subscribeTopicEvents;
    }
}
