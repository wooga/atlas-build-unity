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

import static com.wooga.security.SecurityHelper.*
import spock.lang.Shared
import spock.lang.Specification

abstract class SecurityCommandSpec<T extends SecurityCommand> extends Specification {

    @Shared
    String keychainPassword = "testPassword"

    @Shared
    File testKeychainLocation

    abstract T getCommand()

    def setup() {
        def (File location, String _) = setupTestKeychain()
        this.testKeychainLocation = location
    }

    def cleanup() {
        this.testKeychainLocation.delete()
    }

    File createTempKeychainLocation() {
        new File(File.createTempDir(), "test.keychain")
    }

    Tuple2<File, String> setupTestKeychain() {
        def location = createTempKeychainLocation()
        def password = keychainPassword
        createKeychain(location, password)
        assert location.exists()
        new Tuple2<File, String>(location, password)
    }
}
