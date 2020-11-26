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

    Boolean getCompileBitcode() {
        plist[COMPILE_BITCODE_KEY] ?: true
    }

    void setCompileBitcode(Boolean value) {
        plist[COMPILE_BITCODE_KEY] = value
    }

    String getDestination() {
        plist[DESTINATION_KEY]
    }

    void setDestination(String destination) {
        plist[DESTINATION_KEY] = destination
    }

    String getDistributionBundleIdentifier() {
        plist[DISTRIBUTION_BUNDLE_IDENTIFIER_KEY]
    }

    void setDistributionBundleIdentifier(String distributionBundleIdentifier) {
        plist[DISTRIBUTION_BUNDLE_IDENTIFIER_KEY] = distributionBundleIdentifier
    }

    Boolean getEmbedOnDemandResourcesAssetPacksInBundle() {
        plist[EMBED_ON_DEMAND_RESOURCES_ASSET_PACKS_IN_BUNDLE_KEY] ?: plist[ON_DEMAND_RESOURCES_ASSET_PACKS_BASE_URL_KEY] ? false : true
    }

    void setEmbedOnDemandResourcesAssetPacksInBundle(Boolean embedOnDemandResourcesAssetPacksInBundle) {
        plist[EMBED_ON_DEMAND_RESOURCES_ASSET_PACKS_IN_BUNDLE_KEY] = embedOnDemandResourcesAssetPacksInBundle
    }

    Boolean getGenerateAppStoreInformation() {
        plist[GENERATE_APP_STORE_INFORMATION_KEY] ?: false
    }

    void setGenerateAppStoreInformation(Boolean generateAppStoreInformation) {
        plist[GENERATE_APP_STORE_INFORMATION_KEY] = generateAppStoreInformation
    }

    String getiCloudContainerEnvironment() {
        plist[I_CLOUD_CONTAINER_ENVIRONMENT_KEY]
    }

    void setiCloudContainerEnvironment(String iCloudContainerEnvironment) {
        plist[I_CLOUD_CONTAINER_ENVIRONMENT_KEY] = iCloudContainerEnvironment
    }

    String getInstallerSigningCertificate() {
        plist[INSTALLER_SIGNING_CERTIFICATE_KEY]
    }

    void setInstallerSigningCertificate(String installerSigningCertificate) {
        plist[INSTALLER_SIGNING_CERTIFICATE_KEY] = installerSigningCertificate
    }

    DistributionManifest getManifest() {
        (DistributionManifest) plist[MANIFEST_KEY]
    }

    void setManifest(DistributionManifest manifest) {
        plist[MANIFEST_KEY] = manifest
    }

    String getMethod() {
        plist[METHOD_KEY]
    }

    void setMethod(String method) {
        plist[METHOD_KEY] = method
    }

    String getOnDemandResourcesAssetPacksBaseURL() {
        plist[ON_DEMAND_RESOURCES_ASSET_PACKS_BASE_URL_KEY]
    }

    void setOnDemandResourcesAssetPacksBaseURL(String onDemandResourcesAssetPacksBaseURL) {
        plist[ON_DEMAND_RESOURCES_ASSET_PACKS_BASE_URL_KEY] = onDemandResourcesAssetPacksBaseURL
    }

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

    String getSigningCertificate() {
        plist[SIGNING_CERTIFICATE_KEY]
    }

    void setSigningCertificate(String signingCertificate) {
        plist[SIGNING_CERTIFICATE_KEY] = signingCertificate
    }

    String getSigningStyle() {
        plist[SIGNING_STYLE_KEY]
    }

    void setSigningStyle(String signingStyle) {
        plist[SIGNING_STYLE_KEY] = signingStyle
    }

    Boolean getStripSwiftSymbols() {
        plist[STRIP_SWIFT_SYMBOLS_KEY] ?: true
    }

    void setStripSwiftSymbols(Boolean stripSwiftSymbols) {
        plist[STRIP_SWIFT_SYMBOLS_KEY] = stripSwiftSymbols
    }

    String getTeamID() {
        plist[TEAM_ID_KEY]
    }

    void setTeamID(String teamID) {
        plist[TEAM_ID_KEY] = teamID
    }

    String getThinning() {
        plist[THINNING_KEY]
    }

    void setThinning(String thinning) {
        plist[THINNING_KEY] = thinning
    }

    Boolean getUploadBitcode() {
        plist[UPLOAD_BITCODE_KEY] ?: true
    }

    void setUploadBitcode(Boolean uploadBitcode) {
        plist[UPLOAD_BITCODE_KEY] = uploadBitcode
    }

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

    class DistributionManifest {

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
    }
}
