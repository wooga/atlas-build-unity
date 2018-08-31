/*
 * Copyright 2017 the original author or authors.
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
import org.gradle.api.file.FileCollection
import wooga.gradle.build.unity.UnityBuildPluginConsts
import wooga.gradle.build.unity.UnityBuildPluginExtension
import wooga.gradle.unity.UnityPluginExtension

class DefaultUnityBuildPluginExtension implements UnityBuildPluginExtension {

    private final Project project

    private String toolsVersion
    private String exportMethodName

    private Object outputDirectoryBase
    private Object appConfigsDirectory
    private String appConfigIncludePattern
    private String appConfigExcludePattern
    private String defaultAppConfigName
    private FileCollection appConfigs

    DefaultUnityBuildPluginExtension(final Project project) {
        this.project = project
    }

    @Override
    String getExportMethodName() {
        if (exportMethodName) {
            return exportMethodName
        }
        System.getenv().get(UnityBuildPluginConsts.EXPORT_METHOD_NAME_ENV_VAR) ?:
                project.properties.get(UnityBuildPluginConsts.EXPORT_METHOD_NAME_OPTION, UnityBuildPluginConsts.DEFAULT_EXPORT_METHOD_NAME)
    }

    @Override
    void setExportMethodName(String method) {
        exportMethodName = method
    }

    @Override
    UnityBuildPluginExtension exportMethodName(String method) {
        setExportMethodName(method)
        this
    }

    @Override
    String getDefaultAppConfigName() {
        if (defaultAppConfigName) {
            return defaultAppConfigName
        }
        System.getenv()[UnityBuildPluginConsts.DEFAULT_APP_CONFIG_NAME_ENV_VAR] ?:
                project.properties.get(UnityBuildPluginConsts.DEFAULT_APP_CONFIG_NAME_OPTION)
    }

    @Override
    void setDefaultAppConfigName(String name) {
        defaultAppConfigName = name
    }

    @Override
    UnityBuildPluginExtension defaultAppConfigName(String name) {
        setDefaultAppConfigName(name)
        return this
    }

    @Override
    String getToolsVersion() {
        if (toolsVersion) {
            return toolsVersion
        }
        System.getenv().get(UnityBuildPluginConsts.BUILD_TOOLS_VERSION_ENV_VAR) ?:
                project.properties.get(UnityBuildPluginConsts.BUILD_TOOLS_VERSION_OPTION, null)
    }

    @Override
    void setToolsVersion(String version) {
        toolsVersion = version
    }

    @Override
    UnityBuildPluginExtension toolsVersion(String version) {
        setToolsVersion(version)
        this
    }

    @Override
    File getOutputDirectoryBase() {
        if (outputDirectoryBase) {
            return project.file(outputDirectoryBase)
        }

        project.file("${project.buildDir}/${UnityBuildPluginConsts.DEFAULT_EXPORT_DIRECTORY_NAME}")
    }

    @Override
    void setOutputDirectoryBase(Object outputDirectoryBase) {
        this.outputDirectoryBase = outputDirectoryBase
    }

    @Override
    UnityBuildPluginExtension outputDirectoryBase(Object outputDirectoryBase) {
        setOutputDirectoryBase(outputDirectoryBase)
        this
    }


    File getAppConfigsDirectory() {
        if (appConfigsDirectory) {
            return project.file(appConfigsDirectory)
        }

        UnityPluginExtension unity = project.extensions.getByType(UnityPluginExtension)

        project.file("${unity.assetsDir}/${UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_DIRECTORY}")
    }


    void setAppConfigsDirectory(Object appConfigsDirectory) {
        this.appConfigsDirectory = appConfigsDirectory
        appConfigs = null
    }

    @Override
    UnityBuildPluginExtension appConfigsDirectory(Object appConfigsDirectory) {
        setAppConfigsDirectory(appConfigsDirectory)
        return this
    }

    @Override
    FileCollection getAppConfigs() {
        if(!appConfigs) {
            appConfigs = project.fileTree(getAppConfigsDirectory()) {
                it.include UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_INCLUDE_PATTERN
                it.exclude UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_EXCLUDE_PATTERN
            }
        }
        appConfigs
    }
}
