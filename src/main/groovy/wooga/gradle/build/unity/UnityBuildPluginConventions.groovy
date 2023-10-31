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

import com.wooga.gradle.PropertyLookup

class UnityBuildPluginConventions {

    /**
     * Method used by the Unity editor application to start a build
     */
    static final PropertyLookup BUILD_METHOD_NAME = new PropertyLookup(
        "UNITY_BUILD_EXPORT_METHOD_NAME",
        "unityBuild.exportMethodName",
        "Wooga.UnifiedBuildSystem.Editor.BuildEngine.BuildFromEnvironment")

    /**
     * The name of the default AppConfig, the serialized configuration for the build
     */
    static final PropertyLookup DEFAULT_APP_CONFIG_NAME = new PropertyLookup("UNITY_BUILD_DEFAULT_APP_CONFIG_NAME", "unityBuild.defaultAppConfigName", null)

    /**
     * The version used for the Unity application being built
     */
    static PropertyLookup BUILD_VERSION = new PropertyLookup("UNITY_BUILD_VERSION", "unityBuild.version", null)

    /**
     * The version of the Unified Build System to be compatible for, which affects task generation and certain configurations.
     */
    static PropertyLookup COMPATIBILITY_VERSION = new PropertyLookup("UNITY_BUILD_COMPATIBILITY_VERSION", "unityBuild.version", UBSVersion.v160)

    /**
     * The version code used for the Unity application being built
     */
    static PropertyLookup BUILD_VERSION_CODE = new PropertyLookup("UNITY_BUILD_VERSION_CODE", "unityBuild.versionCode", null)

    /**
     * The version of the build tools used by the Unity Editor
     */
    static PropertyLookup BUILD_TOOLS_VERSION = new PropertyLookup("UNITY_BUILD_TOOLS_VERSION", "unityBuild.toolsVersion", null)

    /**
     * The hash of the current commit the build is being made from
     */
    static PropertyLookup BUILD_COMMIT_HASH = new PropertyLookup("UNITY_BUILD_COMMIT_HASH", "unityBuild.commitHash", null)

    /**
     * The base directory where builds are exported to
     */
    static PropertyLookup EXPORT_BUILD_DIR_BASE = new PropertyLookup("UNITY_BUILD_EXPORT_BUILD_DIR_BASE", "unityBuild.exportBuildDirBase", null)

    /**
     * The script to be used by the Gradle build tasks
     */
    static PropertyLookup EXPORT_INIT_SCRIPT = new PropertyLookup("UNITY_BUILD_EXPORT_INIT_SCRIPT", "unityBuild.exportInitScript", null)

    /**
     * Whether the build output directory should be cleaned before a build
     */
    static PropertyLookup CLEAN_BUILD_DIR_BEFORE_BUILD = new PropertyLookup("UNITY_BUILD_CLEAN_BUILD_DIR_BEFORE_BUILD", "unityBuild.cleanBuildDirBeforeBuild", false)

    /**
     * Wheater to skip the default export task action
     */
    static PropertyLookup SKIP_EXPORT = new PropertyLookup("UNITY_BUILD_SKIP_EXPORT", "unityBuild.skipExport", false)

    /**
     * The key used for looking up secrets in the AppConfig during a build
     */
    static PropertyLookup APP_CONFIG_SECRETS_KEY = new PropertyLookup("UNITY_BUILD_APP_CONFIG_SECRETS_KEY", "unityBuild.appConfigSecretsKey", "secretIds")

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
    static String DEFAULT_CONFIGS_DIRECTORY_V2 = "UnifiedBuildSystem-Assets/Configs"

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
