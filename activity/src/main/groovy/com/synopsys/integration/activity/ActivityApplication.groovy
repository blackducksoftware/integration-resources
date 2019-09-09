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
package com.synopsys.integration.activity

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class ActivityApplication implements ApplicationRunner {
    @Autowired
    AllReports allReports

    static void main(final String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ActivityApplication.class)
        builder.logStartupInfo(false)
        builder.run(args)
    }

    @Override
    void run(final ApplicationArguments applicationArguments) {
        String reportToRun = applicationArguments.getOptionValues('r')[0]
        String methodToUse = applicationArguments.getOptionValues('m')[0]

        if (reportToRun == 'github') {
            if (methodToUse == 'local') {
                allReports.runLocalGithubActivityReport()
            } else if (methodToUse == 'email') {
                allReports.runEmailGithubActivityReport()
            }
        } else if (reportToRun == 'code') {
            if (methodToUse == 'local') {
                allReports.runLocalCodeQualityReport()
            } else if (methodToUse == 'email') {
                allReports.runEmailCodeQualityReport()
            }
        }
    }

}
