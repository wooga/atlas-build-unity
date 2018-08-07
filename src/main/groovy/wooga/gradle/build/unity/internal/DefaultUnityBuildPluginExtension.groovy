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
import org.gradle.util.GUtil
import wooga.gradle.build.unity.UnityBuildPluginConsts
import wooga.gradle.build.unity.UnityBuildPluginExtension

class DefaultUnityBuildPluginExtension implements UnityBuildPluginExtension {

    private final Set<String> environments = new ArrayList<String>()
    private final Set<String> platforms = new ArrayList<String>()
    private final Project project

    private String defaultPlatform
    private String defaultEnvironment
    private String toolsVersion
    private String exportMethodName

    private Object outputDirectoryBase

    DefaultUnityBuildPluginExtension(final Project project) {
        this.project = project
    }

    @Override
    Set<String> getPlatforms() {
        String platforms = System.getenv(UnityBuildPluginConsts.PLATFORMS_ENV_VAR) ?:
                project.properties.get(UnityBuildPluginConsts.PLATFORMS_OPTION)

        if (this.platforms.empty && platforms) {
            this.platforms(platforms.split(',').collect { it.trim() })
        } else {
            this.platforms(UnityBuildPluginConsts.DEFAULT_PLATFORMS)
        }

        this.platforms
    }

    @Override
    void setPlatforms(Iterable platforms) {
        this.platforms.clear()
        this.platforms.addAll(platforms)
    }

    @Override
    UnityBuildPluginExtension platforms(Iterable platforms) {
        GUtil.addToCollection(this.platforms, platforms)
        this
    }

    @Override
    UnityBuildPluginExtension platforms(String[] platforms) {
        if (environments == null) {
            throw new IllegalArgumentException("platforms == null!")
        }
        this.platforms.addAll(Arrays.asList(platforms))
        this
    }

    @Override
    UnityBuildPluginExtension platform(String platform) {
        this.platforms.add(platform)
        return this
    }

    @Override
    Set<String> getEnvironments() {
        String environments = System.getenv(UnityBuildPluginConsts.ENVIRONMENTS_ENV_VAR) ?:
                project.properties.get(UnityBuildPluginConsts.ENVIRONMENTS_OPTION)

        if (this.environments.empty && environments) {
            this.environments(environments.split(',').collect { it.trim() })
        } else {
            this.environments(UnityBuildPluginConsts.DEFAULT_ENVIRONMENTS)
        }

        this.environments
    }

    @Override
    void setEnvironments(Iterable environments) {
        this.environments.clear()
        this.environments.addAll(environments)
    }

    @Override
    UnityBuildPluginExtension environments(Iterable environments) {
        GUtil.addToCollection(this.environments, environments)
        this
    }

    @Override
    UnityBuildPluginExtension environments(String[] environments) {
        if (environments == null) {
            throw new IllegalArgumentException("environments == null!")
        }
        this.environments.addAll(Arrays.asList(environments))
        this
    }

    @Override
    UnityBuildPluginExtension environment(String environment) {
        this.environments.add(environment)
        this
    }

    @Override
    String getExportMethodName() {
        if(exportMethodName) {
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
    String getDefaultPlatform() {
        if(defaultPlatform) {
            return defaultPlatform
        }
        System.getenv()[UnityBuildPluginConsts.PLATFORM_ENV_VAR] ?:
                project.properties.get(UnityBuildPluginConsts.PLATFORM_OPTION, getPlatforms().first())
    }

    @Override
    void setDefaultPlatform(String platform) {
        defaultPlatform = platform
    }

    @Override
    UnityBuildPluginExtension defaultPlatform(String platform) {
        setDefaultPlatform(platform)
        this
    }

    @Override
    String getDefaultEnvironment() {
        if(defaultEnvironment) {
            return defaultEnvironment
        }
        System.getenv()[UnityBuildPluginConsts.ENVIRONMENT_ENV_VAR] ?:
                project.properties.get(UnityBuildPluginConsts.ENVIRONMENT_OPTION, getEnvironments().first())
    }

    @Override
    void setDefaultEnvironment(String environment) {
        defaultEnvironment = environment
    }

    @Override
    UnityBuildPluginExtension defaultEnvironment(String environment) {
        setDefaultEnvironment(environment)
        this
    }

    @Override
    String getToolsVersion() {
        if(toolsVersion) {
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
}
