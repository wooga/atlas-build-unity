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
import org.gradle.api.provider.Provider

interface XcodeExportActionSpec<T extends XcodeExportActionSpec> extends XcodeActionSpec<T>, XcodeArchiveActionSpecBase<T> {
    RegularFileProperty getExportOptionsPlist()

    void setExportOptionsPlist(Provider<RegularFile> value)
    void setExportOptionsPlist(File value)

    T exportOptionsPlist(Provider<RegularFile> value)
    T exportOptionsPlist(File value)

    DirectoryProperty getXcArchivePath()

    void setXcArchivePath(Provider<Directory> value)
    void setXcArchivePath(File value)

    T xcArchivePath(Provider<Directory> value)
    T xcArchivePath(File value)
}
