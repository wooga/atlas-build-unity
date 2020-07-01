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

class UnityBuildPluginConsts {

    /**
     * Default value for {@code exportMethodname}.
     *
     * @value "Wooga.UnifiedBuildSystem.Build.Export"
     */
    static String DEFAULT_EXPORT_METHOD_NAME = "Wooga.UnifiedBuildSystem.Build.Export"

    /**
     * Gradle property baseName to set the default value for {@code exportMethodName}.
     *
     * @value "unityBuild.exportMethodName"
     * @see UnityBuildPluginExtension#getExportMethodName()
     */
    static String EXPORT_METHOD_NAME_OPTION = "unityBuild.exportMethodName"

    /**
     * Environment variable to set the default value for {@code exportMethodName}.
     *
     * @value "UNITY_BUILD_EXPORT_METHOD_NAME"
     * @see UnityBuildPluginExtension#getExportMethodName()
     */
    static String EXPORT_METHOD_NAME_ENV_VAR = "UNITY_BUILD_EXPORT_METHOD_NAME"

    /**
     * Gradle property baseName to set the default value for {@code defaultAppConfigName}.
     *
     * @value "unityBuild.defaultAppConfigName"
     * @see UnityBuildPluginExtension#getDefaultAppConfigName()
     */
    static String DEFAULT_APP_CONFIG_NAME_OPTION = "unityBuild.defaultAppConfigName"

    /**
     * Environment variable to set the default value for {@code exportMethodName}.
     *
     * @value "UNITY_BUILD_DEFAULT_APP_CONFIG_NAME"
     * @see UnityBuildPluginExtension#getDefaultAppConfigName()
     */
    static String DEFAULT_APP_CONFIG_NAME_ENV_VAR = "UNITY_BUILD_DEFAULT_APP_CONFIG_NAME"

    /**
     * Gradle property baseName to set the default value for {@code version}.
     *
     * @value "unityBuild.version"
     * @see UnityBuildPluginExtension#getVersion()
     */
    static String BUILD_VERSION_OPTION = "unityBuild.version"

    /**
     * Environment variable to set the default value for {@code version}.
     *
     * @value "UNITY_BUILD_VERSION"
     * @see UnityBuildPluginExtension#getVersion()
     */
    static String BUILD_VERSION_ENV_VAR = "UNITY_BUILD_VERSION"

    /**
     * Gradle property baseName to set the default value for {@code versionCode}.
     *
     * @value "unityBuild.versionCode"
     * @see UnityBuildPluginExtension#getVersionCode()
     */
    static String BUILD_VERSION_CODE_OPTION = "unityBuild.versionCode"

    /**
     * Environment variable to set the default value for {@code versionCode}.
     *
     * @value "UNITY_BUILD_VERSION_CODE"
     * @see UnityBuildPluginExtension#getVersionCode()
     */
    static String BUILD_VERSION_CODE_ENV_VAR = "UNITY_BUILD_VERSION_CODE"

    /**
     * Gradle property baseName to set the default value for {@code toolsVersion}.
     *
     * @value "unityBuild.toolsVersion"
     * @see UnityBuildPluginExtension#getToolsVersion()
     */
    static String BUILD_TOOLS_VERSION_OPTION = "unityBuild.toolsVersion"

    /**
     * Environment variable to set the default value for {@code toolsVersion}.
     *
     * @value "UNITY_BUILD_TOOLS_VERSION"
     * @see UnityBuildPluginExtension#getToolsVersion()
     */
    static String BUILD_TOOLS_VERSION_ENV_VAR = "UNITY_BUILD_TOOLS_VERSION"

    /**
     * Gradle property baseName to set the default value for {@code commitHash}.
     *
     * @value "unityBuild.commitHash"
     * @see UnityBuildPluginExtension#getCommitHash()
     */
    static String BUILD_COMMIT_HASH_OPTION = "unityBuild.commitHash"

