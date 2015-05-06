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
package com.github.lburgazzoli.camel.component.chronicle;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 * Represents a Chronicle endpoint.
 */
@UriEndpoint(scheme = "chronicle", title = "Chronicle", syntax="chronicle:name", consumerClass = ChronicleConsumer.class, label = "Chronicle")
public class ChronicleEndpoint extends DefaultEndpoint {
    @UriPath @Metadata(required = "true")
    private String name;
    @UriParam(defaultValue = "10")
    private int option = 10;

    public ChronicleEndpoint() {
    }

    public ChronicleEndpoint(String uri, ChronicleComponent component) {
        super(uri, component);
    }

    public ChronicleEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public Producer createProducer() throws Exception {
        return new ChronicleProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new ChronicleConsumer(this, processor);
    }

    public boolean isSingleton() {
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public int getOption() {
        return option;
    }
}
