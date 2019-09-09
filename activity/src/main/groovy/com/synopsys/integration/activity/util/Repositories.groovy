/*
 * activity
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.activity.util

import org.springframework.stereotype.Component

import java.util.function.Function

@Component
class Repositories {
    private static final Function<String, String> SHORTEN_NAME = new Function<String, String>() {
        @Override
        String apply(String s) {
            return s.substring(s.lastIndexOf('/') + 1)
        }
    }

    private Map<String, String> repositoriesToSimpleNames = new HashMap<>()
    private Map<String, String> repositoriesToSonarOverride = new HashMap<>()

    Repositories() {
        addRepository('blackducksoftware/black-duck-radar', 'radar')
        addRepository('blackducksoftware/blackduck-alert', 'alert')
        addRepository('blackducksoftware/blackduck-artifactory', 'artifactory')
        addRepository('blackducksoftware/blackduck-common')
        addRepository('blackducksoftware/blackduck-common-api')
        addRepository('blackducksoftware/blackduck-docker-inspector', 'docker-inspector')
        addRepository('blackducksoftware/blackduck-jira', 'jira')
        addRepository('blackducksoftware/blackduck-nexus3', 'nexus3')
        addRepository('blackducksoftware/blackduck-nuget-inspector')
        addRepository('blackducksoftware/common-gradle-plugin')
        addRepository('blackducksoftware/coverity-common')
        addRepository('blackducksoftware/detect-for-tfs')
        addRepository('blackducksoftware/ducky-mod')
        addRepository('blackducksoftware/hub-nexus', 'nexus2')
        addRepository('blackducksoftware/hub-sonarqube', 'sonarqube')
        addRepository('blackducksoftware/hub-spdx')
        addRepository('blackducksoftware/int-jira-common', 'jira-common')
        addRepository('blackducksoftware/integration-bdio')
        addRepository('blackducksoftware/integration-common')
        addRepository('blackducksoftware/integration-nuget-inspector')
        addRepository('blackducksoftware/integration-pipeline-library')
        addRepository('blackducksoftware/integration-reporting')
        addRepository('blackducksoftware/integration-resources')
        addRepository('blackducksoftware/integration-rest')
        addRepository('blackducksoftware/phone-home-client')
        addRepository('blackducksoftware/polaris-common')
        addRepository('blackducksoftware/polaris-common-api')
        addRepository('blackducksoftware/swagger-hub')
        addRepository('blackducksoftware/synopsys-coverity-azure-devops', 'coverity-azure')
        addRepository('blackducksoftware/synopsys-detect', 'detect')
        addRepository('jenkinsci/synopsys-detect-plugin', 'detect-jenkins')
        addRepository('jenkinsci/synopsys-coverity-plugin', 'coverity-jenkins')
        addRepository('synopsys-sig/synopsys-detect-scripts')

        addSonarOverride('jenkinsci/synopsys-detect-plugin', 'Synopsys Detect for Jenkins')
        addSonarOverride('jenkinsci/synopsys-coverity-plugin', 'Synopsys Coverity for Jenkins')
        addSonarOverride('blackducksoftware/blackduck-jira', 'Black Duck JIRA Plugin')
    }

    Set<String> getRepositories() {
        return repositoriesToSimpleNames.keySet()
    }

    String getSimpleName(String repository) {
        repositoriesToSimpleNames.get(repository)
    }

    String getSonarName(String repository) {
        repositoriesToSonarOverride.computeIfAbsent(repository, SHORTEN_NAME)
    }

    private void addRepository(String repository, String simpleName) {
        repositoriesToSimpleNames.put(repository, simpleName)
    }

    private void addRepository(String repository) {
        String shortName = SHORTEN_NAME.apply(repository)
        repositoriesToSimpleNames.put(repository, shortName)
    }

    private void addSonarOverride(String repository, String sonarOverride) {
        repositoriesToSonarOverride.put(repository, sonarOverride)
    }

}
