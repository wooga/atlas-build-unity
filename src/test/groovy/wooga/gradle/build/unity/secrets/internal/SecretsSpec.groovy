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


import spock.lang.Specification
import spock.lang.Unroll
import wooga.gradle.build.unity.secrets.EncryptedSecret

import javax.crypto.spec.SecretKeySpec

class SecretsSpec extends Specification {

    SecretKeySpec key
    Secrets secrets

    def setup() {
        key = EncryptionSpecHelper.createSecretKey("secret_pass_phrase")
        secrets = new Secrets()
    }

    @Unroll
    def "can put unencrypted secret<#type>"() {
        when:
        secrets.putSecret(secretId, new DefaultSecret(value), key)

        then:
        noExceptionThrown()
        secrets.containsKey(secretId)
        secrets.get(secretId) != null
        EncryptedSecret.isInstance(secrets.get(secretId))

        where:
        type     | value            | supported
        "String" | "a secret"       | true
        "byte[]" | "a secret".bytes | true
        secretId = "someId"
    }

    def "can get unencrypted secret<#type>"() {
        given: "a secret"
        def secret = new DefaultSecret(value)
        secrets.putSecret(secretId, secret, key)

        when:
        def result = secrets.getSecret(secretId, key)

        then:
        noExceptionThrown()
        result != null
        result.secretValue == secret.secretValue
        result != secret

        where:
        type     | value            | supported
        "String" | "a secret"       | true
        "byte[]" | "a secret".bytes | true
        secretId = "someId"
    }

    def "can list secretId"() {
        given: "a few secrets"
        secretIds.each {
            secrets.putSecret(it, new DefaultSecret(it.toUpperCase()), key)
        }

        expect:
        secrets.keySet() == secretIds.toSet()

        where:
        secretIds = ["secret1", "secret2", "secret3", "secret4"]
    }

    def "can list encrypted secret values"() {
        given: "a few secrets"
        secretIds.each {
            secrets.putSecret(it, new DefaultSecret(testValue), key)
        }

        expect:
        secrets.values().size() == secretIds.size()
        secrets.values().every { it.secretValue != testValue}
        secrets.values().every { it.decryptedSecretValue(key) == testValue}

        where:
        secretIds = ["secret1", "secret2", "secret3", "secret4"]
        testValue = "testValue"
    }

    def "can list decrypted secret values"() {
        given: "a few secrets"
        secretIds.each {
            secrets.putSecret(it, new DefaultSecret(testValue), key)
        }

        expect:
        secrets.secretValues(key) != secrets.values()
        secrets.secretValues(key).size() == secretIds.size()
        secrets.secretValues(key).every { it.secretValue == testValue}

        where:
        secretIds = ["secret1", "secret2", "secret3", "secret4"]
        testValue = "testValue"
    }

    @Unroll
    def "add secret fails when type is not supported (#type)"() {
        when:
        secrets.putSecret("someId", new DefaultSecret(value), key)
        then:
        def e = thrown(IllegalArgumentException)
        e.message.startsWith("Unsupported secret type")

        where:
        type    | value
        "bool"  | true
        "int"   | 123456
        "float" | 1.6
    }

    def "can dump encrypted secrets to yml"() {
        given: "some secret texts"
        secrets.putSecret("test1", new SecretText("testValue1"), key)
        secrets.putSecret("test2", new SecretText("testValue2"), key)

        and: "some secret files"
        secrets.putSecret("test3", new SecretFile("testValue1".bytes), key)
        secrets.putSecret("test4", new SecretFile("testValue2".bytes), key)

        secrets.values()

        when:
        String ymlDump = secrets.encode()

        then:
        noExceptionThrown()
        ymlDump != null
        def loadedSecrets = Secrets.decode(ymlDump)
        loadedSecrets.encode() == ymlDump
        loadedSecrets.getSecret("test3", key).secretValue == "testValue1".bytes
    }

    def "can encode for environment"() {
        given: "some secret texts"
        secrets.putSecret("test1", new SecretText("testValue1"), key)
        secrets.putSecret("test2", new SecretText("testValue2"), key)

        and: "some secret files"
        secrets.putSecret("test3", new SecretFile("testValue1".bytes), key)
        secrets.putSecret("test4", new SecretFile("testValue2".bytes), key)

        when:
        def env = secrets.encodeEnvironment(key)

        then:
        noExceptionThrown()
        env != null
        env["TEST1"] == "testValue1"
        env["TEST2"] == "testValue2"
        ((File)(env["TEST3"])).bytes == "testValue1".bytes
        ((File)(env["TEST4"])).bytes == "testValue2".bytes

        cleanup:
        env.each { _, value ->
            if (File.isInstance(value)) {
                ((File) value).delete()
            }
        }
    }
}
