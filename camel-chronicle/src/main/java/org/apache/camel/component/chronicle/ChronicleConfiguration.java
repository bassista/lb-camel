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
package org.apache.camel.component.chronicle;

import net.openhft.chronicle.wire.WireType;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriParams;

@UriParams
public class ChronicleConfiguration implements CamelContextAware {
    @UriParam(javaType = "java.lang.String")
    @Metadata(required = "true")
    private String[] addresses;

    @UriParam(defaultValue = "BINARY", javaType = "java.lang.String")
    private WireType wireType = WireType.BINARY;

    @UriParam(defaultValue = "true")
    private boolean subscribeMapEvents = true;

    @UriParam(javaType = "java.lang.String")
    private String[] filteredMapEvents;

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

    public String[] getAddresses() {
        return addresses;
    }

    /**
     * Description
     */
    public void setAddresses(String addresses) {
        setAddresses(addresses.split(","));
    }

    /**
     * Description
     */
    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    public WireType getWireType() {
        return wireType;
    }

    /**
     * Description
     */
    public void setWireType(String wireType) {
        setWireType(WireType.valueOf(wireType));
    }

    /**
     * Description
     */
    public void setWireType(WireType wireType) {
        this.wireType = wireType;
    }
}
