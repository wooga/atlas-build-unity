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

@Requires({os.macOs})
class DeleteKeychainSpec extends SecurityCommandSpec<DeleteKeychain> {

    DeleteKeychain command = new DeleteKeychain()
    List<Tuple2<File,String>> testKeychains = []

    def setup() {
        0..5.each {
            testKeychains.add(setupTestKeychain())
        }
    }

    def cleanup() {
        testKeychains.each {
            it.first.delete()
        }
    }

    def "deletes single keychain"() {
        given: "a keychain"
        def (File location, _) = testKeychains.first()
        command.withKeychain(location)

        when:
        command.execute()

        then:
        !location.exists()
    }

    def "deletes multiple keychains"() {
        given: "multiple keychains"
        testKeychains.each {
            def (File location, _) = it
            command.withKeychain(location)
        }

        when:
        command.execute()

        then:
        testKeychains.every { !it.first.exists() }
    }

    def "deletes single keychain with MacOsKeychain"() {
        given: "a keychain"
        def (File location, String password) = testKeychains.first()
        def k = new MacOsKeychain(location, password)
        command.withKeychain(k)

        when:
        command.execute()

        then:
        !k.exists()
        !location.exists()
    }

    @Unroll("fails with #expectedExeption when #reason")
    def "fails with exception when keychain is invalid"() {
        given: "invalid keychain"
        command.withKeychain((File) value)

        when:
        command.execute()

        then:
        def e = thrown(expectedExeption)
        e.message.contains(message)

        where:
        value                  | m                     | expectedExeption               | message
        new File("/some/file") | "does not exists"     | IllegalArgumentException.class | "does not exist"
        File.createTempDir()   | "points to directory" | IllegalArgumentException.class | "is not a file"

        reason = "provided keychain ${m}"
    }
}
