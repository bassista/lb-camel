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

import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.map.InsertedEvent;
import net.openhft.chronicle.engine.map.RemovedEvent;
import net.openhft.chronicle.engine.map.UpdatedEvent;

public final class ChronicleEngineHelper {

    private ChronicleEngineHelper() {
    }

    public static String toMapEventTypeName(MapEvent event) {
        if (event instanceof InsertedEvent) {
            return ChronicleEngineConstants.MAP_EVENT_TYPE_INSERT;
        }
        if (event instanceof UpdatedEvent) {
            return ChronicleEngineConstants.MAP_EVENT_TYPE_UPDATE;
        }
        if (event instanceof RemovedEvent) {
            return ChronicleEngineConstants.MAP_EVENT_TYPE_REMOVE;
        }

        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }

    public static Class<? extends MapEvent> fromMapEventTypeName(String event) {
        switch (event.toUpperCase()) {
        case ChronicleEngineConstants.MAP_EVENT_TYPE_INSERT:
            return InsertedEvent.class;
        case ChronicleEngineConstants.MAP_EVENT_TYPE_UPDATE:
            return UpdatedEvent.class;
        case ChronicleEngineConstants.MAP_EVENT_TYPE_REMOVE:
            return RemovedEvent.class;
        default:
            throw new IllegalArgumentException("Unknown event type: " + event);
        }
    }
}
