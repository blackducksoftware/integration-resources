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
package com.synopsys.integration.activity.report.codequality

import com.synopsys.integration.activity.report.Report
import com.synopsys.integration.activity.util.KnownUsers
import com.synopsys.integration.activity.util.Repositories
import groovy.json.JsonSlurper
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Component
class CodeQuality extends Report {
    @Autowired
    KnownUsers knownUsers

    @Autowired
    Repositories repositories

    String computeContents(int daysToInclude) {
        def jsonSlurper = new JsonSlurper()
        def client = HttpClientBuilder.create().build()

        DateTimeFormatter iso8601 = DateTimeFormatter.ofPattern('yyyy-MM-dd\'T\'HH:mm:ssX')
        DateTimeFormatter easternClock = DateTimeFormatter.ofPattern('yyyy-MM-dd hh:mm:ss a')
        Instant now = Instant.now()
        Instant before = now.minus(daysToInclude, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
        def since = before.atZone(ZoneOffset.UTC).format(iso8601)
        def sinceEastern = before.atZone(TimeZone.getTimeZone("America/New_York").toZoneId()).format(easternClock)

        def simpleNameToStatus = new HashMap<String, RepoStatus>()

        repositories.repositories.each { repoName ->
            String simpleName = repositories.getSimpleName(repoName)

            def done = false
            RepoStatus repoStatus = new RepoStatus(repoName)

            while (!done) {
                // Get and process builds
                def buildsResponse = getBuildsForRepo(client.&execute, repoName, repoStatus.totalBuildsForRepo)
                def jsonbuildsResponse = jsonSlurper.parse(buildsResponse.getEntity().getContent())

                if (jsonbuildsResponse.'@type' == 'error' || jsonbuildsResponse.'@pagination'.count == 0) {
                    done = true
                } else {
                    for (build in jsonbuildsResponse.builds) {
                        done = (build.started_at != null && Instant.parse(build.started_at).isBefore(before))
                        if (done) break
                        if (build.branch?.name != 'master' || build.state == 'cancelled') continue

                        if (!repoStatus.latestBuildStatus) {
                            repoStatus.latestBuildStatus = build.state
                        }

                        if (build.state == 'passed') {
                            repoStatus.totalBuildsPassed++
                        }

                        repoStatus.totalBuildsForRepo++
                    }
                }
            }
            if (repoStatus.totalBuildsForRepo > 0) {
                // Get Sonar quality gate
                String projectName = repositories.getSonarName(repoName)
                def projectKeyResponse = getSonarProjectKey(client.&execute, projectName)
                def jsonProjectKeyResponse = jsonSlurper.parse(projectKeyResponse.getEntity().getContent())

                if (jsonProjectKeyResponse.components != null && jsonProjectKeyResponse.components.size() > 0) {
                    def projectKey = jsonProjectKeyResponse.components[0].key
                    def qualityGateResponse = getSonarQualityGateStatus(client.&execute, projectKey)
                    def jsonQualityGateResponse = jsonSlurper.parse(qualityGateResponse.getEntity().getContent())
                    if (jsonQualityGateResponse?.projectStatus?.status) {
                        repoStatus.sonarStatus = parseStatus(jsonQualityGateResponse.projectStatus.status)
                    }
                }

                simpleNameToStatus.put(simpleName, repoStatus)
            }
        }

        StringBuilder content = new StringBuilder()

        content.append '<html>\n'
        content.append("""<head>
\t<style type="text/css">
\t\tth {
\t\t\tfont-family: courier;
\t\t\tsize: 10pt;
\t\t\tpadding: 0 15px 0 15px;
\t\t}
\t\ttd,span {
\t\t\tfont-family: courier;
\t\t\tsize: 10pt;
\t\t\tpadding: 0 15px 0 15px;
\t\t}
\t</style>
</head>""")
        content.append '<body>\n'
        content.append "<span>In the last ${daysToInclude} days</span><br />\n"
        content.append '<br />\n'
        content.append '<table>\n'
        simpleNameToStatus.toSorted { it.key }.each { simpleName, repoStatus ->
            content.append "<tr><th colspan=2 style='text-align: left;'>${simpleName}</th></tr>\n"
            content.append "<tr><td style='text-align: right;'>Quality gate:</td><td>${repoStatus.sonarStatus}</td></tr>\n"
            content.append "<tr><td style='text-align: right;'>Latest build:</td><td>${repoStatus.latestBuildStatus.toUpperCase()}</td></tr>\n"
            content.append "<tr><td style='text-align: right;'>History:</td><td>${repoStatus.getPassPercentage()}% of ${repoStatus.totalBuildsForRepo} builds have passed</td></tr>\n"
            content.append '<tr><td colspan=2>&nbsp;</td></tr>\n'
        }
        content.append '</table>\n'

        content.append '<table>\n'
        simpleNameToStatus.toSorted { it.key }.each { simpleName, repoStatus ->
            content.append '<tr>\n'
            content.append "<td>${simpleName}</td><td><a href=${repoStatus.gitHubUrl}>${repoStatus.gitHubUrl}</a></td>\n"
            content.append '</tr>\n'
        }
        content.append '</table>\n'

        content.append '<br />\n'
        content.append '<br />\n'

        content.append "<span>Since: ${since} (${sinceEastern})</span><br />\n"

        content.append '</body></html>\n'

        content.toString()
    }

    private HttpResponse getBuildsForRepo(Closure clientExecutor, String repo, int offset) {
        def get = new HttpGet("https://api.travis-ci.org/repo/${URLEncoder.encode(repo)}/builds?offset=${offset}")
        get.addHeader('Travis-API-Version', '3')
        get.addHeader('Authorization', "token ${System.getenv('TRAVIS_AUTH_TOKEN')}")

        clientExecutor.call(get)
    }

    private HttpResponse getSonarProjectKey(Closure clientExecutor, String projectName) {
        def get = new HttpGet("https://sonarcloud.io/api/components/search?qualifiers=TRK&organization=black-duck-software&q=${URLEncoder.encode(projectName)}")

        clientExecutor.call(get)
    }

    private HttpResponse getSonarQualityGateStatus(Closure clientExecutor, String projectKey) {
        def get = new HttpGet("https://sonarcloud.io/api/qualitygates/project_status?projectKey=${URLEncoder.encode(projectKey)}");

        clientExecutor.call(get)
    }

    private String parseStatus(String status) {
        if ('OK'.equals(status)) {
            return 'PASSING'
        }
        if ('ERROR'.equals(status)) {
            return 'FAILING'
        }
        return status
    }

}
