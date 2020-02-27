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

class EnvironmentVariables {
    static final String LOCAL_REPORT_FILENAME_FORMAT = System.getenv('LOCAL_REPORT_FILENAME_FORMAT')
//    static final String LOCAL_REPORT_FILENAME_FORMAT = '~/atom_workspace/swipedir/code_quality_%s.html'
//    static final String LOCAL_REPORT_FILENAME_FORMAT = 'C:\\Users\\ekerwin\\Documents\\team_activity_%s.html'

    static final String LOCAL_REPORT_HTML_EXECUTOR_PATH = System.getenv('LOCAL_REPORT_HTML_EXECUTOR_PATH')
//    static final String LOCAL_REPORT_HTML_EXECUTOR_PATH = System.getenv('/Applications/Vivaldi.app/Contents/MacOS/Vivaldi')
//    static final String LOCAL_REPORT_HTML_EXECUTOR_PATH = System.getenv('C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe')

    static final String EMAIL_REPORT_TO = System.getenv('EMAIL_REPORT_TO')
    static final String EMAIL_REPORT_FROM = System.getenv('EMAIL_REPORT_FROM')
    static final String EMAIL_REPORT_HOST = System.getenv('EMAIL_REPORT_HOST')

}
