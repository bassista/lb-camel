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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.ObjectHelper;


@UriEndpoint(scheme = "chronicle", title = "Chronicle", syntax="chronicle:engine/path", consumerClass = ChronicleEngineConsumer.class, label = "Chronicle")
class ChronicleEngineEnpoint extends AbstractChronicleEndpoint<ChronicleEngineConfiguration> {
    @UriPath(description = "The data path")
    @Metadata(required = "true")
    private final String path;

    ChronicleEngineEnpoint(String uri, ChronicleComponent component, String remaining, ChronicleEngineConfiguration configuration) throws Exception {
        super(uri, component, configuration);

        this.path = ObjectHelper.after(remaining, "/");
        if (ObjectHelper.isEmpty(this.path)) {
            throw new IllegalArgumentException("path can't be empty");
        }
    }

    @Override
    public Producer createProducer() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        return new ChronicleEngineConsumer(this, processor);
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }

    protected String getPath() {
        return this.path;
    }
}
