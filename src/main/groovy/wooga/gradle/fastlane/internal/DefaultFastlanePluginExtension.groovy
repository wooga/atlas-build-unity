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

package wooga.gradle.fastlane.internal


import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.fastlane.FastlanePluginExtension

class DefaultFastlanePluginExtension implements FastlanePluginExtension {

    private final Project project

    DefaultFastlanePluginExtension(Project project) {
        this.project = project

        username = project.objects.property(String)
        password = project.objects.property(String)
        apiKeyPath = project.layout.fileProperty()
    }

    final Property<String> username

    @Override
    void setUsername(String value) {
        username.set(value)
    }

    @Override
    void setUsername(Provider<String> value) {
        username.set(value)
    }

    @Override
    FastlanePluginExtension username(String value) {
        setUsername(value)
        this
    }

    @Override
    FastlanePluginExtension username(Provider<String> value) {
        setUsername(value)
        this
    }

    final Property<String> password

    @Override
    void setPassword(String value) {
        password.set(value)
    }

    @Override
    void setPassword(Provider<String> value) {
        password.set(value)
    }

    @Override
    FastlanePluginExtension password(String value) {
        setPassword(value)
        this
    }

    @Override
    FastlanePluginExtension password(Provider<String> value) {
        setPassword(value)
        this
    }

    final RegularFileProperty apiKeyPath

    @Override
    void setApiKeyPath(File value) {
        apiKeyPath.set(value)
    }

    @Override
    void setApiKeyPath(Provider<RegularFile> value) {
        apiKeyPath.set(value)
    }

    @Override
    FastlanePluginExtension apiKeyPath(File value) {
        setApiKeyPath(value)
        this
    }

    @Override
    FastlanePluginExtension apiKeyPath(Provider<RegularFile> value) {
        setApiKeyPath(value)
        this
    }
}
