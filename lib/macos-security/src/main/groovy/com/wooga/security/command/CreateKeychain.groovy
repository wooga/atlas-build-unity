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

/*
 * create-keychain [-hP] [-p password] [keychain...]
 *             Create keychains.
 *
 *             -P              Prompt the user for a password using the Secu-
 *                             rityAgent.
 *             -p password     Use password as the password for the keychains
 *                             being created.
 *
 *             If neither -P or -p password are specified, the user is prompted
 *             for a password on the command line. Use of the -p option is inse-
 *             cure.
 */

class CreateKeychain extends SecurityCommand<MacOsKeychain> {
    String password

    CreateKeychain withPassword(String value) {
        this.password = value
        this
    }
    File location

    CreateKeychain withLocation(File value) {
        this.location = value
        this
    }

    CreateKeychain(String password, File location) {
        this.password = password
        this.location = location
    }

    final String command = "create-keychain"

    @Override
    protected MacOsKeychain convertResult(String output) {
        new MacOsKeychain(location, password)
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []

        validateStringProperty(password, "password")

        arguments << "-p" << password

        if (!location) {
            throw new NullPointerException("provided location is null")
        }

        arguments << location.path
        arguments
    }

    @Override
    List<String> getSensitiveFlags() {
        ["-p"]
    }
}
