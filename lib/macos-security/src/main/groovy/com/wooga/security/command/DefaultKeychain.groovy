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
 *  default-keychain [-h] [-d user|system|common|dynamic] [-s [keychain]]
 *             Display or set the default keychain.
 *
 *             -d user|system|common|dynamic
 *                      Use the specified preference domain.
 *             -s       Set the default keychain to the specified keychain.
 *                      Unset it if no keychain is specified.
 */

class DefaultKeychain extends SecurityCommand<File> implements KeychainCommand<DefaultKeychain> {
    String command = "default-keychain"

    Domain domain

    DefaultKeychain withDomain(Domain value) {
        this.domain = value
        this
    }

    Boolean setDefaultKeychain

    DefaultKeychain setDefaultKeychain(Boolean value = true) {
        this.setDefaultKeychain = value
        this
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        if (domain) {
            arguments << "-d" << domain.toString()
        }
        if (setDefaultKeychain) {
            arguments << "-s"
            arguments.addAll(getOptionalKeychainArgument())
        }
        arguments
    }

    @Override
    protected File convertResult(String output) {
        if (!output) {
            return null
        }
        new File(output.trim().replaceAll(/^"|"$/, ''))
    }
}
