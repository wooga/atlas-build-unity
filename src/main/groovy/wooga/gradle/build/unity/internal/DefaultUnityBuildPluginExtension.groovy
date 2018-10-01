/*
 * Copyright 2018 Wooga GmbH
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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.build.unity.UnityBuildPluginConsts
import wooga.gradle.build.unity.UnityBuildPluginExtension
import wooga.gradle.unity.UnityPluginExtension

import java.util.concurrent.Callable

class DefaultUnityBuildPluginExtension implements UnityBuildPluginExtension {

    protected final Project project

    final DirectoryProperty appConfigsDirectory
    final DirectoryProperty outputDirectoryBase
    final Property<String> toolsVersion
    final Property<String> exportMethodName
    final Property<String> defaultAppConfigName
    final Provider<Directory> assetsDir
    final ConfigurableFileCollection ignoreFilesForExportUpToDateCheck

    DefaultUnityBuildPluginExtension(final Project project) {
        this.project = project

        appConfigsDirectory = project.layout.directoryProperty()
        outputDirectoryBase = project.layout.directoryProperty()
        toolsVersion = project.objects.property(String.class)
        exportMethodName = project.objects.property(String.class)
        defaultAppConfigName = project.objects.property(String.class)
        assetsDir = project.layout.directoryProperty()
        ignoreFilesForExportUpToDateCheck = project.layout.configurableFiles()

        exportMethodName.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv().get(UnityBuildPluginConsts.EXPORT_METHOD_NAME_ENV_VAR) ?:
                        project.properties.getOrDefault(UnityBuildPluginConsts.EXPORT_METHOD_NAME_OPTION, UnityBuildPluginConsts.DEFAULT_EXPORT_METHOD_NAME)
            }
        }))

        defaultAppConfigName.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv()[UnityBuildPluginConsts.DEFAULT_APP_CONFIG_NAME_ENV_VAR] ?:
                        project.properties.get(UnityBuildPluginConsts.DEFAULT_APP_CONFIG_NAME_OPTION)
            }
        }))

        toolsVersion.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv().get(UnityBuildPluginConsts.BUILD_TOOLS_VERSION_ENV_VAR) ?:
                        project.properties.get(UnityBuildPluginConsts.BUILD_TOOLS_VERSION_OPTION, null)
            }
        }))

        outputDirectoryBase.set(project.layout.buildDirectory.dir(UnityBuildPluginConsts.DEFAULT_EXPORT_DIRECTORY_NAME))
        appConfigsDirectory.set(assetsDir.dir(UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_DIRECTORY))

        assetsDir.set(project.provider(new Callable<Directory>() {
            @Override
            Directory call() throws Exception {
                UnityPluginExtension unity = project.extensions.getByType(UnityPluginExtension)
                def assetDir = project.layout.directoryProperty()
                assetDir.set(project.file(unity.assetsDir))
                assetDir.get()
            }
        }))
    }

    @Override
    FileCollection getAppConfigs() {
        project.fileTree(getAppConfigsDirectory()) {
            it.include UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_INCLUDE_PATTERN
            it.exclude UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_EXCLUDE_PATTERN
        }
    }
}
