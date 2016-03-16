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
package org.apache.camel.component.servicenow.auth;


import java.io.IOException;
import java.util.Base64;

import org.apache.camel.component.servicenow.ServiceNowConfiguration;
import org.apache.cxf.rs.security.oauth2.common.ClientAccessToken;

public final class AuthenticationUtil {
    private AuthenticationUtil() {
    }

    public static boolean isExpired(final ClientAccessToken token) {
        return System.currentTimeMillis() >= token.getIssuedAt() + token.getExpiresIn();
    }

    public static String authenticationString(final ClientAccessToken token) {
        return token.getTokenType() + " " + token.getTokenKey();
    }

    public static String authenticationString(final ServiceNowConfiguration configuration) throws IOException {
        String userAndPassword = configuration.getUserName() + ":" + configuration.getPassword();
        byte[] userAndPasswordBytes = userAndPassword.getBytes("UTF-8");
        return "Basic " + Base64.getEncoder().encodeToString(userAndPasswordBytes);
    }
}
