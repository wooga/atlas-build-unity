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

import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.xcodebuild.XcodeArchiveActionSpec
import wooga.gradle.xcodebuild.XcodeBuildAction
import wooga.gradle.xcodebuild.XcodeBuildPluginConventions
import wooga.gradle.xcodebuild.config.BuildSettings

class XcodeArchive extends AbstractXcodeArchiveTask implements XcodeArchiveActionSpec {

    @Optional
    @Input
    final Property<String> configuration

    @Override
    void setConfiguration(String value) {
        configuration.set(value)
    }

    @Override
    void setConfiguration(Provider<String> value) {
        configuration.set(value)
    }

    @Override
    XcodeArchive configuration(String value) {
        setConfiguration(value)
        this
    }

    @Override
    XcodeArchive configuration(Provider<String> value) {
        setConfiguration(value)
        this
    }

    @Input
    @Optional
    final Property<Boolean> clean

    @Override
    void setClean(Boolean value) {
        clean.set(value)
    }

    @Override
    void setClean(Provider<Boolean> value) {
        clean.set(value)
    }

    @Override
    XcodeArchive clean(Boolean value) {
        setClean(value)
        this
    }

    @Override
    XcodeArchive clean(Provider<Boolean> value) {
        setClean(value)
        this
    }

    @Input
    final Property<String> scheme

    @Override
    void setScheme(String value) {
        scheme.set(value)
    }

    @Override
    void setScheme(Provider<String> value) {
        scheme.set(value)
    }

    @Override
    XcodeArchive scheme(String value) {
        setScheme(value)
        this
    }

    @Override
    XcodeArchive scheme(Provider<String> value) {
        setScheme(value)
        this
    }

    @Optional
    @Input
    final Property<String> teamId

    @Override
    void setTeamId(String value) {
        teamId.set(value)
    }

    @Override
    void setTeamId(Provider<String> value) {
        teamId.set(value)
    }

    @Override
    XcodeArchive teamId(String value) {
        setTeamId(value)
        this
    }

    @Override
    XcodeArchive teamId(Provider<String> value) {
        setTeamId(value)
        this
    }

    @Internal
    final DirectoryProperty derivedDataPath

    @Override
    void setDerivedDataPath(File value) {
        derivedDataPath.set(value)
    }

    @Override
    void setDerivedDataPath(Provider<Directory> value) {
        derivedDataPath.set(value)
    }

    @Override
    XcodeArchive derivedDataPath(File value) {
        setDerivedDataPath(value)
        this
    }

    @Override
    XcodeArchive derivedDataPath(Provider<Directory> value) {
        setDerivedDataPath(value)
        this
    }

    final DirectoryProperty projectPath

    @SkipWhenEmpty
    @InputDirectory
    DirectoryProperty getProjectPath() {
        projectPath
    }

    @InputFiles
    protected getInputFiles() {
        project.files(projectPath)
    }

    @Override
    void setProjectPath(File value) {
        projectPath.set(value)
    }

    @Override
    void setProjectPath(Provider<Directory> value) {
        projectPath.set(value)
    }

    @Override
    XcodeArchive projectPath(File value) {
        setProjectPath(value)
        this
    }

    @Override
    XcodeArchive projectPath(Provider<Directory> value) {
        setProjectPath(value)
        this
    }

    @Optional
    @InputFile
    final RegularFileProperty buildKeychain

    @Override
    void setBuildKeychain(File value) {
        buildKeychain.set(value)
    }

    @Override
    void setBuildKeychain(Provider<RegularFile> value) {
        buildKeychain.set(value)
    }

    @Override
    XcodeArchive buildKeychain(File value) {
        setBuildKeychain(value)
        this
    }

    @Override
    XcodeArchive buildKeychain(Provider<RegularFile> value) {
        setBuildKeychain(value)
        this
    }

    @OutputDirectory
    final Provider<Directory> xcArchivePath

    @Input
    final Provider<List<String>> buildArguments

    XcodeArchive() {
        super()
        configuration = project.objects.property(String)
        scheme = project.objects.property(String)
        teamId = project.objects.property(String)
        clean = project.objects.property(Boolean)

        projectPath = project.objects.directoryProperty()
        derivedDataPath = project.objects.directoryProperty()
        buildKeychain = project.objects.fileProperty()

        xcArchivePath = destinationDir.map(new Transformer<Directory, Directory>() {
            @Override
            Directory transform(Directory directory) {
                directory.dir(archiveName.get())
            }
        })

        buildArguments = project.provider({
            List<String> arguments = new ArrayList<String>()
            BuildSettings settings = buildSettings.getOrElse(BuildSettings.EMPTY).clone()
            arguments << "xcodebuild"

            if (clean.present && clean.get()) {
                arguments << XcodeBuildAction.clean.toString()
            }

            arguments << XcodeBuildAction.archive.toString()

            def projectOrWorkspace = projectPath.get().asFile

            if (projectOrWorkspace.name.endsWith(".xcworkspace")) {
                arguments << "-workspace" << projectOrWorkspace.getPath()
            } else if (projectOrWorkspace.name.endsWith(".xcodeproj")) {
                arguments << "-project" << projectOrWorkspace.getPath()
            } else {
                throw new ScriptException(XcodeBuildPluginConventions.INVALID_XCODE_PROJECT_ERROR_MESSAGE)
            }

            arguments << "-scheme" << scheme.get()

            if (configuration.present) {
                arguments << "-configuration" << configuration.get()
            }

            if (buildKeychain.present) {
                settings.otherCodeSignFlags("--keychain", buildKeychain.get().asFile.path)
            }

            if (teamId.present) {
                settings.developmentTeam(teamId.get())
            }

            arguments << "-archivePath" << xcArchivePath.get().asFile.path

            if (derivedDataPath.present) {
                arguments << "-derivedDataPath" << derivedDataPath.get().asFile.path
            }

            if (additionalBuildArguments.present) {
                additionalBuildArguments.get().each {
                    arguments << it
                }
            }

            arguments.addAll(settings.toList())
            arguments
        })
    }
}
