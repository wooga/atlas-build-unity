/*
 * Copyright 2017 the original author or authors.
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
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.artifacts.publish.AbstractPublishArtifact
import org.gradle.api.tasks.*

class XCodeExportTask extends DefaultTask {
    private Object archivePath
    private Object exportPath
    private Object exportOptionsPlist

    PublishArtifact getArtifact() {
        new AbstractPublishArtifact(this) {
            @Override
            String getName() {
                return null
            }

            @Override
            String getExtension() {
                return "ipa"
            }

            @Override
            String getType() {
                return "iOS application archive"
            }

            @Override
            String getClassifier() {
                return null
            }

            @Override
            File getFile() {
                return project.fileTree("${project.buildDir}/outputs"){ it.include "*.ipa"}.singleFile
            }

            @Override
            Date getDate() {
                return null
            }
        }
    }

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        project.files(this.archivePath, this.exportOptionsPlist)
    }

    @InputFile
    File getExportOptionsPlist() {
        project.files(exportOptionsPlist).getSingleFile()
    }

    void setExportOptionsPlist(Object path) {
        exportOptionsPlist = path
    }

    XCodeExportTask exportOptionsPlist(Object path) {
        setExportOptionsPlist(path)
    }

    @InputDirectory
    File getArchivePath() {
        project.files(archivePath).getSingleFile()
    }

    void setArchivePath(Object path) {
        archivePath = path
    }

    XCodeExportTask archivePath(Object path) {
        setArchivePath(path)
    }

    @OutputDirectory
    File getExportPath() {
        project.file(exportPath)
    }

    void setExportPath(Object path) {
        exportPath = path
    }

    XCodeExportTask exportPath(Object path) {
        setExportPath(path)
    }

    @TaskAction
    protected void export() {
        List<String> arguments = new ArrayList<String>()
        arguments << "xcodebuild"
        arguments << "-exportArchive"
        arguments << "-exportPath" << getExportPath().getPath()
        arguments << "-exportOptionsPlist" << getExportOptionsPlist().getPath()
        arguments << "-archivePath" << getArchivePath().getPath()

        project.exec {
            executable "/usr/bin/xcrun"
            args = arguments
        }

        project.copy {
            from getExportPath()
            include "*.ipa"
            into project.file("$project.buildDir/outputs")
            it.rename { filename ->
                FilenameUtils.getBaseName(getArchivePath().getPath()) + '.ipa'
            }
        }
    }
}
