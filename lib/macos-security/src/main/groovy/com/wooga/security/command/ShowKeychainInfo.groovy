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
import com.wooga.security.MacOsKeychainSettings

/*
 * show-keychain-info [-h] [keychain]
 *             Show the settings for keychain.
 */
class ShowKeychainInfo extends SecurityCommand<MacOsKeychainSettings> {

    final String command = "show-keychain-info"

    final File keychain

    ShowKeychainInfo(File keychain) {
        this.keychain = keychain
    }

    ShowKeychainInfo(MacOsKeychain keychain) {
        this(keychain.location)
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        validateKeychainProperty(keychain)
        arguments << keychain.path
    }

    @Override
    protected MacOsKeychainSettings convertResult(String output) {
        MacOsKeychainSettings.fromOutput(output)
    }
}
