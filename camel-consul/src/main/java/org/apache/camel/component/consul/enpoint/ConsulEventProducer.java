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

import com.orbitz.consul.EventClient;
import com.orbitz.consul.option.EventOptions;
import com.orbitz.consul.option.QueryOptions;
import org.apache.camel.Message;
import org.apache.camel.component.consul.AbstractConsulEndpoint;
import org.apache.camel.component.consul.AbstractConsulProducer;
import org.apache.camel.component.consul.ConsulConfiguration;

public class ConsulEventProducer extends AbstractConsulProducer {
    private EventClient client;

    ConsulEventProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint, configuration);

        this.client = null;
    }

    @Override
    protected void doStart() throws Exception {
        client = endpoint.getConsul().eventClient();
    }

    @Override
    protected void doStop() throws Exception {
        client = null;
    }

    // *************************************************************************
    //
    // *************************************************************************

    @ConsulActionProcessor(ConsulEventActions.FIRE)
    private void fire(Message message) throws Exception {
        setBodyAndResult(
            message,
            client.fireEvent(
                getMandatoryKey(message),
                getOption(message, EventOptions.BLANK, EventOptions.class),
                message.getBody(String.class)
            )
        );
    }

    @ConsulActionProcessor(ConsulEventActions.LIST)
    private void list(Message message) throws Exception {
        setBodyAndResult(
            message,
            client.listEvents(
                getKey(message),
                getOption(message, QueryOptions.BLANK, QueryOptions.class)
            )
        );
    }
}
