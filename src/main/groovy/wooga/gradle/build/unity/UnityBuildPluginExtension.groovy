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
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import wooga.gradle.build.unity.models.UBSCompatibility
import wooga.gradle.build.unity.models.UnityBuildSpec
import wooga.gradle.build.unity.models.VersionSpec

trait UnityBuildPluginExtension<T extends UnityBuildPluginExtension> extends UnityBuildSpec implements VersionSpec, UBSCompatibility {

    /**
     * @return The method to have Unity execute
     */
    @Input
    Property<String> getExportMethodName() {
        exportMethodName
    }

    private final Property<String> exportMethodName = objects.property(String)

    void setExportMethodName(String unityMethodName) {
        this.exportMethodName.set(unityMethodName)
    }

    /**
     * @return The directory where Configs are located
     */
    DirectoryProperty getConfigsDirectory() {
        configsDirectory
    }

    private final DirectoryProperty configsDirectory = objects.directoryProperty()

    /**
     * @return The base of the output direectory
     */
    DirectoryProperty getOutputDirectoryBase() {
        outputDirectoryBase
    }

    private final DirectoryProperty outputDirectoryBase = objects.directoryProperty()


    /**
     * @return The name of the default configuration
     */
    Property<String> getDefaultConfigName() {
        defaultConfigName
    }

    private final Property<String> defaultConfigName = objects.property(String)

    // TODO: Explain
    /**
     * @return The script for... exports?
     */
    RegularFileProperty getExportInitScript() {
        exportInitScript
    }

    private final RegularFileProperty exportInitScript = objects.fileProperty()

    /**
     * @return The directory for exports
     */
    Property<File> getExportBuildDirBase() {
        exportBuildDirBase
    }

    private final Property<File> exportBuildDirBase = objects.property(File)

    /**
     * @return Whether the build direectory should be cleaned before each build
     */
    Property<Boolean> getCleanBuildDirBeforeBuild() {
        cleanBuildDirBeforeBuild
    }

    private final Property<Boolean> cleanBuildDirBeforeBuild = objects.property(Boolean)

    /**
     * @return WHether to skip exporting after a build
     */
    Property<Boolean> getSkipExport() {
        skipExport
    }

    private final Property<Boolean> skipExport = objects.property(Boolean)

    /**
     * @return The directory where assets are located
     */
    DirectoryProperty getAssetsDir() {
        assetsDir
    }

    private final DirectoryProperty assetsDir = objects.directoryProperty()

    // TODO: Explain
    /**
     * @return Files to ignore when checking whether...
     */
    ConfigurableFileCollection getIgnoreFilesForExportUpToDateCheck() {
        ignoreFilesForExportUpToDateCheck
    }

    private final ConfigurableFileCollection ignoreFilesForExportUpToDateCheck = objects.fileCollection()

    /**
     * @return The key to use for resolving secrets found in a configuration
     */
    Property<String> getConfigSecretsKey() {
        return configSecretsKey
    }

    private final Property<String> configSecretsKey = objects.property(String)

    void setConfigSecretsKey(String key) {
        configSecretsKey.set(key)
    }

    T configSecretsKey(String key) {
        configSecretsKey.set(key)
        this
    }

    /**
     * @return All found configurations in the project
     */
    FileCollection getConfigs() {
        null
    }
}
