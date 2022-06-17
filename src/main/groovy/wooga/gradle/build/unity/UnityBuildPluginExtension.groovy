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

import com.wooga.gradle.BaseSpec
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

trait UnityBuildSpec implements BaseSpec {

    private final Property<String> version = objects.property(String)

    @Input
    Property<String> getVersion() {
        version
    }

    private final Property<String> versionCode = objects.property(String)

    @Optional
    @Input
    Property<String> getVersionCode() {
        versionCode
    }

    private final Property<String> toolsVersion = objects.property(String)

    @Optional
    @Input
    Property<String> getToolsVersion() {
        toolsVersion
    }

    private final Property<String> commitHash = objects.property(String)

    @Optional
    @Input
    Property<String> getCommitHash() {
        commitHash
    }

    private final Property<String> exportMethodName = objects.property(String)

    @Input
    Property<String> getExportMethodName() {
        exportMethodName
    }

    private final MapProperty<String, ?> customArguments = objects.mapProperty(String, Object)

    @Optional
    @Input
    MapProperty<String, ?> getCustomArguments() {
        customArguments
    }
}

trait UnityBuildPluginExtension<T extends UnityBuildPluginExtension> extends UnityBuildSpec {

    private final DirectoryProperty appConfigsDirectory = objects.directoryProperty()

    DirectoryProperty getAppConfigsDirectory() {
        appConfigsDirectory
    }

    private final DirectoryProperty outputDirectoryBase = objects.directoryProperty()

    DirectoryProperty getOutputDirectoryBase() {
        outputDirectoryBase
    }

    private final Property<UBSVersion> ubsCompatibilityVersion = objects.property(UBSVersion)

    Property<UBSVersion> getUbsCompatibilityVersion() {
        ubsCompatibilityVersion
    }

    private final Property<String> defaultAppConfigName = objects.property(String)

    Property<String> getDefaultAppConfigName() {
        defaultAppConfigName
    }

    private final RegularFileProperty exportInitScript = objects.fileProperty()

    RegularFileProperty getExportInitScript() {
        exportInitScript
    }

    private final Property<File> exportBuildDirBase = objects.property(File)

    Property<File> getExportBuildDirBase() {
        exportBuildDirBase
    }

    private final Property<Boolean> cleanBuildDirBeforeBuild = objects.property(Boolean)

    Property<Boolean> getCleanBuildDirBeforeBuild() {
        cleanBuildDirBeforeBuild
    }

    private final Property<Boolean> skipExport = objects.property(Boolean)

    Property<Boolean> getSkipExport() {
        skipExport
    }

    private final DirectoryProperty assetsDir = objects.directoryProperty()

    DirectoryProperty getAssetsDir() {
        assetsDir
    }

    private final ConfigurableFileCollection ignoreFilesForExportUpToDateCheck = objects.fileCollection()

    ConfigurableFileCollection getIgnoreFilesForExportUpToDateCheck() {
        ignoreFilesForExportUpToDateCheck
    }

    private final Property<String> appConfigSecretsKey = objects.property(String)

    Property<String> getAppConfigSecretsKey() {
        return appConfigSecretsKey
    }

    void setAppConfigSecretsKey(String key) {
        appConfigSecretsKey.set(key)
    }

    T appConfigSecretsKey(String key) {
        appConfigSecretsKey.set(key)
        this
    }

    FileCollection getAppConfigs() {
        null
    }
}
