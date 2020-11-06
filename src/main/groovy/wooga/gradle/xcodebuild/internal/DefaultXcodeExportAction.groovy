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

package wooga.gradle.xcodebuild.internal

import groovy.transform.InheritConstructors
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.WorkResult
import org.gradle.workers.internal.DefaultWorkResult
import wooga.gradle.xcodebuild.ConsoleSettings

@InheritConstructors
class DefaultXcodeExportAction extends XcodeBuildAction implements CopyAction {

    final Provider<Directory> exportPath
    final Provider<Directory> outputPath

    DefaultXcodeExportAction(Project project, Provider<ConsoleSettings> consoleSettings, Provider<List<String>> buildArguments, Provider<RegularFile> logFile, Provider<Directory> exportPath, Provider<Directory> outputPath) {
        super(project, buildArguments, logFile, consoleSettings)
        this.exportPath = exportPath
        this.outputPath = outputPath
    }

    @Override
    WorkResult execute(CopyActionProcessingStream copyActionProcessingStream) {
        def result = exec()
        if (result.getExitValue() != 0) {
            return new DefaultWorkResult(false, null)
        }

        project.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec copySpec) {
                copySpec.with {
                    from exportPath.get().asFile.path
                    include "*.ipa"
                    into outputPath.parent
                    it.rename { filename ->
                        FilenameUtils.getBaseName(outputPath.get().asFile.path) + '.ipa'
                    }
                }
            }
        })
    }
}
