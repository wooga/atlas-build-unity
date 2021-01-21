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
 * lock-keychain [-h] [-a|keychain]
 *             Lock keychain, or the default keychain if none is specified.  If
 *             the -a option is specified, all keychains are locked.
 */
class LockKeychain extends SecurityCommand<Void> implements KeychainCommand<LockKeychain> {

    final String command = "lock-keychain"

    Boolean all

    LockKeychain all(Boolean value = true) {
        this.all = value
        this
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []

        if (all) {
            arguments << "-a"
        } else if (keychain) {
            arguments.addAll(getOptionalKeychainArgument())
        }
        arguments
    }

    @Override
    protected Void convertResult(String output) {
    }
}
