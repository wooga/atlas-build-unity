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

import spock.lang.Unroll
import static com.wooga.security.SecurityHelper.*

abstract class FindPasswordSpec<T extends SecurityCommand> extends SecurityCommandSpec<T> {
    String testAccount = "someAccount"
    String testServiceOrServer = "someService"
    String testPassword = "somePassword"

    abstract void addTestPassword(String account, String serviceOrServer, String password)

    def "finds password"() {
        given: "a test password"
        addTestPassword(testAccount, testServiceOrServer, testPassword)
        assert keychainHasPassword(testKeychainLocation, testAccount, testServiceOrServer)

        when:
        def result = (command as FindPassword<SecurityCommand<String>>).printPassword().execute()

        then:
        result != null
        result.contains(testPassword)
    }

    @Unroll("fails with #expectedExeption when #reason")
    def "fails with exception when property is invalid addPassword findPassword"() {
        given: "invalid keychain"
        command.setProperty(property, value)

        when:
        command.execute()

        then:
        def e = thrown(expectedExeption)
        e.message.matches(messagePattern)

        where:
        property    | value                    | expectedExeption               | message
        "keychains" | [new File("/some/file")] | IllegalArgumentException.class | "does not exist"
        "keychains" | [File.createTempDir()]   | IllegalArgumentException.class | "is not a file"

        "account"   | null                     | NullPointerException.class     | "is null"
        "account"   | ""                       | IllegalArgumentException.class | "is empty"
        reason = "provided ${property} ${message}"
        propertyPattern = property == "keychains" ? "keychain" : property
        messagePattern = /provided ${propertyPattern}.*${message}/
    }

    @Unroll("property #property sets commandline flag #commandlineFlag")
    def "property sets commandline base"() {
        given: "set property"
        command.invokeMethod(method, value)

        when:
        def arguments = command.getArguments().join(" ")
        then:
        arguments.contains(expectedArguments)

        where:
        property            | commandlineFlag | value
        "account"           | "-a"            | "someAccount"
        "creator"           | "-c"            | "someCreator"
        "type"              | "-C"            | "someType"
        "kind"              | "-D"            | "someKind"
        "label"             | "-l"            | "someLabel"
        "printPassword"     | "-g"            | true
        "printPasswordOnly" | "-w"            | true
        expectedArguments = Boolean.isInstance(value) ? "${commandlineFlag}" : "${commandlineFlag} ${value.toString()}"
        method = Boolean.isInstance(value) ? property : "with${property.capitalize()}"
    }
}
