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
 *
 *
 *
 */

package wooga.gradle.xcodebuild

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty

trait XcodeArchiveActionSpec extends BaseSpec {

    final Property<String> configuration = objects.property(String)

    @Optional
    @Input
    Property<String> getConfiguration() {
        configuration
    }

    void setConfiguration(String value) {
        configuration.set(value)
    }

    void setConfiguration(Provider<String> value) {
        configuration.set(value)
    }

    final Property<Boolean> clean = objects.property(Boolean)

    @Input
    @Optional
    Property<Boolean> getClean() {
        clean
    }

    void setClean(Boolean value) {
        clean.set(value)
    }

    void setClean(Provider<Boolean> value) {
        clean.set(value)
    }

    final Property<String> scheme = objects.property(String)

    @Input
    Property<String> getScheme() {
        scheme
    }

    void setScheme(String value) {
        scheme.set(value)
    }

    void setScheme(Provider<String> value) {
        scheme.set(value)
    }

    final Property<String> teamId = objects.property(String)

    @Optional
    @Input
    Property<String> getTeamId() {
        teamId
    }

    void setTeamId(String value) {
        teamId.set(value)
    }

    void setTeamId(Provider<String> value) {
        teamId.set(value)
    }

    final DirectoryProperty derivedDataPath = objects.directoryProperty()

    @Internal
    DirectoryProperty getDerivedDataPath() {
        derivedDataPath
    }

    void setDerivedDataPath(File value) {
        derivedDataPath.set(value)
    }

    void setDerivedDataPath(Provider<Directory> value) {
        derivedDataPath.set(value)
    }

    final RegularFileProperty buildKeychain = objects.fileProperty()

    @Optional
    @InputFile
    RegularFileProperty getBuildKeychain() {
        buildKeychain
    }

    void setBuildKeychain(File value) {
        buildKeychain.set(value)
    }

    void setBuildKeychain(Provider<RegularFile> value) {
        buildKeychain.set(value)
    }

    final DirectoryProperty projectPath = objects.directoryProperty()

    @SkipWhenEmpty
    @InputDirectory
    DirectoryProperty getProjectPath() {
        projectPath
    }

    void setProjectPath(File value) {
        projectPath.set(value)
    }

    void setProjectPath(Provider<Directory> value) {
        projectPath.set(value)
    }
}
