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
import spock.lang.Requires
import spock.lang.Unroll

import static com.wooga.security.SecurityHelper.getKeychainSettings

@Requires({ os.macOs })
class SetKeychainSettingsSpec extends SecurityCommandSpec<SetKeychainSettings> {

    SetKeychainSettings command = new SetKeychainSettings()

    MacOsKeychainSettings initialSettings

    def setup() {
        this.initialSettings = getKeychainSettings(testKeychainLocation)
    }

    def "sets keychain settings"() {
        given: "a new settings file"
        def newSettings = new MacOsKeychainSettings(true, 2000)
        command.withKeychain(testKeychainLocation)
        command.withSettings(newSettings)

        when:
        command.execute()

        then:
        noExceptionThrown()
        def currentSettings = getKeychainSettings(testKeychainLocation)
        currentSettings == newSettings
        currentSettings != initialSettings
    }

    @Unroll("fails with #expectedExeption when #reason")
    def "fails with exception when property is invalid"() {
        given: "invalid keychain"
        command.setProperty(property, value)

        when:
        command.execute()

        then:
        def e = thrown(expectedExeption)
        e.message.matches(messagePattern)

        where:
        property   | value                  | expectedExeption               | message
        "keychain" | new File("/some/file") | IllegalArgumentException.class | "does not exist"
        "keychain" | File.createTempDir()   | IllegalArgumentException.class | "is not a file"

        reason = "provided ${property} ${message}"
        messagePattern = /provided ${property}.*${message}/
    }

    @Unroll("property #property sets commandline flag #commandlineFlag")
    def "property sets commandline"() {
        given: "set property"
        command.invokeMethod(method, value)

        when:
        def arguments = command.getArguments().join(" ")
        then:
        arguments.contains(expectedArguments)

        where:
        property               | commandlineFlag | value
        "lockWhenSystemSleeps" | "-l"            | true
        "timeout"              | "-t"            | 1000
        expectedArguments = Boolean.isInstance(value) ? "${commandlineFlag}" : "${commandlineFlag} ${value.toString()}"
        method = Boolean.isInstance(value) ? property : "with${property.capitalize()}"
    }
}
