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

package wooga.gradle.build.unity.ios.tasks

import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.workers.internal.DefaultWorkResult

class XCodeExportTask extends AbstractArchiveTask {

    private Object exportOptionsPlist;
    private Object xcarchivePath;

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        project.files(xcarchivePath, exportOptionsPlist)
    }

    File getExportOptionsPlist() {
        project.files(exportOptionsPlist).getSingleFile()
    }

    void setExportOptionsPlist(Object path) {
        exportOptionsPlist = path
    }

    XCodeExportTask exportOptionsPlist(Object path) {
        setExportOptionsPlist(path)
    }

    File getXcarchivePath() {
        project.files(xcarchivePath).getSingleFile()
    }

    void setXcarchivePath(Object path) {
        xcarchivePath = path
    }

    XCodeExportTask xcarchivePath(Object path) {
        setXcarchivePath(path)
    }

    @Override
    protected CopyAction createCopyAction() {
        def outputPath = new File(this.getDestinationDir(), this.getArchiveName())
        new XCodeExportAction(getTemporaryDir(), outputPath, getExportOptionsPlist(), getXcarchivePath(), project)
    }
}

class XCodeExportAction implements CopyAction {

    File exportPath
    File exportOptionsPlist
    File archivePath
    File outputPath
    Project project

    XCodeExportAction(File exportPath, File outputPath, File exportOptionsPlist, File archivePath, Project project) {
        this.exportPath = exportPath
        this.outputPath = outputPath
        this.exportOptionsPlist = exportOptionsPlist
        this.archivePath = archivePath
        this.project = project
    }

    @Override
    WorkResult execute(CopyActionProcessingStream copyActionProcessingStream) {

        List<String> arguments = new ArrayList<String>()
        arguments << "xcodebuild"
        arguments << "-exportArchive"
        arguments << "-exportPath" << getExportPath().getPath()
        arguments << "-exportOptionsPlist" << getExportOptionsPlist().getPath()
        arguments << "-archivePath" << getArchivePath().getPath()

        def result = project.exec {
            executable "/usr/bin/xcrun"
            args = arguments
            ignoreExitValue = true
        }

        if (result.getExitValue() != 0) {
            return new DefaultWorkResult(false, null)
        }

        project.copy {
            from getExportPath()
            include "*.ipa"
            into outputPath.parent
            it.rename { filename ->
                FilenameUtils.getBaseName(getOutputPath().getPath()) + '.ipa'
            }
        }
    }
}