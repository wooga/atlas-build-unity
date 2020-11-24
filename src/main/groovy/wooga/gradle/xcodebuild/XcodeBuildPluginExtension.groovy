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

package wooga.gradle.xcodebuild

import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface XcodeBuildPluginExtension<T extends XcodeBuildPluginExtension> {
    DirectoryProperty getLogsDir()

    void setLogsDir(File value)

    void setLogsDir(Provider<Directory> value)

    T logsDir(File value)

    T logsDir(Provider<Directory> value)

    DirectoryProperty getDerivedDataPath()

    void setDerivedDataPath(File value)

    void setDerivedDataPath(Provider<Directory> value)

    T derivedDataPath(File value)

    T derivedDataPath(Provider<Directory> value)

    DirectoryProperty  getXarchivesDir()

    void setXarchivesDir(File value)
    void setXarchivesDir(Provider<Directory> value)

    T xarchivesDir(File value)
    T xarchivesDir(Provider<Directory> value)

    DirectoryProperty getDebugSymbolsDir()

    void setDebugSymbolsDir(File value)
    void setDebugSymbolsDir(Provider<Directory> value)

    T debugSymbolsDir(File value)
    T debugSymbolsDir(Provider<Directory> value)

    Property<ConsoleSettings> getConsoleSettings()

    void setConsoleSettings(ConsoleSettings value)
    void setConsoleSettings(Provider<ConsoleSettings> value)

    T consoleSettings(ConsoleSettings value)
    T consoleSettings(Provider<ConsoleSettings> value)

    T consoleSettings(Closure configuration)
    T consoleSettings(Action<ConsoleSettings> action)
}
