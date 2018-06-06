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

package wooga.gradle.unity.build

class UnityBuildPluginConsts {
    /**
     * Default build environments.
     * @value ["ci", "staging", "production"]
     */
    static List<String> DEFAULT_ENVIRONMENTS = ["ci", "staging", "production"]

    /**
     * Default build platforms.
     * @value ["android", "iOS", "webGL"]
     */
    static List<String> DEFAULT_PLATFORMS = ["android", "iOS", "webGL"]

    /**
     * Default value for {@code exportMethodname}.
     *
     * @value "Wooga.UnityBuild.NewAutomatedBuild.Export"
     */
    static String DEFAULT_EXPORT_METHOD_NAME = "Wooga.UnityBuild.NewAutomatedBuild.Export"

    /**
     * Gradle property name to set the default value for {@code platforms}.
     *
     * @value "unityBuild.platforms"
     * @see UnityBuildPluginExtension#getPlatforms()
     */
    static String PLATFORMS_OPTION = "unityBuild.platforms"

    /**
    * Environment variable name to set the default value for {@code platforms}.
    *
    * @value "UNITY_BUILD_PLATFORMS"
    * @see UnityBuildPluginExtension#getPlatforms()
    */
    static String PLATFORMS_ENV_VAR = "UNITY_BUILD_PLATFORMS"

    /**
     * Gradle property name to set the default value for {@code buildPlatform}.
     *
     * @value "unityBuild.platform"
     */
    static String PLATFORM_OPTION = "unityBuild.platform"

    /**
     * Environment variable name to set the default value for {@code buildPlatform}.
     *
     * @value "UNITY_BUILD_PLATFORM"
     */
    static String PLATFORM_ENV_VAR = "UNITY_BUILD_PLATFORM"

    /**
     * Gradle property name to set the default value for {@code environments}.
     *
     * @value "unityBuild.environments"
     * @see UnityBuildPluginExtension#getEnvironments()
     */
    static String ENVIRONMENTS_OPTION = "unityBuild.environments"

    /**
     * Environment variable name to set the default value for {@code environments}.
     *
     * @value "unityBuild.environments"
     * @see UnityBuildPluginExtension#getEnvironments()
     */
    static String ENVIRONMENTS_ENV_VAR = "UNITY_BUILD_ENVIRONMENTS"

    /**
     * Gradle property name to set the default value for {@code buildEnvironment}.
     *
     * @value "unityBuild.environment"
     */
    static String ENVIRONMENT_OPTION = "unityBuild.environment"

    /**
     * Environment variable name to set the default value for {@code buildEnvironment}.
     *
     * @value "UNITY_BUILD_ENVIRONMENT"
     */
    static String ENVIRONMENT_ENV_VAR = "UNITY_BUILD_ENVIRONMENT"

    /**
     * Gradle property name to set the default value for {@code exportMethodName}.
     *
     * @value "unityBuild.exportMethodName"
     * @see UnityBuildPluginExtension#getExportMethodName()
     */
    static String EXPORT_METHOD_NAME_OPTION = "unityBuild.exportMethodName"

    /**
     * Gradle property name to set the default value for {@code exportMethodName}.
     *
     * @value "UNITY_BUILD_EXPORT_METHOD_NAME"
     * @see UnityBuildPluginExtension#getExportMethodName()
     */
    static String EXPORT_METHOD_NAME_ENV_VAR = "UNITY_BUILD_EXPORT_METHOD_NAME"
}
