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
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.XcodeExportActionSpec
import wooga.gradle.xcodebuild.internal.BuildSettings
import wooga.gradle.xcodebuild.internal.DefaultXcodeExportAction

import static org.gradle.util.ConfigureUtil.configureUsing

class XCodeExport extends AbstractArchiveTask implements XcodeExportActionSpec {

    final RegularFileProperty exportOptionsPlist

    @Override
    void setExportOptionsPlist(Provider value) {
        exportOptionsPlist.set(value)
    }

    @Override
    void setExportOptionsPlist(File value) {
        exportOptionsPlist.set(value)
    }

    @Override
    XCodeExport exportOptionsPlist(Provider value) {
        setExportOptionsPlist(value)
        this
    }

    @Override
    XCodeExport exportOptionsPlist(File value) {
        setExportOptionsPlist(value)
        this
    }

    final DirectoryProperty xcArchivePath

    @Override
    void setXcArchivePath(Provider value) {
        xcArchivePath.set(value)
    }

    @Override
    void setXcArchivePath(File value) {
        xcArchivePath.set(value)
    }

    @Override
    XCodeExport xcArchivePath(Provider value) {
        setXcArchivePath(value)
        this
    }

    @Override
    XCodeExport xcArchivePath(File value) {
        setXcArchivePath(value)
        this
    }

    final Provider<Directory> exportPath
    final Provider<Directory> outputPath

    @Internal
    final Property<ConsoleSettings> consoleSettings

    @Override
    void setConsoleSettings(ConsoleSettings value) {
        consoleSettings.set(value)
    }

    @Override
    void setConsoleSettings(Provider<ConsoleSettings> value) {
        consoleSettings.set(value)
    }

    @Override
    XCodeExport consoleSettings(ConsoleSettings value) {
        setConsoleSettings(value)
        this
    }

    @Override
    XCodeExport consoleSettings(Provider<ConsoleSettings> value) {
        setConsoleSettings(value)
        this
    }

    @Override
    XCodeExport consoleSettings(Closure configuration) {
        consoleSettings(configureUsing(configuration))
        this
    }

    @Override
    XCodeExport consoleSettings(Action<ConsoleSettings> action) {
        def printSettings = consoleSettings.getOrElse(new ConsoleSettings())
        action.execute(printSettings)
        consoleSettings.set(printSettings)
        this
    }

    final ListProperty<String> additionalBuildArguments

    @Override
    void setAdditionalBuildArguments(Iterable<String> value) {
        additionalBuildArguments.set(value)
    }

    @Override
    void setAdditionalBuildArguments(Provider<? extends Iterable<String>> value) {
        additionalBuildArguments.set(value)
    }

    @Override
    XCodeExport buildArgument(String argument) {
        additionalBuildArguments.add(argument)
        return this
    }

    @Override
    XCodeExport buildArguments(String[] arguments) {
        additionalBuildArguments.addAll(project.provider({ arguments.toList() }))
        return this
    }

    @Override
    XCodeExport buildArguments(Iterable arguments) {
        additionalBuildArguments.addAll(project.provider({ arguments }))
        return this
    }

    final Property<BuildSettings> buildSettings

    @Override
    void setBuildSettings(BuildSettings value) {
        buildSettings.set(value)
    }

    @Override
    void setBuildSettings(Provider<BuildSettings> value) {
        buildSettings.set(value)
    }

    @Override
    XCodeExport buildSettings(BuildSettings value) {
        setBuildSettings(value)
        this
    }

    @Override
    XCodeExport buildSettings(Provider<BuildSettings> value) {
        setBuildSettings(value)
        this
    }

    @Override
    XCodeExport buildSettings(Closure configuration) {
        buildSettings(configureUsing(configuration))
        this
    }

    @Override
    XCodeExport buildSettings(Action<BuildSettings> action) {
        def settings = buildSettings.getOrElse(new BuildSettings())
        action.execute(settings)
        buildSettings.set(settings)
        this
    }

    final RegularFileProperty logFile

    @Input
    final Provider<List<String>> buildArguments

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        project.files(xcArchivePath, exportOptionsPlist)
    }

    XCodeExport() {
        exportOptionsPlist = project.layout.fileProperty()
        xcArchivePath = project.layout.directoryProperty()

        consoleSettings = project.objects.property(ConsoleSettings)
        Provider<Directory> logsDir = project.layout.buildDirectory.dir("logs")

        logFile = project.layout.fileProperty()
        logFile.set(logsDir.map(new Transformer<RegularFile, Directory>() {
            @Override
            RegularFile transform(Directory directory) {
                directory.file("${name}.log")
            }
        }))

        outputPath = project.provider({
            DirectoryProperty d = project.layout.directoryProperty()
            d.set(new File(this.getDestinationDir(), this.getArchiveName()))
            d
        }) as Provider<Directory>

        exportPath = project.provider({
            DirectoryProperty d = project.layout.directoryProperty()
            d.set(temporaryDir)
            d
        }) as Provider<Directory>

        additionalBuildArguments = project.objects.listProperty(String)
        buildSettings = project.objects.property(BuildSettings)

        buildArguments = project.provider({
            List<String> arguments = new ArrayList<String>()
            BuildSettings buildSettings = buildSettings.get()
            arguments << "xcodebuild"
            arguments << "-exportArchive"
            arguments << "-exportPath" << exportPath.get().asFile.path
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
    }

    @Override
    protected CopyAction createCopyAction() {
        new DefaultXcodeExportAction(project, consoleSettings, buildArguments, logFile, exportPath, outputPath)
    }
}
