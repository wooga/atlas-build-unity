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

import org.apache.commons.lang3.RandomStringUtils
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.introspector.BeanAccess
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import wooga.gradle.build.unity.secrets.EncryptedSecret
import wooga.gradle.build.unity.secrets.Secret

import javax.crypto.spec.SecretKeySpec

class Secrets implements Map<String, EncryptedSecret<?>> {

    @Delegate
    Map<String, EncryptedSecret<?>> secrets = [:]

    Secrets() {
    }

    static Secrets decode(String input) {
        Constructor constructor = new Constructor()
        constructor.addTypeDescription(new TypeDescription(Secrets.class, "!secrets"))
        constructor.addTypeDescription(new TypeDescription(EncryptedSecretText.class, "!secretText"))
        constructor.addTypeDescription(new TypeDescription(EncryptedSecretFile.class, "!secretFile"))
        constructor.propertyUtils.beanAccess = BeanAccess.FIELD
        Yaml yaml = new Yaml(constructor)
        yaml.loadAs(input, Secrets.class)
    }

    String encode() {
        Representer representer = new Representer()
        representer.propertyUtils.beanAccess = BeanAccess.FIELD
        representer.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        representer.addClassTag(Secrets.class, new Tag("!secrets"))
        representer.addClassTag(EncryptedSecretText.class, new Tag("!secretText"))
        representer.addClassTag(EncryptedSecretFile.class, new Tag("!secretFile"))
        Yaml yaml = new Yaml(representer)

        yaml.dump(this)
    }

    class EnvironmentSecrets implements Map<String, Object> {
        @Delegate
        private final Map<String,Object> environment = [:]

        void clear() {
            environment.each { _,secret ->
                if (File.isInstance(secret)) {
                    ((File) secret).delete()
                }
            }
            environment.clear()
        }
    }

    EnvironmentSecrets encodeEnvironment(SecretKeySpec secretsKey) {
        EnvironmentSecrets env = new EnvironmentSecrets()
        secrets.each {secretId, secret ->
            def decodedSecret = secret.decryptedSecretValue(secretsKey)
            if(String.isInstance(decodedSecret)) {
                env.put(secretId.toUpperCase(), decodedSecret as String)
            } else if(byte[].isInstance(decodedSecret)) {
                File tempFile = File.createTempFile(RandomStringUtils.random(10, true, true), RandomStringUtils.random(10, true, true))
                tempFile.deleteOnExit()
                tempFile.bytes = decodedSecret as byte[]
                env.put(secretId.toUpperCase(), tempFile)
            } else {
                throw new ScriptException("Unsupported secret type ${secret.secretValue.class} of ${secretId}")
            }
        }
        env
    }

    void putSecret(String secretId, Secret<?> secret, SecretKeySpec secretsKey) {
        def encryptedSecret

        if(String.isInstance(secret.secretValue)) {
            encryptedSecret = new EncryptedSecretText(secret as Secret<String>, secretsKey)
        } else if(byte[].isInstance(secret.secretValue)) {
            encryptedSecret = new EncryptedSecretFile(secret as Secret<byte[]>, secretsKey)
        } else {
            throw new IllegalArgumentException("Unsupported secret type ${secret.secretValue.class} of ${secretId}")
        }
        secrets.put(secretId, encryptedSecret)
    }

    Secret<?> getSecret(String secretId, SecretKeySpec secretsKey) {
        if(secrets.containsKey(secretId)) {
            EncryptedSecret<?> secret = secrets.get(secretId)
            return secret.decryptSecret(secretsKey)
        }
        return null
    }

    Collection<Secret<?>> secretValues(SecretKeySpec secretsKey) {
        secrets.values().collect {it.decryptSecret(secretsKey)}
    }
}
