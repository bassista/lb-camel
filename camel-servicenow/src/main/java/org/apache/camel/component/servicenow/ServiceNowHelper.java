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

package org.apache.camel.component.servicenow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;

public final class ServiceNowHelper {

    private ServiceNowHelper() {
    }

    public static Object extractResult(ObjectMapper mapper, Class<?> model, JsonNode answer) throws Exception {
        Object result = null;

        if (answer != null) {
            JsonNode node = answer.get("result");
            if (node != null) {
                if (model == null) {
                    result = mapper.writeValueAsString(node);
                } else {
                    if (node.isArray()) {
                        if (model.isInstance(Map.class)) {
                            result = mapper.treeToValue(node, List.class);
                        } else {
                            List<Object> list = new ArrayList<>(node.size());
                            for (int i = 0; i < node.size(); i++) {
                                list.add(mapper.treeToValue(node.get(i), model));
                            }

                            result = list;
                        }
                    } else {
                        result = mapper.treeToValue(node, model);
                    }
                }
            }
        }

        return result;
    }

    public static void validateBody(Message message, Class<?> model) {
        validateBody(message.getBody(), model);
    }

    public static void validateBody(Object body, Class<?> model) {
        ObjectHelper.notNull(body, "body");

        if (!body.getClass().isAssignableFrom(model)) {
            throw new IllegalArgumentException(
                "Body is not compatible with model (body=" + body.getClass() + ", model=" + model);
        }
    }
}
