/*
 * Copyright 2018-2020 Wooga GmbH
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

package wooga.gradle.xcodebuild.config

import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser

class ExportOptions implements GroovyInterceptable {

    private static final String COMPILE_BITCODE_KEY = "compileBitcode"
    private static final String DESTINATION_KEY = "destination"
    private static final String DISTRIBUTION_BUNDLE_IDENTIFIER_KEY = "distributionBundleIdentifier"
    private static final String EMBED_ON_DEMAND_RESOURCES_ASSET_PACKS_IN_BUNDLE_KEY = "embedOnDemandResourcesAssetPacksInBundle"
    private static final String GENERATE_APP_STORE_INFORMATION_KEY = "generateAppStoreInformation"
    private static final String I_CLOUD_CONTAINER_ENVIRONMENT_KEY = "iCloudContainerEnvironment"
    private static final String INSTALLER_SIGNING_CERTIFICATE_KEY = "installerSigningCertificate"
    private static final String MANAGE_APP_VERSION_AND_BUILD_NUMBER_KEY = "manageAppVersionAndBuildNumber"
    private static final String MANIFEST_KEY = "manifest"
    private static final String METHOD_KEY = "method"
    private static final String ON_DEMAND_RESOURCES_ASSET_PACKS_BASE_URL_KEY = "onDemandResourcesAssetPacksBaseURL"
    private static final String PROVISIONING_PROFILES_KEY = "provisioningProfiles"
    private static final String SIGNING_CERTIFICATE_KEY = "signingCertificate"
    private static final String SIGNING_STYLE_KEY = "signingStyle"
    private static final String STRIP_SWIFT_SYMBOLS_KEY = "stripSwiftSymbols"
    private static final String TEAM_ID_KEY = "teamID"
    private static final String THINNING_KEY = "thinning"
    private static final String UPLOAD_BITCODE_KEY = "uploadBitcode"
    private static final String UPLOAD_SYMBOLS_KEY = "uploadSymbols"

    private Map plist = new HashMap<String, Object>()

    private ExportOptions(Map data) {
        plist = data

        def distributionManifest = (Map<String, String>) plist[MANIFEST_KEY]
        if (distributionManifest) {
            plist[MANIFEST_KEY] = new DistributionManifest(distributionManifest)
        }
        plist[PROVISIONING_PROFILES_KEY] = plist[PROVISIONING_PROFILES_KEY] ?: [:]
    }

    ExportOptions() {
        this([:])
    }

    /**
     * For non-App Store exports, should Xcode re-compile the app from bitcode?
     */
    Boolean getCompileBitcode() {
        plist[COMPILE_BITCODE_KEY] ?: true
    }

    void setCompileBitcode(Boolean value) {
        plist[COMPILE_BITCODE_KEY] = value
    }

    /**
     * Determines whether the app is exported locally or uploaded to Apple. Options are export or upload.
     * The available options vary based on the selected distribution method.
     */
    String getDestination() {
        plist[DESTINATION_KEY] ?: "export"
    }

    void setDestination(String destination) {
        plist[DESTINATION_KEY] = destination
    }

    /**
     * Reformat archive to focus on eligible target bundle identifier.
     */
    String getDistributionBundleIdentifier() {
        plist[DISTRIBUTION_BUNDLE_IDENTIFIER_KEY]
    }

    void setDistributionBundleIdentifier(String distributionBundleIdentifier) {
        plist[DISTRIBUTION_BUNDLE_IDENTIFIER_KEY] = distributionBundleIdentifier
    }

    /**
     * For non-App Store exports, if the app uses On Demand Resources and this is YES, asset packs are embedded in the
     * app bundle so that the app can be tested without a server to host asset packs.
     * Defaults to true unless onDemandResourcesAssetPacksBaseURL is specified.
     *
     */
    Boolean getEmbedOnDemandResourcesAssetPacksInBundle() {
        plist[EMBED_ON_DEMAND_RESOURCES_ASSET_PACKS_IN_BUNDLE_KEY] ?: plist[ON_DEMAND_RESOURCES_ASSET_PACKS_BASE_URL_KEY] ? false : true
    }

    void setEmbedOnDemandResourcesAssetPacksInBundle(Boolean embedOnDemandResourcesAssetPacksInBundle) {
        plist[EMBED_ON_DEMAND_RESOURCES_ASSET_PACKS_IN_BUNDLE_KEY] = embedOnDemandResourcesAssetPacksInBundle
    }

    /**
     * For App Store exports, should Xcode generate App Store Information for uploading with iTMSTransporter?
     */
    Boolean getGenerateAppStoreInformation() {
        plist[GENERATE_APP_STORE_INFORMATION_KEY] ?: false
    }

    void setGenerateAppStoreInformation(Boolean generateAppStoreInformation) {
        plist[GENERATE_APP_STORE_INFORMATION_KEY] = generateAppStoreInformation
    }

    /**
     * If the app is using CloudKit, this configures the "com.apple.developer.icloud-container-environment" entitlement.
     * Available options vary depending on the type of provisioning profile used, but may include: Development and Production.
     */
    String getiCloudContainerEnvironment() {
        plist[I_CLOUD_CONTAINER_ENVIRONMENT_KEY]
    }

    void setiCloudContainerEnvironment(String iCloudContainerEnvironment) {
        plist[I_CLOUD_CONTAINER_ENVIRONMENT_KEY] = iCloudContainerEnvironment
    }

    /**
     * For manual signing only. Provide a certificate name, SHA-1 hash, or automatic selector to use for signing.
     * Automatic selectors allow Xcode to pick the newest installed certificate of a particular type.
     * The available automatic selectors are "Developer ID Installer" and "Mac Installer Distribution".
     * Defaults to an automatic certificate selector matching the current distribution method.
     */
    String getInstallerSigningCertificate() {
        plist[INSTALLER_SIGNING_CERTIFICATE_KEY]
    }

    void setInstallerSigningCertificate(String installerSigningCertificate) {
        plist[INSTALLER_SIGNING_CERTIFICATE_KEY] = installerSigningCertificate
    }

    /**
     * Should Xcode manage the app's build number when uploading to App Store Connect?
     */
    Boolean getManageAppVersionAndBuildNumber() {
        plist[MANAGE_APP_VERSION_AND_BUILD_NUMBER_KEY] ?: true
    }

    void setManageAppVersionAndBuildNumber(Boolean value) {
        plist[MANAGE_APP_VERSION_AND_BUILD_NUMBER_KEY] = method
    }

    /**
     * For non-App Store exports, users can download your app over the web by opening your distribution manifest
     * file in a web browser.
     * <p>
     * To generate a distribution manifest, the value of this key should be a dictionary with three sub-keys:
     * appURL, displayImageURL, fullSizeImageURL.
     * The additional sub-key assetPackManifestURL is required when using on-demand resources.
     */
    DistributionManifest getManifest() {
        (DistributionManifest) plist[MANIFEST_KEY]
    }

    void setManifest(DistributionManifest manifest) {
        plist[MANIFEST_KEY] = manifest
    }

    void setManifest(Map<String, ?> manifest) {
        plist[MANIFEST_KEY] = DistributionManifest.distributionManifest(manifest)
    }

    /**
     * Describes how Xcode should export the archive. Available options: app-store, validation, ad-hoc, package,
     * enterprise, development, developer-id, and mac-application.
     * The list of options varies based on the type of archive. Defaults to development.
     */
    String getMethod() {
        plist[METHOD_KEY]
    }

    void setMethod(String method) {
        plist[METHOD_KEY] = method
    }

    /**
     * For non-App Store exports, if the app uses On Demand Resources and embedOnDemandResourcesAssetPacksInBundle
     * isn't {@code true}, this should be a base URL specifying where asset packs are going to be hosted.
     * This configures the app to download asset packs from the specified URL.
     * @return
     */
    String getOnDemandResourcesAssetPacksBaseURL() {
        plist[ON_DEMAND_RESOURCES_ASSET_PACKS_BASE_URL_KEY]
    }

    void setOnDemandResourcesAssetPacksBaseURL(String onDemandResourcesAssetPacksBaseURL) {
        plist[ON_DEMAND_RESOURCES_ASSET_PACKS_BASE_URL_KEY] = onDemandResourcesAssetPacksBaseURL
    }

    /**
     *  For manual signing only. Specify the provisioning profile to use for each executable in your app.
     *  <p>
     *  Keys in this dictionary are the bundle identifiers of executables;
     *  values are the provisioning profile name or UUID to use.
     * @return
     */
    Map<String, String> getProvisioningProfiles() {
        (Map<String, String>) plist[PROVISIONING_PROFILES_KEY]
    }

    void setProvisioningProfiles(Map<String, String> provisioningProfiles) {
        plist[PROVISIONING_PROFILES_KEY] = provisioningProfiles
    }

    ExportOptions provisionProfile(String bundleIdentifier, String profileName) {
        if (!plist[PROVISIONING_PROFILES_KEY]) {
            plist[PROVISIONING_PROFILES_KEY] = [:]
        }
        plist[PROVISIONING_PROFILES_KEY][bundleIdentifier] = profileName
        this
    }

    String provisionProfile(String bundleIdentifier) {
        if (!plist[PROVISIONING_PROFILES_KEY]) {
            return null
        }
        plist[PROVISIONING_PROFILES_KEY][bundleIdentifier]
    }

    /**
     * For manual signing only. Provide a certificate name, SHA-1 hash,
     * or automatic selector to use for signing.
     * <p>
     * Automatic selectors allow Xcode to pick the newest installed certificate of a particular type.
     * The available automatic selectors are "Mac App Distribution", "iOS Developer", "iOS Distribution",
     * "Developer ID Application", "Apple Distribution", "Mac Developer", and "Apple Development".
     * Defaults to an automatic certificate selector matching the current distribution method.
     */
    String getSigningCertificate() {
        plist[SIGNING_CERTIFICATE_KEY]
    }

    void setSigningCertificate(String signingCertificate) {
        plist[SIGNING_CERTIFICATE_KEY] = signingCertificate
    }

    /**
     * The signing style to use when re-signing the app for distribution. Options are manual or automatic.
     * Apps that were automatically signed when archived can be signed manually or automatically during distribution,
     * and default to automatic. Apps that were manually signed when archived must be manually signed during distribution,
     * so the value of signingStyle is ignored.
     */
    String getSigningStyle() {
        plist[SIGNING_STYLE_KEY]
    }

    void setSigningStyle(String signingStyle) {
        plist[SIGNING_STYLE_KEY] = signingStyle
    }

    /**
     * Should symbols be stripped from Swift libraries in your IPA? Defaults to {@code true}.
     */
    Boolean getStripSwiftSymbols() {
        plist[STRIP_SWIFT_SYMBOLS_KEY] ?: true
    }

    void setStripSwiftSymbols(Boolean stripSwiftSymbols) {
        plist[STRIP_SWIFT_SYMBOLS_KEY] = stripSwiftSymbols
    }

    /**
     * The Developer Portal team to use for this export. Defaults to the team used to build the archive.
     */
    String getTeamID() {
        plist[TEAM_ID_KEY]
    }

    void setTeamID(String teamID) {
        plist[TEAM_ID_KEY] = teamID
    }

    /**
     * For non-App Store exports, should Xcode thin the package for one or more device variants?
     * <p>
     * Available options:
     * <ul>
     *   <li><b>none</b> (Xcode produces a non-thinned universal app)</li>
     *   <li><b>thin-for-all-variants</b> (Xcode produces a universal app and all available thinned variants)</li>
     *   <li>a model identifier for a specific device (e.g. "iPhone7,1")</li>
     * </ul>
     * Defaults to &lt;none&gt;.
     * @return
     */
    String getThinning() {
        plist[THINNING_KEY]
    }

    void setThinning(String thinning) {
        plist[THINNING_KEY] = thinning
    }

    /**
     * For App Store exports, should the package include bitcode?
     * Defaults to {@code true}.
     */
    Boolean getUploadBitcode() {
        plist[UPLOAD_BITCODE_KEY] ?: true
    }

    void setUploadBitcode(Boolean uploadBitcode) {
        plist[UPLOAD_BITCODE_KEY] = uploadBitcode
    }

    /**
     * For App Store exports, should the package include symbols?
     * Defaults to {@code true}.
     */
    Boolean getUploadSymbols() {
        plist[UPLOAD_SYMBOLS_KEY] ?: true
    }

    void setUploadSymbols(Boolean uploadSymbols) {
        plist[UPLOAD_SYMBOLS_KEY] = uploadSymbols
    }

    static ExportOptions open(InputStream stream) {
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(stream)
        new ExportOptions((Map) rootDict.toJavaObject())
    }

    static ExportOptions open(File plistFile) {
        NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(plistFile)
        new ExportOptions((Map) rootDict.toJavaObject())
    }

    NSDictionary toDictionary() {
        DistributionManifest distributionManifest = (DistributionManifest) getManifest()
        def plistForOutput = plist.clone()
        if (distributionManifest) {
            plistForOutput["manifest"] = NSDictionary.fromJavaObject(distributionManifest.plist)
        }
        if (provisioningProfiles.isEmpty()) {
            plistForOutput["provisioningProfiles"] = null
        }
        (NSDictionary) NSDictionary.fromJavaObject(plistForOutput)
    }

    String toXMLPropertyList() {
        toDictionary().toXMLPropertyList()
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ExportOptions)) return false

        ExportOptions that = (ExportOptions) o

        if (plist != that.plist) return false
        return true
    }

    def propertyMissing(String name, Object value) {
        plist[name] = value
    }

    def propertyMissing(String name) {
        plist[name]
    }

    static class DistributionManifest {

        private static final String APP_URL = "appURL"
        private static final String DISPLAY_IMAGE_URL = "displayImageURL"
        private static final String FULL_SIZE_IMAGE_URL = "fullSizeImageURL"
        private static final String ASSET_PACK_MANIFEST_URL = "assetPackManifestURL"

        private Map plist = new HashMap<String, Object>()

        DistributionManifest(String appURL, String displayImageURL, String fullSizeImageURL, String assetPackManifestURL = null) {
            this(["appURL"              : appURL,
                  "displayImageURL"     : displayImageURL,
                  "fullSizeImageURL"    : fullSizeImageURL,
                  "assetPackManifestURL": assetPackManifestURL
            ])
        }

        static DistributionManifest distributionManifest(String appURL, String displayImageURL, String fullSizeImageURL, String assetPackManifestURL = null) {
            new DistributionManifest(appURL, displayImageURL, fullSizeImageURL, assetPackManifestURL)
        }

        static DistributionManifest distributionManifest(Map<String, ?> data) {
            new DistributionManifest(data)
        }

        private DistributionManifest(Map data) {
            this.plist = data
        }

        String getAppURL() {
            plist[APP_URL]
        }

        void setAppURL(String appUrl) {
            plist[APP_URL] = appUrl
        }


        String getDisplayImageURL() {
            plist[DISPLAY_IMAGE_URL]
        }


        void setDisplayImageURL(String displayImageURL) {
            plist[DISPLAY_IMAGE_URL] = displayImageURL
        }


        String getFullSizeImageURL() {
            plist[FULL_SIZE_IMAGE_URL]
        }


        void setFullSizeImageURL(String fullSizeImageURL) {
            plist[FULL_SIZE_IMAGE_URL] = fullSizeImageURL
        }


        String getAssetPackManifestURL() {
            plist[ASSET_PACK_MANIFEST_URL]
        }


        void setAssetPackManifestURL(String assetPackManifestURL) {
            plist[ASSET_PACK_MANIFEST_URL] = assetPackManifestURL
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (!(o instanceof DistributionManifest)) return false

            DistributionManifest that = (DistributionManifest) o

            if (plist != that.plist) return false

            return true
        }


        @Override
        String toString() {
            "DistributionManifest{" +
                    "apppUrl='" + appURL + '\'' +
                    ", displayImageURL='" + displayImageURL + '\'' +
                    ", fullSizeImageURL='" + fullSizeImageURL + '\'' +
                    ", assetPackManifestURL='" + assetPackManifestURL + '\'' +
                    '}'
        }
    }
}
