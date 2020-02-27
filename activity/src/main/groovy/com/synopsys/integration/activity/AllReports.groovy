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
package com.synopsys.integration.activity

import com.synopsys.integration.activity.report.EmailReportContext
import com.synopsys.integration.activity.report.EmailReporter
import com.synopsys.integration.activity.report.LocalReportContext
import com.synopsys.integration.activity.report.LocalReporter
import com.synopsys.integration.activity.report.ReportRunner
import com.synopsys.integration.activity.report.codequality.CodeQuality
import com.synopsys.integration.activity.report.github.GithubActivity
import com.synopsys.integration.activity.util.EnvironmentVariables
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AllReports {
    @Autowired
    ReportRunner reportRunner

    @Autowired
    CodeQuality codeQuality

    @Autowired
    GithubActivity githubActivity

    @Autowired
    EmailReporter emailReporter

    @Autowired
    LocalReporter localReporter

    private LocalReportContext defaultLocalContext
    private EmailReportContext defaultGithubEmailContext
    private EmailReportContext defaultCodeEmailContext

    AllReports() {
        defaultLocalContext = new LocalReportContext(EnvironmentVariables.LOCAL_REPORT_FILENAME_FORMAT, EnvironmentVariables.LOCAL_REPORT_HTML_EXECUTOR_PATH)
        defaultGithubEmailContext = new EmailReportContext(EnvironmentVariables.EMAIL_REPORT_TO, EnvironmentVariables.EMAIL_REPORT_FROM, 'github activity', EnvironmentVariables.EMAIL_REPORT_HOST)
        defaultCodeEmailContext = new EmailReportContext(EnvironmentVariables.EMAIL_REPORT_TO, EnvironmentVariables.EMAIL_REPORT_FROM, 'code quality', EnvironmentVariables.EMAIL_REPORT_HOST)
    }

    void runLocalGithubActivityReport() {
        reportRunner.runReport(14, githubActivity, localReporter, defaultLocalContext)
    }

    void runEmailGithubActivityReport() {
        reportRunner.runReport(14, githubActivity, emailReporter, defaultGithubEmailContext)
    }

    void runLocalCodeQualityReport() {
        reportRunner.runReport(14, codeQuality, localReporter, defaultLocalContext)
    }

    void runEmailCodeQualityReport() {
        reportRunner.runReport(14, codeQuality, emailReporter, defaultCodeEmailContext)
    }

}
