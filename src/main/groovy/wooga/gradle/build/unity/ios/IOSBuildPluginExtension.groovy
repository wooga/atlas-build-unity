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

package wooga.gradle.build.unity.ios

import org.gradle.api.Action
import org.gradle.api.credentials.PasswordCredentials

interface IOSBuildPluginExtension {

    PasswordCredentials getFastlaneCredentials()
    void setFastlaneCredentials(PasswordCredentials cred)

    IOSBuildPluginExtension fastlaneCredentials(Closure configuration)
    IOSBuildPluginExtension fastlaneCredentials(Action<PasswordCredentials> action)
    IOSBuildPluginExtension fastlaneCredentials(PasswordCredentials cred)


    String getKeychainPassword()
    void setKeychainPassword(String value)
    IOSBuildPluginExtension keychainPassword(String password)

    String getCertificatePassphrase()
    void setCertificatePassphrase(String passphrase)
    IOSBuildPluginExtension certificatePassphrase(String passphrase)

    String getAppIdentifier()
    void setAppIdentifier(String identifier)
    IOSBuildPluginExtension appIdentifier(String identifier)

    String getTeamId()
    void setTeamId(String id)
    IOSBuildPluginExtension teamId(String id)

    String getScheme()
    void setScheme(String scheme)
    IOSBuildPluginExtension scheme(String scheme)

    String getConfiguration()
    void setConfiguration(String configuration)
    IOSBuildPluginExtension configuration(String configuration)

    String getProvisioningName()
    void setProvisioningName(String provisioningName)
    IOSBuildPluginExtension provisioningName(String provisioningName)

    Boolean getAdhoc()
    void setAdhoc(Boolean value)
    IOSBuildPluginExtension adhoc(Boolean value)

    Boolean getPublishToTestFlight()
    void setPublishToTestFlight(Boolean value)
    IOSBuildPluginExtension publishToTestFlight(Boolean value)

}
