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


import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider

trait XcodeBuildPluginExtension extends XcodeSpec {

    private final DirectoryProperty logsDir = objects.directoryProperty()

    DirectoryProperty getLogsDir() {
        logsDir
    }

    void setLogsDir(File value) {
        logsDir.set(value)
    }

    void setLogsDir(Provider<Directory> value) {
        logsDir.set(value)
    }

    final DirectoryProperty derivedDataPath = objects.directoryProperty()

    DirectoryProperty getDerivedDataPath() {
        derivedDataPath
    }

    void setDerivedDataPath(File value) {
        derivedDataPath.set(value)
    }

    void setDerivedDataPath(Provider<Directory> value) {
        derivedDataPath.set(value)
    }

    final DirectoryProperty xarchivesDir = objects.directoryProperty()

    DirectoryProperty getXarchivesDir() {
        xarchivesDir
    }

    void setXarchivesDir(File value) {
        xarchivesDir.set(value)
    }

    void setXarchivesDir(Provider<Directory> value) {
        xarchivesDir.set(value)
    }

    final DirectoryProperty debugSymbolsDir = objects.directoryProperty()

    DirectoryProperty getDebugSymbolsDir() {
        debugSymbolsDir
    }

    void setDebugSymbolsDir(File value) {
        debugSymbolsDir.set(value)
    }

    void setDebugSymbolsDir(Provider<Directory> value) {
        debugSymbolsDir.set(value)
    }
}
