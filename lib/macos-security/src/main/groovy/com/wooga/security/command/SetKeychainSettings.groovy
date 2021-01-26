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

import com.wooga.security.MacOsKeychainSettings

/*
 * set-keychain-settings [-hlu] [-t timeout] [keychain]
 *             Set settings for keychain, or the default keychain if none is
 *             specified.
 *
 *             -l              Lock keychain when the system sleeps.
 *             -u              Lock keychain after timeout interval.
 *             -t timeout      Specify timeout interval in seconds (omitting this
 *                             option specifies "no timeout").
 */

class SetKeychainSettings extends SecurityCommand<Void> implements KeychainCommand<SetKeychainSettings> {

    final String command = "set-keychain-settings"

    Boolean lockWhenSystemSleeps

    SetKeychainSettings lockWhenSystemSleeps(Boolean value) {
        this.lockWhenSystemSleeps = value
        this
    }

    Integer timeout

    SetKeychainSettings withTimeout(Integer value) {
        this.timeout = value
        this
    }

    SetKeychainSettings withSettings(MacOsKeychainSettings settings) {
        withTimeout(settings.timeout)
        lockWhenSystemSleeps(settings.lockWhenSystemSleeps)
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        if (lockWhenSystemSleeps) {
            arguments << "-l"
        }

        if (timeout >= 0) {
            arguments << "-t" << timeout.toString()
        }

        arguments.addAll(getOptionalKeychainArgument())
        arguments
    }

    @Override
    protected Void convertResult(String output) {
    }
}
