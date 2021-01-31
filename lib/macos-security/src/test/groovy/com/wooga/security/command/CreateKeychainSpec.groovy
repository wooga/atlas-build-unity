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
import spock.lang.Requires
import spock.lang.Unroll

@Requires({ os.macOs })
class CreateKeychainSpec extends SecurityCommandSpec<CreateKeychain> {

    CreateKeychain command

    def setup() {
        testKeychainLocation = createTempKeychainLocation()
        command = new CreateKeychain(keychainPassword, testKeychainLocation)
    }

    def cleanup() {
        testKeychainLocation.delete()
    }

    def "creates keychain at location"() {
        given: "no file at location"
        assert !testKeychainLocation.exists()

        when:
        command.execute()

        then:
        testKeychainLocation.exists()
    }

    def "returns MacOsKeychain object"() {
        when:
        def k = command.execute()

        then:
        k != null
        MacOsKeychain.isInstance(k)
        k.exists()
        k.location == testKeychainLocation
        k.password == keychainPassword
    }

    @Unroll("fails with #expectedExeption when #reason")
    def "fails with exception when property is invalid"() {
        when:
        command.execute()

        then:
        def e = thrown(expectedExeption)
        e.message.contains(message)

        where:
        property   | value         | expectedExeption           | message                                         | command
        "password" | "is null"     | NullPointerException.class | "provided password is null"                     | new CreateKeychain(null, createTempKeychainLocation())
        "location" | "is null"     | NullPointerException.class | "provided location is null"                     | new CreateKeychain(keychainPassword, null)
        "location" | "a directory" | IOException.class          | "A keychain with the same name already exists." | new CreateKeychain(keychainPassword, File.createTempDir())
        "location" | "exists"      | IOException.class          | "A keychain with the same name already exists." | new CreateKeychain(keychainPassword, File.createTempDir())
        reason = "property '${property}' '${value}'"
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
        property   | commandlineFlag | value
        "password" | "-p"            | "somePassword"
        expectedArguments = Boolean.isInstance(value) ? "${commandlineFlag}" : "${commandlineFlag} ${value.toString()}"
        method = Boolean.isInstance(value) ? property : "with${property.capitalize()}"
    }
}
