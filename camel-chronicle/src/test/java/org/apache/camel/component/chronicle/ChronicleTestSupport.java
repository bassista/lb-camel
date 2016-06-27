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

import net.openhft.chronicle.engine.server.ServerEndpoint;
import net.openhft.chronicle.engine.tree.VanillaAssetTree;
import net.openhft.chronicle.network.TCPRegistry;
import net.openhft.chronicle.network.connection.TcpChannelHub;
import net.openhft.chronicle.wire.WireType;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TestName;

@Ignore
public class ChronicleTestSupport extends CamelTestSupport {
    @Rule
    public final TestName name = new TestName();

    private VanillaAssetTree serverAssetTree;
    private ServerEndpoint serverEndpoint;
    private WireType wireType;

    public TestName getName() {
        return name;
    }

    public VanillaAssetTree getServerAssetTree() {
        return serverAssetTree;
    }

    public ServerEndpoint getServerEndpoint() {
        return serverEndpoint;
    }

    public WireType getWireType() {
        return wireType;
    }

    // **************************
    // set-up / tear-down
    // **************************

    @Override
    protected void doPreSetup() throws Exception {
        wireType = WireType.TEXT;
        serverAssetTree = new VanillaAssetTree().forTesting();
        serverEndpoint = new ServerEndpoint("localhost:9876", serverAssetTree);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        serverAssetTree.close();
        serverEndpoint.close();

        TcpChannelHub.closeAllHubs();
        TCPRegistry.reset();
    }
}
