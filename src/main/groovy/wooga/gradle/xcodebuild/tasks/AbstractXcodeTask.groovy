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
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.XcodeActionSpec
import wooga.gradle.xcodebuild.config.BuildSettings
import wooga.gradle.xcodebuild.internal.XcodeBuildAction

import static org.gradle.util.ConfigureUtil.configureUsing

abstract class AbstractXcodeTask extends DefaultTask implements XcodeActionSpec {

    @Console
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
    AbstractXcodeTask consoleSettings(ConsoleSettings value) {
        setConsoleSettings(value)
        this
    }

    @Override
    AbstractXcodeTask consoleSettings(Provider<ConsoleSettings> value) {
        setConsoleSettings(value)
        this
    }

    @Override
    AbstractXcodeTask consoleSettings(Closure configuration) {
        consoleSettings(configureUsing(configuration))
        this
    }

    @Override
    AbstractXcodeTask consoleSettings(Action<ConsoleSettings> action) {
        def settings = consoleSettings.getOrElse(new ConsoleSettings())
        action.execute(settings)
        consoleSettings.set(settings)
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
    AbstractXcodeTask buildArgument(String argument) {
        additionalBuildArguments.add(argument)
        return this
    }

    @Override
    AbstractXcodeTask buildArguments(String[] arguments) {
        additionalBuildArguments.addAll(project.provider({ arguments.toList() }))
        return this
    }

    @Override
    AbstractXcodeTask buildArguments(Iterable arguments) {
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
    AbstractXcodeTask buildSettings(BuildSettings value) {
        setBuildSettings(value)
        this
    }

    @Override
    AbstractXcodeTask buildSettings(Provider<BuildSettings> value) {
        setBuildSettings(value)
        this
    }

    @Override
    AbstractXcodeTask buildSettings(Closure configuration) {
        buildSettings(configureUsing(configuration))
        this
    }

    @Internal
    final RegularFileProperty logFile

    @Override
    void setLogFile(File value) {
        logFile.set(value)
    }

    @Override
    void setLogFile(Provider<RegularFile> value) {
        logFile.set(value)
    }

    @Override
    AbstractXcodeTask logFile(File value) {
        setLogFile(value)
        this
    }

    @Override
    AbstractXcodeTask logFile(Provider<RegularFile> value) {
        setLogFile(value)
        this
    }

    @Override
    AbstractXcodeTask buildSettings(Action<BuildSettings> action) {
        def settings = buildSettings.getOrElse(new BuildSettings())
        action.execute(settings)
        buildSettings.set(settings)
        this
    }

    AbstractXcodeTask() {
        additionalBuildArguments = project.objects.listProperty(String)
        buildSettings = project.objects.property(BuildSettings)
        consoleSettings = project.objects.property(ConsoleSettings)
        logFile = project.layout.fileProperty()
    }

    @TaskAction
    protected void exec() {
        def action = new XcodeBuildAction(project, buildArguments.get(), logFile.get().asFile, consoleSettings.get())
        action.exec()
    }
}
