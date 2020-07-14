/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.build.unity.secrets.internal


import wooga.gradle.build.unity.secrets.EncryptedSecret
import wooga.gradle.build.unity.secrets.Secret

import javax.crypto.BadPaddingException
import javax.crypto.spec.SecretKeySpec

abstract class EncryptedSecretSpec<T, E extends EncryptedSecret<T>, S extends Secret<T>> extends SecretSpec<T, S> {
    SecretKeySpec secretKey

    abstract E createEncryptedSecret(Secret<T> secret, SecretKeySpec key)

    def setup() {
        secretKey = EncryptionSpecHelper.createSecretKey("some_secret_passphrase")
    }

    def "can encrypt secret"() {
        given: "a secret"
        def secret = createSecret(testValue)

        when:
        def encrypted = createEncryptedSecret(secret, secretKey)

        then:
        encrypted.secretValue != secret.secretValue
    }

    def "can decrypt secret"() {
        given: "an encrypted secret"
        def secret = createSecret(testValue)
        def encrypted = createEncryptedSecret(secret, secretKey)

        expect:
        encrypted.decryptedSecretValue(secretKey) == secret.secretValue
    }

    def "can used saved key"() {
        given: "an encrypted secret"
        def secret = createSecret(testValue)
        def encrypted = createEncryptedSecret(secret, secretKey)

        and: "a key saved to disk"
        def testKey = File.createTempFile("test","secretKey")
        testKey.bytes = secretKey.encoded

        and: "a second key created from file"
        def keyFromFile = new SecretKeySpec(testKey.bytes, "AES")

        expect:
        encrypted.decryptedSecretValue(keyFromFile) == secret.secretValue
    }

    def "decrypt with different key fails"() {
        given: "an encrypted secret"
        def secret = createSecret(testValue)
        def encrypted = createEncryptedSecret(secret, secretKey)

        and: "a second encrypted value with a different key"
        def secondKey = EncryptionSpecHelper.createSecretKey("some_other_secret_passphrase")

        when:
        encrypted.decryptedSecretValue(secondKey)

        then:
        thrown(BadPaddingException)
    }
}
