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

import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.wire.WireType;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.chronicle.ChronicleTestSupport;
import org.junit.Test;

public class ChronicleEngineTest extends ChronicleTestSupport {

    @Test
    public void doTest() {
        VanillaAssetTree tree = new VanillaAssetTree()
            .forRemoteAccess(
                "localhost:9876",
                WireType.TEXT,
                t -> { throw new RuntimeCamelException(t); }
            );
    }

    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("chronicle://engine/my/path?address=localhost:9876")
                    .to("log:org.apache.camel.component.chronicle.engine?level=INFO&showAll=true");
            }
        };
    }
}
