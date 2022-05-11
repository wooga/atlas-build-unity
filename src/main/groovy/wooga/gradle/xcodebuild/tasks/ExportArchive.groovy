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

package wooga.gradle.xcodebuild.tasks

import org.gradle.api.Action
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.file.*
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.internal.tasks.TaskResolver
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskDependency
import wooga.gradle.xcodebuild.XcodeExportSpec
import wooga.gradle.xcodebuild.config.BuildSettings

class ExportArchive extends AbstractXcodeArchiveTask implements XcodeExportSpec {

    protected final PublishArtifact internalPublishArtifact

    @Internal
    PublishArtifact getPublishArtifact() {
        internalPublishArtifact
    }

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        project.files(xcArchivePath, exportOptionsPlist)
    }

    Provider<RegularFile> outputPath = destinationDir.file(archiveName)

    ExportArchive() {
        super()
        setInternalArguments(project.provider({
            List<String> arguments = new ArrayList<String>()
            BuildSettings buildSettings = buildSettings.getOrElse(BuildSettings.EMPTY)
            arguments << "xcodebuild"
            arguments << "-exportArchive"
            arguments << "-exportPath" << temporaryDir.path
            arguments << "-exportOptionsPlist" << exportOptionsPlist.get().asFile.path
            arguments << "-archivePath" << xcArchivePath.get().asFile.path

            arguments.addAll(buildSettings.toList())
            arguments
        }))

        internalPublishArtifact = new IPAPublishArtifact(this)
    }

    @Override
    protected void exec() {
        super.exec()

        project.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec copySpec) {
                copySpec.with {
                    from temporaryDir.path
                    include "*.ipa"
                    into destinationDir.get().asFile
                    it.rename { filename ->
                        archiveName.get()
                    }
                }
            }
        })
    }

    private class IPAPublishArtifact implements PublishArtifact {

        @Override
        String getName() {
            exportTask.archiveName.get()
        }

        @Override
        String getExtension() {
            "ipa"
        }

        @Override
        String getType() {
            "zip"
        }

        @Override
        String getClassifier() {
            exportTask.classifier.getOrNull()
        }

        @Override
        File getFile() {
            exportTask.outputPath.get().asFile
        }

        @Override
        Date getDate() {
            null
        }

        def taskDependency

        @Override
        TaskDependency getBuildDependencies() {
            taskDependency
        }

        private final ExportArchive exportTask

        IPAPublishArtifact(ExportArchive exportTask) {
            this.exportTask = exportTask
            taskDependency = new DefaultTaskDependency(exportTask.project.tasks as TaskResolver)
            taskDependency.add(exportTask)
        }
    }
}
