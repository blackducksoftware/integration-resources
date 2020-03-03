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

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class KnownUsers {
    private final Map<String, String> emailsToNames = new HashMap<>();

    private void addUser(String name, String email) {
        emailsToNames.put(email, name);
    }

    public KnownUsers() {
        addUser("Brian", "bmandel@synopsys.com");
        addUser("Brian", "bmandel@blackducksoftware.com");
        addUser("Brian", "bamandel@gmail.com");
        addUser("Brian", "bamandel@users.noreply.github.com");
        addUser("Gavin", "gavink@synopsys.com");
        addUser("Gavin", "gkillough@blackducksoftware.com");
        addUser("Paulo", "psantos@synopsys.com");
        addUser("Paulo", "psantos@blackducksoftware.com");
        addUser("Rich", "rotte@synopsys.com");
        addUser("Rich", "rotte@blackducksoftware.com");
        addUser("Steve", "sbillings@synopsys.com");
        addUser("Steve", "sbillings@blackducksoftware.com");
        addUser("Steve", "billings@synopsys.com");
        addUser("Eric", "ekerwin@synopsys.com");
        addUser("Eric", "ekerwin@blackducksoftware.com");
        addUser("Eric", "ekerwin@kingofnerds.com");
        addUser("James", "jrichard@synopsys.com");
        addUser("James", "jrichard@blackducksoftware.com");
        addUser("Jordan", "jordanp@synopsys.com");
        addUser("Jordan", "jpiscitelli@blackducksoftware.com");
        addUser("Jordan", "jordan.r.piscitelli@gmail.com");
        addUser("Jake", "jakem@synopsys.com");
        addUser("Jake", "jmathews@blackducksoftware.com");
        addUser("Jake", "jake.mathews.email@gmail.com");
        addUser("Alex", "alex.crowley@synopsys.com");
        addUser("Alex", "51929980+crowleySynopsys@users.noreply.github.com");
    }

    public boolean containsEmail(String email) {
        return emailsToNames.containsKey(email);
    }

    public String getNameForEmail(String email) {
        return emailsToNames.get(email);
    }

}
