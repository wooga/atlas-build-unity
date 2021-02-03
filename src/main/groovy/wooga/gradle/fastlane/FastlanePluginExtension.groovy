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

package wooga.gradle.fastlane

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface FastlanePluginExtension<T extends FastlanePluginExtension> {
    Property<String> getUsername()

    void setUsername(String value)
    void setUsername(Provider<String> value)

    T username(String value)
    T username(Provider<String> value)

    Property<String> getPassword()

    void setPassword(String value)
    void setPassword(Provider<String> value)

    T password(String value)
    T password(Provider<String> value)

    RegularFileProperty getApiKeyPath()

    void setApiKeyPath(File value)
    void setApiKeyPath(Provider<RegularFile> value)

    T apiKeyPath(File value)
    T apiKeyPath(Provider<RegularFile> value)
}
