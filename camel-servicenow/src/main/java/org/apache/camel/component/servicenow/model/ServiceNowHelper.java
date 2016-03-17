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

package org.apache.camel.component.servicenow.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ServiceNowHelper {

    protected static Object extractResult(ObjectMapper mapper, Class<?> model, JsonNode answer) throws Exception {
        Object result = null;

        if (answer != null) {
            JsonNode node = answer.get("result");
            if (node != null) {
                if (model == null) {
                    result = mapper.writeValueAsString(node);
                } else {
                    if (node.isArray()) {
                        List<Object> list = new ArrayList<>(node.size());
                        for (int i = 0; i < node.size(); i++) {
                            list.add(mapper.treeToValue(node.get(i), model));
                        }

                        result = list;
                    } else {
                        result = mapper.treeToValue(node, model);
                    }
                }
            }
        }

        return result;
    }
}
