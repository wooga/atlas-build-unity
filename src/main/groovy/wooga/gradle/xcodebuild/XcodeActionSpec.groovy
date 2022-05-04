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

package wooga.gradle.xcodebuild

import org.gradle.api.Action
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import wooga.gradle.xcodebuild.config.BuildSettings

interface XcodeActionSpec<T extends XcodeActionSpec> extends XcodeAction {
    @Input
    Provider<List<String>> getBuildArguments()

    @Console
    Property<ConsoleSettings> getConsoleSettings()

    void setConsoleSettings(ConsoleSettings value)
    void setConsoleSettings(Provider<ConsoleSettings> value)

    T consoleSettings(ConsoleSettings value)
    T consoleSettings(Provider<ConsoleSettings> value)

    T consoleSettings(Closure configuration)
    T consoleSettings(Action<ConsoleSettings> action)

    @Internal
    RegularFileProperty getLogFile()

    void setLogFile(File value)
    void setLogFile(Provider<RegularFile> value)

    T logFile(File value)
    T logFile(Provider<RegularFile> value)

    void setAdditionalBuildArguments(Iterable<String> value)
    void setAdditionalBuildArguments(Provider<? extends Iterable<String>> value)

    T buildArgument(String argument)
    T buildArguments(String... arguments)
    T buildArguments(Iterable<String> arguments)

    @Input
    @Optional
    Property<BuildSettings> getBuildSettings()

    void setBuildSettings(BuildSettings value)
    void setBuildSettings(Provider<BuildSettings> value)

    T buildSettings(BuildSettings value)
    T buildSettings(Provider<BuildSettings> value)

    T buildSettings(Closure configuration)
    T buildSettings(Action<BuildSettings> action)
}
