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
import spock.lang.Requires
import spock.lang.Unroll

import static com.wooga.security.SecurityHelper.getKeychainSettings

@Requires({os.macOs})
class ShowKeychainInfoSpec extends SecurityCommandSpec<ShowKeychainInfo> {
    ShowKeychainInfo command

    def setup() {
        command = new ShowKeychainInfo(testKeychainLocation)
    }

    def "returns settings for provided keychain"() {
        given:
        def expectedSettings = getKeychainSettings(testKeychainLocation)

        when:
        def result = command.execute()

        then:
        result != null
        MacOsKeychainSettings.isInstance(result)
        result == expectedSettings
    }

    @Unroll("fails with #expectedExeption when #reason")
    def "fails with exception when property is invalid"() {
        when:
        command.execute()

        then:
        def e = thrown(expectedExeption)
        e.message.matches(messagePattern)

        where:
        property   | expectedExeption               | message          | command
        "keychain" | NullPointerException.class     | "is null"        | new ShowKeychainInfo((File) null)
        "keychain" | IllegalArgumentException.class | "does not exist" | new ShowKeychainInfo(new File("/some/file"))
        "keychain" | IllegalArgumentException.class | "is not a file"  | new ShowKeychainInfo(File.createTempDir())

        reason = "property ${property} ${message}"
        messagePattern = /provided ${property}.*${message}/
    }

    @Unroll
    def "can initialize command with #type"() {
        when:
        new ShowKeychainInfo(value)

        then:
        noExceptionThrown()

        where:
        type                | value
        File.class          | new File("some/file")
        MacOsKeychain.class | new MacOsKeychain(new File("some/file"), "pw")
    }
}

