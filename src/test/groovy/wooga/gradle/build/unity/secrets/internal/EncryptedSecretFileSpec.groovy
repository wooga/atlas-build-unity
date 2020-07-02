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


import wooga.gradle.build.unity.secrets.Secret

import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

class EncryptedSecretFileSpec extends EncryptedSecretSpec<byte[], EncryptedSecretFile, SecretFile> {

    @Override
    byte[] getTestValue() {
        SecureRandom random = new SecureRandom()
        byte[] test = new byte[2048]
        random.nextBytes(test)
        test
    }

    @Override
    SecretFile createSecret(byte[] value) {
        new SecretFile(value)
    }

    @Override
    EncryptedSecretFile createEncryptedSecret(Secret<byte[]> secret, SecretKeySpec secretKey) {
        new EncryptedSecretFile(secret, secretKey)
    }
}
