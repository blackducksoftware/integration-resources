/*
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
package com.synopsys.integration.activity.util

import groovy.json.JsonSlurper

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.utils.URIBuilder
import org.springframework.stereotype.Component

@Component
public class GitHubRepositories {
    private final Map<String, String> simpleNameToSonarOverride
    private final Map<String, String> simpleNameToOverride
    private final List<GitHubRepository> gitHubRepositories

    public GitHubRepositories() {
        this.gitHubRepositories = new ArrayList<>()
        this.simpleNameToOverride = new HashMap<>()
        this.simpleNameToSonarOverride = new HashMap<>()

        simpleNameToOverride.put('black-duck-radar', 'radar')
        simpleNameToOverride.put('blackduck-alert', 'alert')
        simpleNameToOverride.put('blackduck-artifactory', 'artifactory')
        simpleNameToOverride.put('blackduck-docker-inspector', 'docker-inspector')
        simpleNameToOverride.put('blackduck-jira', 'jira')
        simpleNameToOverride.put('blackduck-nexus3', 'nexus3')
        simpleNameToOverride.put('hub-nexus', 'nexus2')
        simpleNameToOverride.put('hub-sonarqube', 'sonarqube')
        simpleNameToOverride.put('int-jira-common', 'jira-common')
        simpleNameToOverride.put('synopsys-coverity-azure-devops', 'coverity-azure')
        simpleNameToOverride.put('synopsys-detect', 'detect')
        simpleNameToOverride.put('synopsys-detect-plugin', 'detect-jenkins')
        simpleNameToOverride.put('synopsys-coverity-plugin', 'coverity-jenkins')
        simpleNameToOverride.put('synopsys-polaris-plugin', 'polaris-jenkins')

        simpleNameToSonarOverride.put('synopsys-detect-plugin', 'Synopsys Detect for Jenkins')
        simpleNameToSonarOverride.put('synopsys-coverity-plugin', 'Synopsys Coverity for Jenkins')
        simpleNameToSonarOverride.put('blackduck-jira', 'Black Duck JIRA Plugin')
    }

    public List<GitHubRepository> getGitHubRepositories(Closure<HttpResponse> clientExecutor, JsonSlurper jsonSlurper) throws URISyntaxException, IOException {
        if (gitHubRepositories.isEmpty()) {
            boolean done = false
            for (int page = 1; !done; page++) {
                final URI uri = new URIBuilder()
                        .setScheme('https')
                        .setHost('api.github.com')
                        .setPath('/search/repositories=')
                        .addParameter('q','topic:integration-team+org:synopsys-sig+org:blackducksoftware+org:jenkinsci')
                        .addParameter('page', Integer.toString(page))
                        .build()

                final HttpGet get = new HttpGet(uri)

                final HttpResponse httpResponse = clientExecutor.call(get)

                def jsonResponse = jsonSlurper.parse(httpResponse.getEntity().getContent())
                if (jsonResponse.items) {
                    jsonResponse.items.each {
                        if (it.archived == false) {
                            final String simpleName = simpleNameToOverride.getOrDefault(it.name, it.name)
                            final String sonarName = simpleNameToSonarOverride.getOrDefault(it.name, it.name)
                            gitHubRepositories.add(new GitHubRepository(simpleName, it.full_name, sonarName))
                        }
                    }
                } else {
                    done = true
                }
            }
        }

        return gitHubRepositories
    }

}
