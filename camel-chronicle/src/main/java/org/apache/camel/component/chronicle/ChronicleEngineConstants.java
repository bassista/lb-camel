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

public interface ChronicleEngineConstants {
    String ASSET_NAME = "ChronicleEngineAssetName";
    String PATH = "ChronicleEnginePath";
    String TOPIC = "ChronicleEngineTopic";
    String TOPOLOGICAL_EVENT_NAME = "ChronicleEngineTopologicalEventName";
    String TOPOLOGICAL_EVENT_FULL_NAME = "ChronicleEngineTopologicalEventFullName";
    String TOPOLOGICAL_EVENT_ADDED = "ChronicleEngineTopologicalEventAdded";
    String MAP_EVENT_TYPE = "ChronicleEngineMapEventType";
    String RESULT = "ChronicleEngineResult";
    String KEY = "ChronicleEngineKey";
    String VALUE = "ChronicleEngineValue";
    String DEFAULT_VALUE = "ChronicleEngineDefaultValue";
    String OLD_VALUE = "ChronicleEngineOldValue";
    String ACTION = "ChronicleEngineAction";
    String ACTION_PUBLISH = "PUBLISH";
    String ACTION_PUT = "PUT";
    String ACTION_GET_AND_PUT = "GET_AND_PUT";
    String ACTION_PUT_ALL = "PUT_ALL";
    String ACTION_PUT_IF_ABSENT = "PUT_IF_ABSENT";
    String ACTION_GET = "GET";
    String ACTION_GET_AND_REMOVE = "GET_AND_REMOVE";
    String ACTION_GET_OR_DEFAULT = "GET_OR_DEFAULT";
    String ACTION_REMOVE = "REMOVE";
    String ACTION_IS_EMPTY = "IS_EMPTY";
    String ACTION_IS_SIZE = "SIZE";
}
