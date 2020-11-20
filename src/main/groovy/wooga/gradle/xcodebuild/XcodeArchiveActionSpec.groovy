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

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface XcodeArchiveActionSpec<T extends XcodeArchiveActionSpec> extends XcodeArchiveActionSpecBase<T> {

    Provider<Directory> getXcArchivePath()

    Property<String> getConfiguration()

    void setConfiguration(String value)
    void setConfiguration(Provider<String> value)

    T configuration(String value)
    T configuration(Provider<String> value)

    Property<Boolean> getClean()

    void setClean(Boolean value)
    void setClean(Provider<Boolean> value)

    T clean(Boolean value)
    T clean(Provider<Boolean> value)

    Property<String> getScheme()

    void setScheme(String value)
    void setScheme(Provider<String> value)

    T scheme(String value)
    T scheme(Provider<String> value)

    Property<String> getTeamId()

    void setTeamId(String value)
    void setTeamId(Provider<String> value)

    T teamId(String value)
    T teamId(Provider<String> value)

    DirectoryProperty getDerivedDataPath()

    void setDerivedDataPath(File value)
    void setDerivedDataPath(Provider<Directory> value)

    T derivedDataPath(File value)
    T derivedDataPath(Provider<Directory> value)

    RegularFileProperty getBuildKeychain()

    void setBuildKeychain(File value)
    void setBuildKeychain(Provider<RegularFile> value)

    T buildKeychain(File value)
    T buildKeychain(Provider<RegularFile> value)

    DirectoryProperty getProjectPath()

    void setProjectPath(File value)
    void setProjectPath(Provider<Directory> value)

    T projectPath(File value)
    T projectPath(Provider<Directory> value)
}
