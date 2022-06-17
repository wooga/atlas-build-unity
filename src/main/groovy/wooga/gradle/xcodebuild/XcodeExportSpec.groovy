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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile

trait XcodeExportSpec extends BaseSpec {

    private final RegularFileProperty exportOptionsPlist = objects.fileProperty()

    @InputFile
    RegularFileProperty getExportOptionsPlist() {
        exportOptionsPlist
    }

    void setExportOptionsPlist(Provider value) {
        exportOptionsPlist.set(value)
    }

    void setExportOptionsPlist(File value) {
        exportOptionsPlist.set(value)
    }

    private final DirectoryProperty xcArchivePath = objects.directoryProperty()

    @InputDirectory
    DirectoryProperty getXcArchivePath() {
        xcArchivePath
    }

    void setXcArchivePath(Provider value) {
        xcArchivePath.set(value)
    }

    void setXcArchivePath(File value) {
        xcArchivePath.set(value)
    }

    @OutputFile
    abstract Provider<RegularFile> getOutputPath()
}
