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

import com.wooga.security.MacOsKeychain

trait KeychainCommand<T extends SecurityCommand> {
    File keychain

    T withKeychain(MacOsKeychain value) {
        withKeychain(value.location)
    }

    T withKeychain(File value) {
        this.keychain = value
        this as T
    }

    List<String> getMandatoryKeychainArguments() {
        def arguments = []
        SecurityCommand.validateKeychainProperty(keychain)
        arguments << "-k" << keychain.path
        arguments
    }

    List<String> getOptionalKeychainArgument() {
        def arguments = []
        if (keychain) {
            SecurityCommand.validateKeychainProperty(keychain)
            arguments << keychain.path
        }
        arguments
    }
}
