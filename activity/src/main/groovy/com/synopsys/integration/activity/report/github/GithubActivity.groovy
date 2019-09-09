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
package com.synopsys.integration.activity.report.github

import com.synopsys.integration.activity.report.Report
import com.synopsys.integration.activity.util.KnownUsers
import com.synopsys.integration.activity.util.Repositories
import groovy.json.JsonSlurper
import org.apache.http.HttpMessage
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Component
class GithubActivity extends Report {
    @Autowired
    KnownUsers knownUsers

    @Autowired
    Repositories repositories

    String computeContents(int daysToInclude) {
        //TODO add releases during time range
        def jsonSlurper = new JsonSlurper()
        def client = HttpClientBuilder.create().build()

        DateTimeFormatter iso8601 = DateTimeFormatter.ofPattern('yyyy-MM-dd\'T\'HH:mm:ssX')
        DateTimeFormatter isoShort = DateTimeFormatter.ofPattern('MM/dd')
        DateTimeFormatter easternClock = DateTimeFormatter.ofPattern('yyyy-MM-dd hh:mm:ss a')
        Instant now = Instant.now()
        Instant before = now.minus(daysToInclude, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
        def since = before.atZone(ZoneOffset.UTC).format(iso8601)
        def until = now.atZone(ZoneOffset.UTC).format(iso8601)
        def shortSince = before.atZone(ZoneOffset.UTC).format(isoShort)
        def shortUntil = now.atZone(ZoneOffset.UTC).format(isoShort)
        def sinceEastern = before.atZone(TimeZone.getTimeZone("America/New_York").toZoneId()).format(easternClock)
        def untilEastern = now.atZone(TimeZone.getTimeZone("America/New_York").toZoneId()).format(easternClock)

        def repoToEmails = new HashMap<String, Set<String>>()

        repositories.repositories.each { repoName ->
            def done = false
            def page = 1
            while (!done) {
                def get = getCommitsForRepo(repoName, page, since, until)
                def response = client.execute(get)
                def jsonResponse = jsonSlurper.parse(response.getEntity().getContent())
                if (0 == jsonResponse.size()) {
                    done = true
                } else {
                    Set<String> emails = getEmails(jsonResponse)
                    if (!emails.empty) {
                        if (!repoToEmails.containsKey(repoName)) {
                            repoToEmails.put(repoName, new HashSet<>())
                        }
                        repoToEmails.get(repoName).addAll(emails)
                    }
                    page++
                }
            }
        }

        def reposWithCommits = new HashSet<String>()
        def emailToRepos = new HashMap<String, Set<String>>()
        repoToEmails.each { repo, emails ->
            reposWithCommits.add(repo)
            emails.each { email ->
                if (!emailToRepos.containsKey(email)) {
                    emailToRepos.put(email, new HashSet<>())
                }
                emailToRepos.get(email).add(repo)
            }
        }

        StringBuilder content = new StringBuilder()

        content.append '<html>\n'
        content.append("""<head>
\t<style type="text/css">
\t\ttd,span {
\t\t\tfont-family: courier;
\t\t\tsize: 10pt;
\t\t}
\t\t.paddingBetweenCols {
\t\t\tpadding:0 15px 0 15px;
\t\t}
\t</style>
</head>""")
        content.append '<body>\n'
        content.append "<span>From ${shortSince} to ${shortUntil}</span><br />\n"
        content.append '<br />\n'
        content.append '<table>\n'
        emailToRepos.keySet().toSorted { a, b ->
            a <=> b
        }.each { name ->
            content.append "<tr><td class=\"paddingBetweenCols\"><b>${name}</b></td>\n"
            content.append '<td class=\"paddingBetweenCols\">' + emailToRepos.get(name).collect {
                repositories.getSimpleName(it)
            }.toSorted { a, b -> a <=> b }.join(', ') + '</td></tr>\n'
            content.append '<tr><td>&nbsp</td></tr>\n'
        }
        content.append '</table>\n'

        content.append '<table>\n'
        reposWithCommits.toSorted { a, b ->
            repositories.getSimpleName(a) <=> repositories.getSimpleName(b)
        }.each { repo ->
            content.append '<tr>\n'
            content.append "<td>${repositories.getSimpleName(repo)}</td><td><a href=\"https://github.com/${repo}\">${repo}</a></td>\n"
            content.append '</tr>\n'
        }
        content.append '</table>\n'

        content.append '<br />\n'
        content.append '<br />\n'

        content.append "<span>Since: ${since} (${sinceEastern})</span><br />\n"
        content.append "<span>Until: ${until} (${untilEastern})</span><br />\n"

        content.append '</body></html>\n'

        content.toString()
    }

    private HttpGet getCommitsForRepo(String repo, int page, String since, String until) {
        HttpGet get = new HttpGet("https://api.github.com/repos/${repo}/commits?page=${page}&since=${since}&until=${until}")
        addHeaders(get)

        get
    }

    private void addHeaders(HttpMessage httpMessage) {
        httpMessage.addHeader('Accept', 'application/vnd.github.v3+json')
        httpMessage.addHeader('Authorization', "token ${System.getenv('GITHUB_AUTH_TOKEN')}")
    }

    private Set<String> getEmails(def jsonResponse) {
        Set<String> emails = new HashSet<String>()
        jsonResponse.each {
            def email = it['commit']['author']['email']
            if (email && !email.startsWith('serv-builder')) {
                if (knownUsers.containsEmail(email)) {
                    emails.add(knownUsers.getNameForEmail(email))
                } else {
                    emails.add(email)
                }
            }
        }

        return emails
    }

}
