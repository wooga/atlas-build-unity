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

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.util.GUtil
import wooga.gradle.xcodebuild.XcodeArchiveActionSpec
import wooga.gradle.xcodebuild.XcodeBuildAction
import wooga.gradle.xcodebuild.XcodeBuildPluginConsts
import wooga.gradle.xcodebuild.config.BuildSettings

class XcodeArchive extends AbstractXcodeTask implements XcodeArchiveActionSpec {

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
    final Provider<Set<XcodeBuildAction>> buildActions

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

    @InputDirectory
    final DirectoryProperty projectPath

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

    @SkipWhenEmpty
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
    final Property<String> archiveName

    @Override
    void setArchiveName(String value) {
        archiveName.set(value)
    }

    @Override
    void setArchiveName(Provider<String> value) {
        archiveName.set(value)
    }

    @Override
    XcodeArchive archiveName(String value) {
        setArchiveName(value)
        this
    }

    @Override
    XcodeArchive archiveName(Provider<String> value) {
        setArchiveName(value)
        this
    }

    final Property<String> baseName

    @Override
    void setBaseName(String value) {
        baseName.set(value)
    }

    @Override
    void setBaseName(Provider<String> value) {
        baseName.set(value)
    }

    @Override
    XcodeArchive baseName(String value) {
        setBaseName(value)
        this
    }

    @Override
    XcodeArchive baseName(Provider<String> value) {
        setBaseName(value)
        this
    }

    final Property<String> appendix

    @Override
    void setAppendix(String value) {
        appendix.set(value)
    }

    @Override
    void setAppendix(Provider<String> value) {
        appendix.set(value)
    }

    @Override
    XcodeArchive appendix(String value) {
        setAppendix(value)
        this
    }

    @Override
    XcodeArchive appendix(Provider<String> value) {
        setAppendix(value)
        this
    }

    final Property<String> version

    @Override
    void setVersion(String value) {
        version.set(value)
    }

    @Override
    void setVersion(Provider<String> value) {
        version.set(value)
    }

    @Override
    XcodeArchive version(String value) {
        setVersion(value)
        this
    }

    @Override
    XcodeArchive version(Provider<String> value) {
        setVersion(value)
        this
    }


    final Property<String> extension

    @Override
    void setExtension(String value) {
        extension.set(value)
    }

    @Override
    void setExtension(Provider<String> value) {
        extension.set(value)
    }

    @Override
    XcodeArchive extension(String value) {
        setExtension(value)
        this
    }

    @Override
    XcodeArchive extension(Provider<String> value) {
        setExtension(value)
        this
    }

    final Property<String> classifier

    @Override
    void setClassifier(String value) {
        classifier.set(value)
    }

    @Override
    void setClassifier(Provider<String> value) {
        classifier.set(value)
    }

    @Override
    XcodeArchive classifier(String value) {
        setClassifier(value)
        this
    }

    @Override
    XcodeArchive classifier(Provider<String> value) {
        setClassifier(value)
        this
    }

    final DirectoryProperty destinationDir

    @Override
    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    @Override
    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }

    @Override
    XcodeArchive destinationDir(File value) {
        setDestinationDir(value)
        this
    }

    @Override
    XcodeArchive destinationDir(Provider<Directory> value) {
        setDestinationDir(value)
        this
    }

    @Input
    final Provider<List<String>> buildArguments

    XcodeArchive() {
        super()
        configuration = project.objects.property(String)
        scheme = project.objects.property(String)
        teamId = project.objects.property(String)
        clean = project.objects.property(Boolean)

        projectPath = project.layout.directoryProperty()
        derivedDataPath = project.layout.directoryProperty()
        buildKeychain = project.layout.fileProperty()

        buildActions = project.provider({
            def s = new HashSet<XcodeBuildAction>()
            s << XcodeBuildAction.archive

            if (clean.present && clean.get()) {
                s << XcodeBuildAction.clean
            }
            s
        })

        baseName = project.objects.property(String)
        appendix = project.objects.property(String)
        version = project.objects.property(String)
        extension = project.objects.property(String)
        classifier = project.objects.property(String)

        archiveName = project.objects.property(String)
        archiveName.set(project.provider({
            String name = baseName.getOrElse("") + maybe(baseName.getOrElse(""), appendix)
            name += maybe(name, version)
            name += maybe(name, classifier)
            name += extension.isPresent() && extension.get() != "" ? "." + extension.get() : ""
            name
        }))

        destinationDir = project.layout.directoryProperty()
        xcArchivePath = archiveName.map({ destinationDir.get().dir(it) })

        buildArguments = project.provider({
            List<String> arguments = new ArrayList<String>()
            BuildSettings settings = buildSettings.getOrElse(new BuildSettings())
            arguments << "xcodebuild"

            buildActions.get().each {
                arguments << it.toString()
            }

            def projectOrWorkspace = projectPath.get().asFile

            if (projectOrWorkspace.name.endsWith(".xcworkspace")) {
                arguments << "-workspace" << projectOrWorkspace.getPath()
            } else if (projectOrWorkspace.name.endsWith(".xcodeproj")) {
                arguments << "-project" << projectOrWorkspace.getPath()
            } else {
                throw new ScriptException(XcodeBuildPluginConsts.INVALID_XCODE_PROJECT_ERROR_MESSAGE)
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

            arguments
        })
    }

    protected static String maybe(String prefix, Provider<String> value) {
        if (value.isPresent() && value.get().size() > 0) {
            if (GUtil.isTrue(prefix)) {
                return "-".concat(value.get())
            } else {
                return value.get()
            }
        }
        return ""
    }
}
