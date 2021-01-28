/*
 * Copyright 2018-2020 Wooga GmbH
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

package wooga.gradle.macOS.security.tasks

import org.gradle.api.DefaultTask

import java.security.MessageDigest

abstract class AbstractInteractiveSecurityTask extends DefaultTask {
    static String getTempKeychainFileName(String keychainName) {
        MessageDigest digest = MessageDigest.getInstance("SHA-1")
        digest.update(keychainName.getBytes("ASCII"))
        byte[] passwordDigest = digest.digest()
        String hexString = passwordDigest.collect { String.format('%02x', it) }.join()
        ".fl${hexString.substring(0, 8).toUpperCase()}"
    }
}
