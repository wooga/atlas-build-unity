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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskDependency
import wooga.gradle.xcodebuild.XcodeExportActionSpec
import wooga.gradle.xcodebuild.config.BuildSettings

class ExportArchive extends AbstractXcodeArchiveTask implements XcodeExportActionSpec {

    protected final PublishArtifact internalPublishArtifact

    PublishArtifact getPublishArtifact() {
        internalPublishArtifact
    }

    private final RegularFileProperty exportOptionsPlist

    @InputFile
    RegularFileProperty getExportOptionsPlist() {
        exportOptionsPlist
    }

    @Override
    void setExportOptionsPlist(Provider value) {
        exportOptionsPlist.set(value)
    }

    @Override
    void setExportOptionsPlist(File value) {
        exportOptionsPlist.set(value)
    }

    @Override
    ExportArchive exportOptionsPlist(Provider value) {
        setExportOptionsPlist(value)
        this
    }

    @Override
    ExportArchive exportOptionsPlist(File value) {
        setExportOptionsPlist(value)
        this
    }

    private final DirectoryProperty xcArchivePath

    @InputDirectory
    DirectoryProperty getXcArchivePath() {
        xcArchivePath
    }

    @Override
    void setXcArchivePath(Provider value) {
        xcArchivePath.set(value)
    }

    @Override
    void setXcArchivePath(File value) {
        xcArchivePath.set(value)
    }

    @Override
    ExportArchive xcArchivePath(Provider value) {
        setXcArchivePath(value)
        this
    }

    @Override
    ExportArchive xcArchivePath(File value) {
        setXcArchivePath(value)
        this
    }

    private final Provider<RegularFile> outputPath

    @OutputFile
    Provider<RegularFile> getOutputPath() {
        outputPath
    }

    private final Provider<List<String>> buildArguments

    @Input
    Provider<List<String>> getBuildArguments() {
        buildArguments
    }

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        project.files(xcArchivePath, exportOptionsPlist)
    }

    ExportArchive() {
        super()
        exportOptionsPlist = project.objects.fileProperty()
        xcArchivePath = project.objects.directoryProperty()

        outputPath = destinationDir.file(archiveName)
        buildArguments = project.provider({
            List<String> arguments = new ArrayList<String>()
            BuildSettings buildSettings = buildSettings.getOrElse(BuildSettings.EMPTY)
            arguments << "xcodebuild"
            arguments << "-exportArchive"
            arguments << "-exportPath" << temporaryDir.path
            arguments << "-exportOptionsPlist" << exportOptionsPlist.get().asFile.path
            arguments << "-archivePath" << xcArchivePath.get().asFile.path

            if (additionalBuildArguments.present) {
                additionalBuildArguments.get().each {
                    arguments << it
                }
            }

            arguments.addAll(buildSettings.toList())
            arguments
        })

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
