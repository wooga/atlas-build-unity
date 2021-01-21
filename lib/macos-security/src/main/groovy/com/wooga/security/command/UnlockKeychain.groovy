/*
 * Copyright 2021 Wooga GmbH
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


/*
 * unlock-keychain [-hu] [-p password] [keychain]
 *             Unlock keychain, or the default keychain if none is specified.
 */

class UnlockKeychain extends SecurityCommand<Boolean> implements KeychainCommand<UnlockKeychain> {

    final String command = "unlock-keychain"

    String password

    UnlockKeychain withPassword(String value) {
        this.password = value
        this
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []

        if (password) {
            arguments << "-p" << password
        }

        arguments.addAll(getOptionalKeychainArgument())
        arguments
    }

    @Override
    protected Boolean convertResult(String output) {
        true
    }

    @Override
    List<String> getSensitiveFlags() {
        ["-p"]
    }
}
