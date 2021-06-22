/*
 * Copyright 2020 Wooga GmbH
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
 */

package wooga.gradle.xcodebuild.internal

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.xcodebuild.ConsoleSettings
import wooga.gradle.xcodebuild.XcodeBuildPluginExtension

import static org.gradle.util.ConfigureUtil.configureUsing

class DefaultXcodeBuildPluginExtension implements XcodeBuildPluginExtension {

    final Project project

    final DirectoryProperty logsDir

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
    DefaultXcodeBuildPluginExtension consoleSettings(ConsoleSettings value) {
        setConsoleSettings(value)
        this
    }

    @Override
    DefaultXcodeBuildPluginExtension consoleSettings(Provider<ConsoleSettings> value) {
        setConsoleSettings(value)
        this
    }

    @Override
    void setLogsDir(File value) {
        logsDir.set(value)
    }

    @Override
    void setLogsDir(Provider<Directory> value) {
        logsDir.set(value)
    }

    @Override
    DefaultXcodeBuildPluginExtension logsDir(File value) {
        setLogsDir(value)
        this
    }

    @Override
    DefaultXcodeBuildPluginExtension logsDir(Provider<Directory> value) {
        setLogsDir(value)
        this
    }

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
    DefaultXcodeBuildPluginExtension derivedDataPath(File value) {
        setDerivedDataPath(value)
        this
    }

    @Override
    DefaultXcodeBuildPluginExtension derivedDataPath(Provider<Directory> value) {
        setDerivedDataPath(value)
        this
    }

    final DirectoryProperty xarchivesDir

    @Override
    void setXarchivesDir(File value) {
        xarchivesDir.set(value)
    }

    @Override
    void setXarchivesDir(Provider<Directory> value) {
        xarchivesDir.set(value)
    }

    @Override
    DefaultXcodeBuildPluginExtension xarchivesDir(File value) {
        setXarchivesDir(value)
        this
    }

    @Override
    DefaultXcodeBuildPluginExtension xarchivesDir(Provider<Directory> value) {
        setXarchivesDir(value)
        this
    }

    final DirectoryProperty debugSymbolsDir

    @Override
    void setDebugSymbolsDir(File value) {
        debugSymbolsDir.set(value)
    }

    @Override
    void setDebugSymbolsDir(Provider<Directory> value) {
        debugSymbolsDir.set(value)
    }

    @Override
    DefaultXcodeBuildPluginExtension debugSymbolsDir(File value) {
        setDebugSymbolsDir(value)
        this
    }

    @Override
    DefaultXcodeBuildPluginExtension debugSymbolsDir(Provider<Directory> value) {
        setDebugSymbolsDir(value)
        this
    }

    @Override
    XcodeBuildPluginExtension consoleSettings(Closure configuration) {
        consoleSettings(configureUsing(configuration))
        this
    }

    @Override
    XcodeBuildPluginExtension consoleSettings(Action action) {
        def settings = consoleSettings.getOrElse(new ConsoleSettings())
        action.execute(settings)
        consoleSettings.set(settings)
        this
    }

    DefaultXcodeBuildPluginExtension(Project project) {
        this.project = project
        logsDir = project.objects.directoryProperty()
        derivedDataPath = project.objects.directoryProperty()
        xarchivesDir = project.objects.directoryProperty()
        debugSymbolsDir = project.objects.directoryProperty()
        consoleSettings = project.objects.property(ConsoleSettings)
    }
}
