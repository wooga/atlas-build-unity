/*
 * Copyright 2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.xcodebuild

import wooga.gradle.xcodebuild.internal.PropertyLookup

class XcodeBuildPluginConsts {
    static final String INVALID_XCODE_PROJECT_ERROR_MESSAGE = "xcode project path must be a valid .xcodeproj or .xcworkspace"

    /**
     * Gradle property lookup object with values for fetching the default xcodebuild logs directiory.
     *
     * @environmentVariable "XCODEBUILD_LOGS_DIR"
     * @propertyName "xcodebuild.logsDir"
     * @defaultValue "logs"
     * @see wooga.gradle.xcodebuild.XcodeBuildPluginExtension#getLogsDir()
     */
    static final PropertyLookup LOGS_DIR_LOOKUP = new PropertyLookup("XCODEBUILD_LOGS_DIR", "xcodebuild.logsDir", "logs")

    /**
     * Gradle property lookup object with values to fetch default derived data path.
     *
     * @environmentVariable "XCODEBUILD_DERIVED_DATA_PATH"
     * @propertyName "xcodebuild.derivedDataPath"
     * @defaultValue "derivedData"
     * @see wooga.gradle.xcodebuild.XcodeBuildPluginExtension#getDerivedDataPath()
     */
    static final PropertyLookup DERIVED_DATA_PATH_LOOKUP = new PropertyLookup("XCODEBUILD_DERIVED_DATA_PATH", "xcodebuild.derivedDataPath", "derivedData")


    /**
     * Gradle property lookup object with values to fetch default xarchives path.
     *
     * @environmentVariable "XCODEBUILD_XARCHIVES_DIR"
     * @propertyName "xcodebuild.xarchivesDir"
     * @defaultValue "archives"
     * @see wooga.gradle.xcodebuild.XcodeBuildPluginExtension#getXarchivesDir()
     */
    static final PropertyLookup XARCHIVES_DIR_LOOKUP = new PropertyLookup("XCODEBUILD_XARCHIVES_DIR", "xcodebuild.xarchivesDir", "archives")

    /**
     * Gradle property lookup object with values for default debug symbols output path.
     *
     * @environmentVariable "XCODEBUILD_DEBUG_SYMBOLS_DIR"
     * @propertyName "xcodebuild.debugSymbolsDir"
     * @defaultValue "symbols"
     * @see wooga.gradle.xcodebuild.XcodeBuildPluginExtension#getDebugSymbolsDir()
     */
    static final PropertyLookup DEBUG_SYMBOLS_DIR_LOOKUP = new PropertyLookup("XCODEBUILD_DEBUG_SYMBOLS_DIR", "xcodebuild.debugSymbolsDir", "symbols")
}