    /**
     * Environment variable to set the default value for {@code commitHash}.
     *
     * @value "UNITY_BUILD_COMMIT_HASH"
     * @see UnityBuildPluginExtension#getCommitHash()
     */
    static String BUILD_COMMIT_HASH_ENV_VAR = "UNITY_BUILD_COMMIT_HASH"

    /**
     * Environment variable to set the default value for {@code exportBuildDirBase}.
     *
     * @value "UNITY_BUILD_EXPORT_BUILD_DIR_BASE"
     * @see UnityBuildPluginExtension#getExportBuildDirBase()
     */
    static String EXPORT_BUILD_DIR_BASE_ENV_VAR = "UNITY_BUILD_EXPORT_BUILD_DIR_BASE"

    /**
     * Gradle property baseName to set the default value for {@code exportBuildDirBase}.
     *
     * @value "unityBuild.exportBuildDirBase"
     * @see UnityBuildPluginExtension#getExportBuildDirBase()
     */
    static String EXPORT_BUILD_DIR_BASE_OPTION = "unityBuild.exportBuildDirBase"

    /**
     * Gradle property baseName to set the default value for {@code exportInitScript}.
     *
     * @value "unityBuild.exportInitScript"
     * @see UnityBuildPluginExtension#getExportInitScript()
     */
    static String EXPORT_INIT_SCRIPT_OPTION = "unityBuild.exportInitScript"

    /**
     * Environment variable to set the default value for {@code exportInitScript}.
     *
     * @value "UNITY_BUILD_EXPORT_INIT_SCRIPT"
     * @see UnityBuildPluginExtension#getExportInitScript()
     */
    static String EXPORT_INIT_SCRIPT_ENV_VAR = "UNITY_BUILD_EXPORT_INIT_SCRIPT"

    /**
     * Gradle property baseName to set the default value for {@code cleanBuildDirBeforeBuild}.
     *
     * @value "unityBuild.cleanBuildDirBeforeBuild"
     * @see UnityBuildPluginExtension#getCleanBuildDirBeforeBuild()
     */
    static String CLEAN_BUILD_DIR_BEFORE_BUILD_OPTION = "unityBuild.cleanBuildDirBeforeBuild"

    /**
     * Environment variable to set the default value for {@code cleanBuildDirBeforeBuild}.
     *
     * @value "UNITY_BUILD_CLEAN_BUILD_DIR_BEFORE_BUILD"
     * @see UnityBuildPluginExtension#getCleanBuildDirBeforeBuild()
     */
    static String CLEAN_BUILD_DIR_BEFORE_BUILD_ENV_VAR = "UNITY_BUILD_CLEAN_BUILD_DIR_BEFORE_BUILD"

    static Integer SECRETS_KEY_ITERATION = 65536
    static Integer SECRETS_KEY_LENGTH = 256

    static String SECRETS_KEY_OPTION = "unityBuild.secretsKey"
    static String SECRETS_KEY_ENV_VAR = "UNITY_BUILD_SECRETS_KEY"

    /**
     * Default name for the base export location.
     *
     * @value "export"
     * @see UnityBuildPluginExtension#getOutputDirectoryBase()
     */
    static String DEFAULT_EXPORT_DIRECTORY_NAME = "export"

    /**
     * Default path to app configs in Assets dir.
     *
     * @value "export"
     * @see UnityBuildPluginExtension#getAppConfigsDirectory()
     */
    static String DEFAULT_APP_CONFIGS_DIRECTORY = "UnifiedBuildSystem-Assets/AppConfigs"

    /**
     * Default include pattern for app configs.
     *
     * @value "unityBuild.toolsVersion"
     * @see UnityBuildPluginExtension#getAppConfigs()
     */
    static String DEFAULT_APP_CONFIGS_INCLUDE_PATTERN = "*.asset"

    /**
     * Default exclude pattern for app configs.
     *
     * @value "unityBuild.toolsVersion"
     * @see UnityBuildPluginExtension#getAppConfigs()
     */
    static String DEFAULT_APP_CONFIGS_EXCLUDE_PATTERN = "*.meta"
}
