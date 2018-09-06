/*
 * Copyright 2017 the original author or authors.
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
import wooga.gradle.build.unity.ios.IOSBuildPluginExtension
import static org.gradle.util.ConfigureUtil.configureUsing

class DefaultIOSBuildPluginExtension implements IOSBuildPluginExtension {

    private final org.gradle.api.credentials.PasswordCredentials fastlaneCredentials
    private String keychainPassword
    private String certificatePassphrase
    private String applicationIdentifier
    private String teamId
    private String scheme
    private String configuration

    @Override
    org.gradle.api.credentials.PasswordCredentials getFastlaneCredentials() {
        fastlaneCredentials
    }

    @Override
    void setFastlaneCredentials(org.gradle.api.credentials.PasswordCredentials cred) {
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
    IOSBuildPluginExtension fastlaneCredentials(Action<org.gradle.api.credentials.PasswordCredentials> action) {
        action.execute(fastlaneCredentials)
        this
    }

    @Override
    IOSBuildPluginExtension fastlaneCredentials(org.gradle.api.credentials.PasswordCredentials cred) {
        setFastlaneCredentials(cred)
        this
    }

    @Override
    String getKeychainPassword() {
        keychainPassword
    }

    @Override
    void setKeychainPassword(String value) {
        keychainPassword = value
    }

    @Override
    IOSBuildPluginExtension keychainPassword(String password) {
        setKeychainPassword(password)
        this
    }

    @Override
    String getCertificatePassphrase() {
        certificatePassphrase
    }

    @Override
    void setCertificatePassphrase(String passphrase) {
        certificatePassphrase = passphrase
    }

    @Override
    IOSBuildPluginExtension certificatePassphrase(String passphrase) {
        setCertificatePassphrase(passphrase)
        this
    }

    @Override
    String getAppIdentifier() {
        return applicationIdentifier
    }

    @Override
    void setAppIdentifier(String identifier) {
        applicationIdentifier = identifier
    }

    @Override
    IOSBuildPluginExtension appIdentifier(String identifier) {
        setAppIdentifier(identifier)
        return this
    }

    @Override
    String getTeamId() {
        return teamId
    }

    @Override
    void setTeamId(String id) {
        teamId = id
    }

    @Override
    IOSBuildPluginExtension teamId(String id) {
        setTeamId(id)
        return this
    }

    @Override
    String getScheme() {
        scheme
    }

    @Override
    void setScheme(String scheme) {
        this.scheme = scheme
    }

    @Override
    IOSBuildPluginExtension scheme(String scheme) {
        setScheme(scheme)
        this
    }

    @Override
    String getConfiguration() {
        configuration
    }

    @Override
    void setConfiguration(String configuration) {
        this.configuration = configuration
    }

    @Override
    IOSBuildPluginExtension configuration(String configuration) {
        setConfiguration(configuration)
        this
    }

    DefaultIOSBuildPluginExtension() {
        fastlaneCredentials = new PasswordCredentials()
    }

    class PasswordCredentials implements org.gradle.api.credentials.PasswordCredentials {

        String username
        String password

        PasswordCredentials() {
        }

        PasswordCredentials(String username, String password) {
            this.username = username
            this.password = password
        }

        String toString() {
            String.format("Credentials [username: %s]", this.username)
        }
    }
}
