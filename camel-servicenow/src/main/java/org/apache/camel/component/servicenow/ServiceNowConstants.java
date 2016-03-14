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

public interface ServiceNowConstants {
    String RESOURCE = "ServiceNowResource";
    String TABLE = "ServiceNowTable";
    String ACTION = "ServiceNowAction";

    String RESOURCE_TABLE = "table";
    String RESOURCE_AGGREGATE = "aggregate";
    String RESOURCE_IMPORT = "aggregate";

    String ACTION_RETRIEVE = "retrieve";
    String ACTION_CREATE = "create";
    String ACTION_MODIFY = "modify";
    String ACTION_DELETE = "modify";
    String ACTION_UPDATE = "update";

    String SYSPARM_ID = "ServiceNowSysId";
    String SYSPARM_QUERY = "ServiceNowQuery";
    String SYSPARM_DISPLAY_VALUE = "ServiceNowDisplayValue";
    String SYSPARM_EXCLUDE_REFERENCE_LINK = "ServiceNowExcludeReferenceLink";
    String SYSPARM_FIELDS = "ServiceNowFields";
    String SYSPARM_LIMIT = "ServiceNowLimit";
    String SYSPARM_VIEW = "ServiceNowView";
}
