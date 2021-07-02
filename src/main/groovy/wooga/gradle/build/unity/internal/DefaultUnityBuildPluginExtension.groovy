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

package wooga.gradle.build.unity.internal

import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import wooga.gradle.build.unity.UnityBuildPluginConventions
import wooga.gradle.build.unity.UnityBuildPluginExtension

class DefaultUnityBuildPluginExtension implements UnityBuildPluginExtension {

    protected final Project project

    final DirectoryProperty appConfigsDirectory
    final DirectoryProperty outputDirectoryBase
    final Property<String> toolsVersion
    final Property<String> version
    final Property<String> versionCode
    final Property<String> commitHash
    final Property<String> exportMethodName
    final Property<String> defaultAppConfigName
    final MapProperty<String, ?> customArguments
    final DirectoryProperty assetsDir
    DirectoryProperty getAssetsDir(){
        assetsDir
    }

    final ConfigurableFileCollection ignoreFilesForExportUpToDateCheck
    final RegularFileProperty exportInitScript
    final Property<File> exportBuildDirBase
    final Property<Boolean> cleanBuildDirBeforeBuild

    private final Property<String> appConfigSecretsKey
    Property<String> getAppConfigSecretsKey() {
        return appConfigSecretsKey
    }
    @Override
    void setAppConfigSecretsKey(String key) {
        appConfigSecretsKey.set(key)
    }

    @Override
    DefaultUnityBuildPluginExtension appConfigSecretsKey(String key) {
        setAppConfigSecretsKey(key)
        return this
    }

    DefaultUnityBuildPluginExtension(final Project project) {
        this.project = project

        appConfigsDirectory = project.objects.directoryProperty()
        outputDirectoryBase = project.objects.directoryProperty()
        toolsVersion = project.objects.property(String)
        version = project.objects.property(String)
        versionCode = project.objects.property(String)
        commitHash = project.objects.property(String)
        exportMethodName = project.objects.property(String)
        defaultAppConfigName = project.objects.property(String)
        customArguments = project.objects.mapProperty(String, Object)
        assetsDir = project.objects.directoryProperty()
        ignoreFilesForExportUpToDateCheck = project.objects.fileCollection()
        exportInitScript = project.objects.fileProperty()
        exportBuildDirBase = project.objects.property(File)
        cleanBuildDirBeforeBuild = project.objects.property(Boolean)
        appConfigSecretsKey = project.objects.property(String)
    }

    @Override
    FileCollection getAppConfigs() {
        // Returns all files with .asset in the appconfig directory
        project.fileTree(getAppConfigsDirectory()) {
            it.include UnityBuildPluginConventions.DEFAULT_APP_CONFIGS_INCLUDE_PATTERN
            it.exclude UnityBuildPluginConventions.DEFAULT_APP_CONFIGS_EXCLUDE_PATTERN
        }
    }
}
