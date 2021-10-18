/*
 * Copyright 2018-2020 Wooga GmbH
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

package wooga.gradle.build.unity

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

interface UnityBuildPluginExtension<T extends UnityBuildPluginExtension> {

    DirectoryProperty getAppConfigsDirectory()
    DirectoryProperty getOutputDirectoryBase()
    Property<String> getToolsVersion()
    Property<String> getVersion()
    Property<String> getVersionCode()
    Property<String> getCommitHash()
    Property<String> getExportMethodName()
    Property<String> getDefaultAppConfigName()
    MapProperty<String, String> getCustomArguments()
    RegularFileProperty getExportInitScript()
    Property<File> getExportBuildDirBase()
    Property<File> getExportBuildScript()
    Property<File> getExportTestScript()
    Property<File> getExportPublishScript()
    Property<Boolean> getCleanBuildDirBeforeBuild()
    FileCollection getAppConfigs()
    DirectoryProperty getAssetsDir()

    ConfigurableFileCollection getIgnoreFilesForExportUpToDateCheck()

    Property<String> getAppConfigSecretsKey()
    void setAppConfigSecretsKey(String key)
    T appConfigSecretsKey(String key)

}
