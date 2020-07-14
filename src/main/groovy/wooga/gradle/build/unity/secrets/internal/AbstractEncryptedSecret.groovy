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

import groovy.transform.InheritConstructors
import wooga.gradle.build.unity.secrets.EncryptedSecret
import wooga.gradle.build.unity.secrets.Secret

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.AlgorithmParameters
import java.security.GeneralSecurityException

@InheritConstructors
abstract class AbstractEncryptedSecret<T> extends DefaultSecret<T> implements EncryptedSecret<T> {
    AbstractEncryptedSecret(Secret<T> secret, SecretKeySpec key) {
        secretValue = encryptSecretValue(secret.secretValue, key)
    }

    protected static byte[] encrypt(byte[] property, SecretKeySpec key) throws GeneralSecurityException, UnsupportedEncodingException {
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        pbeCipher.init(Cipher.ENCRYPT_MODE, key)
        AlgorithmParameters parameters = pbeCipher.getParameters()
        IvParameterSpec ivParameterSpec = parameters.getParameterSpec(IvParameterSpec.class)
        byte[] cryptoText = pbeCipher.doFinal(property)
        byte[] iv = ivParameterSpec.getIV()
        byte[] combined = new byte[iv.length + cryptoText.length]
        System.arraycopy(iv, 0, combined, 0, iv.length)
        System.arraycopy(cryptoText, 0, combined, iv.length, cryptoText.length)

        combined
    }

    protected static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    protected static byte[] decrypt(byte[] value, SecretKeySpec key) throws GeneralSecurityException, IOException {
        byte[] iv = Arrays.copyOfRange(value, 0, 16)
        byte[] property = Arrays.copyOfRange(value, 16, value.length)
        Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv))
        pbeCipher.doFinal(property)
    }

    protected static byte[] base64Decode(String property) throws IOException {
        return Base64.getDecoder().decode(property);
    }

    @Override
    Secret<T> decryptSecret(SecretKeySpec key) {
        new DefaultSecret<T>(decryptedSecretValue(key))
    }

    abstract T decryptedSecretValue(SecretKeySpec key)
    abstract protected T encryptSecretValue(T secret, SecretKeySpec key)
}
