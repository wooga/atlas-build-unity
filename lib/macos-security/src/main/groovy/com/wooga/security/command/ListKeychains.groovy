/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wooga.security.command

import com.wooga.security.Domain

/*
 * list-keychains [-h] [-d user|system|common|dynamic] [-s [keychain...]]
 *             Display or manipulate the keychain search list.
 *
 *             -d user|system|common|dynamic
 *                      Use the specified preference domain.
 *             -s       Set the search list to the specified keychains.
 */

class ListKeychains extends SecurityCommand<List<File>> implements MultiKeychainCommand<ListKeychains> {
    String command = "list-keychains"

    Domain domain

    ListKeychains withDomain(Domain value) {
        this.domain = value
        this
    }

    Boolean setKeychainSearchList

    ListKeychains setKeychainSearchList(Boolean value = true) {
        this.setKeychainSearchList = value
        this
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        if (domain) {
            arguments << "-d" << domain.toString()
        }
        if (setKeychainSearchList) {
            arguments << "-s"
            arguments.addAll(getMultiKeychainsArgument())
        }
        arguments
    }

    @Override
    protected List<File> convertResult(String output) {
        if (!output) {
            return []
        }
        output.readLines().collect { new File(it.trim().replaceAll(/^"|"$/, '')) }
    }
}
