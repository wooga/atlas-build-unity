/*
 * Copyright 2018 Wooga GmbH
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

package wooga.gradle.build.unity.ios.internal

import org.gradle.api.Action
import org.gradle.api.credentials.PasswordCredentials
import wooga.gradle.build.unity.ios.IOSBuildPluginExtension
import static org.gradle.util.ConfigureUtil.configureUsing

class DefaultIOSBuildPluginExtension implements IOSBuildPluginExtension {

    private final PasswordCredentials fastlaneCredentials

    @Override
    PasswordCredentials getFastlaneCredentials() {
        fastlaneCredentials
    }

    @Override
    void setFastlaneCredentials(PasswordCredentials cred) {
        fastlaneCredentials.setUsername(cred.username)
        fastlaneCredentials.setPassword(cred.password)
        this
    }

    @Override
    IOSBuildPluginExtension fastlaneCredentials(Closure closure) {
        fastlaneCredentials(configureUsing(closure))
        this
    }

    @Override
    IOSBuildPluginExtension fastlaneCredentials(Action<PasswordCredentials> action) {
        action.execute(fastlaneCredentials)
        this
    }

    @Override
    IOSBuildPluginExtension fastlaneCredentials(PasswordCredentials cred) {
        setFastlaneCredentials(cred)
        this
    }

    DefaultIOSBuildPluginExtension() {
        fastlaneCredentials = new DefaultPasswordCredentials()
    }

    class DefaultPasswordCredentials implements PasswordCredentials {

        String username
        String password

        DefaultPasswordCredentials() {
        }

        String toString() {
            String.format("Credentials [username: %s]", this.username)
        }
    }
}
