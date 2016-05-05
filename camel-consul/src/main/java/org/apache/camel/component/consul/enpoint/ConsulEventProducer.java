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
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.common.ExchangeProcessor;
import org.apache.camel.component.consul.AbstractConsulEndpoint;
import org.apache.camel.component.consul.AbstractConsulProducer;
import org.apache.camel.component.consul.ConsulConfiguration;

public class ConsulEventProducer extends AbstractConsulProducer<EventClient> {
    ConsulEventProducer(AbstractConsulEndpoint endpoint, ConsulConfiguration configuration) {
        super(endpoint, configuration, c -> c.eventClient());
    }

    @ExchangeProcessor(ConsulEventActions.FIRE)
    protected void fire(Exchange exchange) throws Exception {
        Message message = getResultMessage(exchange);
        setBodyAndResult(
            message,
            getClient().fireEvent(
                getMandatoryKey(message),
                getOption(message, EventOptions.BLANK, EventOptions.class),
                message.getBody(String.class)
            )
        );
    }

    @ExchangeProcessor(ConsulEventActions.LIST)
    protected void list(Exchange exchange) throws Exception {
        Message message = getResultMessage(exchange);
        setBodyAndResult(
            message,
            getClient().listEvents(
                getKey(message),
                getOption(message, QueryOptions.BLANK, QueryOptions.class)
            )
        );
    }
}
