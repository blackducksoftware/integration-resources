/**
 * activity
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.activity.util;

public class GitHubRepository {
    private final String simpleName;
    private final String fullName;
    private final String sonarName;

    public GitHubRepository(final String simpleName, final String fullName, final String sonarName) {
        this.simpleName = simpleName;
        this.fullName = fullName;
        this.sonarName = sonarName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSonarName() {
        return sonarName;
    }
}
