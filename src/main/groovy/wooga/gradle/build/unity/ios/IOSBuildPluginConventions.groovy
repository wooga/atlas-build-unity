/*
 * Copyright 2022 Wooga GmbH
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
package wooga.gradle.build.unity.ios

import com.wooga.gradle.PropertyLookup

class IOSBuildPluginConventions {
    static final PropertyLookup cocoaPodsExecutableName = new PropertyLookup(
            "IOS_BUILD_COCOAPODS_EXECUTABLE_NAME",
            "iosBuild.cocoapods.executableName",
            "pod")

    static final PropertyLookup cocoaPodsExecutableDirectory = new PropertyLookup(
            "IOS_BUILD_COCOAPODS_EXECUTABLE_DIRECTORY",
            "iosBuild.cocoapods.executableDirectory",
            null)

    static final PropertyLookup xcodeProjectDirectory = new PropertyLookup(
            "IOS_BUILD_XCODE_PROJECT_DIRECTORY",
            "iosBuild.xcodeProjectDirectory",
            null)

    static final PropertyLookup xcodeProjectPath = new PropertyLookup(
            "IOS_BUILD_XCODE_PROJECT_PATH",
            "iosBuild.xcodeProjectPath",
            null)

    static final PropertyLookup xcodeWorkspacePath = new PropertyLookup(
            "IOS_BUILD_XCODE_WORKSPACE_PATH",
            "iosBuild.xcodeWorkspacePath",
            null)


    static final PropertyLookup projectBaseName = new PropertyLookup(
            "IOS_BUILD_PROJECT_BASE_NAME",
            "iosBuild.projectBaseName",
            "Unity-iPhone")

    static final PropertyLookup preferWorkspace = new PropertyLookup(
            "IOS_BUILD_PREFER_WORKSPACE",
            "iosBuild.preferWorkspace",
            true)

    static final PropertyLookup exportOptionsPlist = new PropertyLookup(
            "IOS_BUILD_EXPORT_OPTIONS_PLIST",
            "iosBuild.exportOptionsPlist",
            null)

    static final PropertyLookup publishToTestFlight = new PropertyLookup(
            "IOS_BUILD_PUBLISH_TO_TEST_FLIGHT",
            ["iosBuild.publishToTestFlight", "publishToTestFlight"],
            false)

    static final PropertyLookup adhoc = new PropertyLookup(
            "IOS_BUILD_ADHOC",
            "iosBuild.adhoc",
            false)

    static final PropertyLookup keychainPassword = new PropertyLookup(
            "IOS_BUILD_KEYCHAIN_PASSWORD",
            "iosBuild.keychainPassword",
            null)

    static final PropertyLookup codeSigningIdentityFile = new PropertyLookup(
            "IOS_BUILD_CODE_SIGNING_IDENTITY_FILE",
            "iosBuild.codeSigningIdentityFile",
            null)

    static final PropertyLookup codeSigningIdentityFilePassphrase = new PropertyLookup(
            "IOS_BUILD_CODE_SIGNING_IDENTITY_FILE_PASSPHRASE",
            "iosBuild.codeSigningIdentityFilePassphrase",
            null)

    static final PropertyLookup scheme = new PropertyLookup(
            "IOS_BUILD_SCHEME",
            "iosBuild.scheme",
            null)

    static final PropertyLookup teamId = new PropertyLookup(
            "IOS_BUILD_TEAM_ID",
            ["iosBuild.teamId", "teamId"],
            null)

    static final PropertyLookup configuration = new PropertyLookup(
            "IOS_BUILD_CONFIGURATION",
            "iosBuild.configuration",
            null)

    static final PropertyLookup provisioningName = new PropertyLookup(
            "IOS_BUILD_PROVISIONING_NAME",
            "iosBuild.provisioningName",
            null)

    static final PropertyLookup signingIdentities = new PropertyLookup(
            "IOS_BUILD_SIGNING_IDENTITIES",
            "iosBuild.signingIdentities",
            null)

    static final PropertyLookup appIdentifier = new PropertyLookup(
            "IOS_BUILD_APP_IDENTIFIER",
            ["iosBuild.appIdentifier", "appIdentifier"],
            null)
}
