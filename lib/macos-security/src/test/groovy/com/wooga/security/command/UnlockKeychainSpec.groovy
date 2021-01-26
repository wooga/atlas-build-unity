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

@Requires({os.macOs})
class UnlockKeychainSpec extends SecurityCommandSpec<UnlockKeychain> {
    UnlockKeychain command = new UnlockKeychain()

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
}
