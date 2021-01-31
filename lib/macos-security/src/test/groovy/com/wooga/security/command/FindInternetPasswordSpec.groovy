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

import spock.lang.Requires
import spock.lang.Unroll

import static com.wooga.security.SecurityHelper.addInternetPassword

@Requires({os.macOs})
class FindInternetPasswordSpec extends FindPasswordSpec<FindInternetPassword> {
    FindInternetPassword command

    def setup() {
        command = new FindInternetPassword(testAccount, testServiceOrServer)
        command.withKeychain(testKeychainLocation)
    }

    @Override
    void addTestPassword(String account, String serviceOrServer, String password) {
        addInternetPassword(testKeychainLocation, testAccount, testServiceOrServer, testPassword)
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
        property | value | expectedExeption               | message
        "server" | null  | NullPointerException.class     | "is null"
        "server" | ""    | IllegalArgumentException.class | "is empty"
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
        property             | commandlineFlag | value          | argument
        "server"             | "-s"            | "someServer"   | _
        "domain"             | "-d"            | "someDomain"   | _
        "path"               | "-p"            | "/some/path"   | _
        "port"               | "-P"            | 4444           | _
        "protocol"           | "-r"            | "https"        | _
        "authenticationType" | "-t"            | "someAuthType" | _
        expectedArguments = Boolean.isInstance(value) ? "${commandlineFlag}" : "${commandlineFlag} ${argument != _ ? argument : value.toString()}"
        method = Boolean.isInstance(value) ? property : "with${property.capitalize()}"
    }
}
