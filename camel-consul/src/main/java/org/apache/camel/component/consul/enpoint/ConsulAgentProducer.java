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
package org.apache.camel.component.consul.enpoint;

import java.util.Map;

import com.orbitz.consul.AgentClient;
import org.apache.camel.component.consul.AbstractConsulEndpoint;
import org.apache.camel.component.consul.AbstractConsulProducer;
import org.apache.camel.component.consul.ConsulConfiguration;
import org.apache.camel.component.consul.MessageProcessor;

public class ConsulAgentProducer extends AbstractConsulProducer<AgentClient> {
    ConsulAgentProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint, configuration, c -> c.agentClient());
    }

    @Override
    protected void bindActionProcessors(Map<String, MessageProcessor> processors) {
        processors.put(ConsulAgentActions.CHECKS, wrap(c -> c.getChecks()));
        processors.put(ConsulAgentActions.SERVICES, wrap(c -> c.getServices()));
        processors.put(ConsulAgentActions.MEMBERS, wrap(c -> c.getMembers()));
        processors.put(ConsulAgentActions.AGENT, wrap(c -> c.getAgent()));
    }

    // *************************************************************************
    //
    // *************************************************************************

    /*
    private void list(Message message) throws Exception {
        setBodyAndResult(
            message,
            getClient().listEvents(
                getKey(message),
                getOption(message, QueryOptions.BLANK, QueryOptions.class)
            )
        );
    }
    */
}
