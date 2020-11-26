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

import spock.lang.Specification
import spock.lang.Unroll

class ExportOptionsSpec extends Specification {

    def plistFile = """
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>
      <key>compileBitcode</key>
      <false/>
      <key>destination</key>
      <string>export</string>
      <key>distributionBundleIdentifier</key>
      <string>com.wooga.test</string>
      <key>embedOnDemandResourcesAssetPacksInBundle</key>
      <false/>
      <key>generateAppStoreInformation</key>
      <false/>
      <key>iCloudContainerEnvironment</key>
      <string>Development</string>
      <key>installerSigningCertificate</key>
      <string>Developer ID Installer</string>
      <key>manifest</key>
      <dict>
          <key>appURL</key>
          <string>http://some/url</string>
          <key>displayImageURL</key>
          <string>http://some/url</string>
          <key>fullSizeImageURL</key>
          <string>http://some/url</string>
          <key>assetPackManifestURL</key>
          <string>http://some/url</string>
      </dict>
      <key>method</key>
      <string>development</string>
      <key>onDemandResourcesAssetPacksBaseURL</key>
      <string>http://some/url</string>
      <key>provisioningProfiles</key>
      <dict>
          <key>net.wooga.xcodebuildPluginTest</key>
          <string>xcodebuildPluginTest</string>
      </dict>
      <key>signingCertificate</key>
      <string>someCert</string>
      <key>signingStyle</key>
      <string>manual</string>
      <key>stripSwiftSymbols</key>
      <false/>
      <key>teamID</key>
      <string>1234567890qwerty</string>
      <key>thinning</key>
      <string>thin-for-all-variants</string>
      <key>uploadBitcode</key>
      <false/>
      <key>uploadSymbols</key>
      <false/>
    </dict>
    </plist>
    """.stripIndent().trim()

    def "can read exportOptions from file"() {
        given: "a test file"
        def exportOptionsFile = File.createTempFile("exportOptions", ".plist")
        exportOptionsFile << plistFile

        when:
        def file = ExportOptions.open(exportOptionsFile)

        then:
        noExceptionThrown()
    }

    def "ExportOptions read from output of other ExportOptions are equal"() {
        given: "file read from test string"
        def file1 = ExportOptions.open(new ByteArrayInputStream(plistFile.bytes))

        and: "a second file read from exported string from first file"
        def file2 = ExportOptions.open(new ByteArrayInputStream(file1.toXMLPropertyList().bytes))

        expect:
        file1 == file2
    }

    def "ExportOptions objects from same file are equal"() {
        given: "file read from test string"
        def file1 = ExportOptions.open(new ByteArrayInputStream(plistFile.bytes))

        and: "a second file from the same test string"
        def file2 = ExportOptions.open(new ByteArrayInputStream(plistFile.bytes))

        expect:
        file1 == file2
    }

    def "equals checks all properties of being equal"() {
        given: "file read from test string"
        def file1 = ExportOptions.open(new ByteArrayInputStream(plistFile.bytes))

        and: "a second file from the same test string"
        def file2 = ExportOptions.open(new ByteArrayInputStream(plistFile.bytes))

        expect:
        file1.compileBitcode == file2.compileBitcode
        file1.destination == file2.destination
        file1.distributionBundleIdentifier == file2.distributionBundleIdentifier
        file1.embedOnDemandResourcesAssetPacksInBundle == file2.embedOnDemandResourcesAssetPacksInBundle
        file1.generateAppStoreInformation == file2.generateAppStoreInformation
        file1.iCloudContainerEnvironment == file2.iCloudContainerEnvironment
        file1.installerSigningCertificate == file2.installerSigningCertificate
        file1.manifest == file2.manifest
        file1.method == file2.method
        file1.onDemandResourcesAssetPacksBaseURL == file2.onDemandResourcesAssetPacksBaseURL
        file1.provisioningProfiles == file2.provisioningProfiles
        file1.signingCertificate == file2.signingCertificate
        file1.signingStyle == file2.signingStyle
        file1.stripSwiftSymbols == file2.stripSwiftSymbols
        file1.teamID == file2.teamID
        file1.thinning == file2.thinning
        file1.uploadBitcode == file2.uploadBitcode
        file1.uploadSymbols == file2.uploadSymbols
    }

    def "supports dynamic properties"() {
        given: "file read from test string"
        def file1 = ExportOptions.open(new ByteArrayInputStream(plistFile.bytes))

        and: "adding an unknown property"
        file1.foo = "some value"

        when:
        def file2 = ExportOptions.open(new ByteArrayInputStream(file1.toXMLPropertyList().bytes))

        then:
        file2.foo == "some value"
    }

    def "default constructor creates empty options with default values"() {
        given:
        def options = new ExportOptions()

        expect:
        with(options) {
            compileBitcode
            destination == null
            distributionBundleIdentifier == null
            embedOnDemandResourcesAssetPacksInBundle
            !generateAppStoreInformation
            iCloudContainerEnvironment == null
            installerSigningCertificate == null
            manifest == null
            method == null
            onDemandResourcesAssetPacksBaseURL == null
            provisioningProfiles == [:]
            signingCertificate == null
            !signingStyle
            stripSwiftSymbols
            teamID == null
            thinning == null
            uploadBitcode
            uploadSymbols
        }
    }

    def "Generates sparse output"() {
        given: "empty options object"
        def emptyOptions = new ExportOptions()

        when:
        def plistXML = emptyOptions.toXMLPropertyList()

        then:
        with(plistXML) {
            !contains("<key>compileBitcode</key>")
            !contains("<key>destination</key>")
            !contains("<key>distributionBundleIdentifier</key>")
            !contains("<key>embedOnDemandResourcesAssetPacksInBundle</key>")
            !contains("<key>generateAppStoreInformation</key>")
            !contains("<key>iCloudContainerEnvironment</key>")
            !contains("<key>installerSigningCertificate</key>")
            !contains("<key>manifest</key>")
            !contains("<key>method</key>")
            !contains("<key>onDemandResourcesAssetPacksBaseURL</key>")
            !contains("<key>provisioningProfiles</key>")
            !contains("<key>signingCertificate</key>")
            !contains("<key>signingStyle</key>")
            !contains("<key>stripSwiftSymbols</key>")
            !contains("<key>teamID</key>")
            !contains("<key>thinning</key>")
            !contains("<key>uploadBitcode</key>")
            !contains("<key>uploadSymbols</key>")
        }
    }

    def propertyToXmlPlistKey(String property) {
        "<key>${property}</key>"
    }

    def valueToXmlPlistValue(Object value) {
        switch (value.getClass().simpleName) {
            case "String":
                return "<string>${value}</string>"
            case "Boolean":
                return "<${value}/>"
            case "LinkedHashMap":
            case "HashMap":
                def builder = new StringBuilder()
                builder << "<dict>"
                ((Map) value).each { k, v ->
                    builder << "<key>${k}</key>"
                    builder << valueToXmlPlistValue(v)
                }
                builder << "</dict>"
                return builder.toString()
        }
    }

    @Unroll
    def "Can set property #propertyName with value #value"() {
        given: "empty options object"
        def emptyOptions = new ExportOptions()

        when:
        emptyOptions.setProperty(propertyName, value)

        then:
        def plistXML = emptyOptions.toXMLPropertyList().readLines().collect { it.trim() }.join()
        plistXML.contains(propertyToXmlPlistKey(propertyName) + valueToXmlPlistValue(value))

        where:
        propertyName                               | value
        "compileBitcode"                           | true
        "compileBitcode"                           | false
        "destination"                              | "a string value"
        "distributionBundleIdentifier"             | "a string value"
        "embedOnDemandResourcesAssetPacksInBundle" | true
        "embedOnDemandResourcesAssetPacksInBundle" | false
        "generateAppStoreInformation"              | true
        "generateAppStoreInformation"              | false
        "iCloudContainerEnvironment"               | "a string value"
        "installerSigningCertificate"              | "a string value"
        "method"                                   | "a string value"
        "onDemandResourcesAssetPacksBaseURL"       | "a string value"
        "signingCertificate"                       | "a string value"
        "signingStyle"                             | "a string value"
        "stripSwiftSymbols"                        | true
        "stripSwiftSymbols"                        | false
        "teamID"                                   | "a string value"
        "thinning"                                 | "a string value"
        "uploadBitcode"                            | true
        "uploadBitcode"                            | false
        "uploadSymbols"                            | true
        "uploadSymbols"                            | false

    }

    def "can set property manifest with custom DistributionManifest"() {
        given: "empty options object"
        def emptyOptions = new ExportOptions()

        and: "a custom distribution manifest"
        def distributionManifest = new ExportOptions.DistributionManifest(emptyOptions, appURL, displayImageURL, fullSizeImageURL, assetPackManifestURL)

        when:
        emptyOptions.manifest = distributionManifest

        then:
        def plistXML = emptyOptions.toXMLPropertyList().readLines().collect { it.trim() }.join()
        plistXML.contains(propertyToXmlPlistKey("manifest") + valueToXmlPlistValue(expectedRawDistributionManifest))

        where:
        appURL = "testAppUrl"
        displayImageURL = "testDisplayImageURL"
        fullSizeImageURL = "testFullSizeImageURL"
        assetPackManifestURL = "testAssetPackManifestURL"
        expectedRawDistributionManifest = [
                'appURL'              : appURL,
                'displayImageURL'     : displayImageURL,
                'fullSizeImageURL'    : fullSizeImageURL,
                'assetPackManifestURL': assetPackManifestURL,
        ]
    }

    def "can access publish manifest properties"() {
        given: "empty options object"
        def emptyOptions = new ExportOptions()

        and: "a custom distribution manifest"
        def distributionManifest = new ExportOptions.DistributionManifest(emptyOptions, appURL, displayImageURL, fullSizeImageURL, assetPackManifestURL)

        when:
        emptyOptions.manifest = distributionManifest

        then:
        emptyOptions.manifest.appURL == appURL
        emptyOptions.manifest.displayImageURL == displayImageURL
        emptyOptions.manifest.fullSizeImageURL == fullSizeImageURL
        emptyOptions.manifest.assetPackManifestURL == assetPackManifestURL

        where:
        appURL = "testAppUrl"
        displayImageURL = "testDisplayImageURL"
        fullSizeImageURL = "testFullSizeImageURL"
        assetPackManifestURL = "testAssetPackManifestURL"

    }

    def "can set publish manifest properties"() {
        given: "empty options object"
        def emptyOptions = new ExportOptions()

        and: "a custom distribution manifest"
        def distributionManifest = new ExportOptions.DistributionManifest(emptyOptions, "test", "test", "test", "test")
        emptyOptions.manifest = distributionManifest

        when:
        emptyOptions.manifest.appURL = appURL
        emptyOptions.manifest.displayImageURL = displayImageURL
        emptyOptions.manifest.fullSizeImageURL = fullSizeImageURL
        emptyOptions.manifest.assetPackManifestURL = assetPackManifestURL

        then:
        def plistXML = emptyOptions.toXMLPropertyList().readLines().collect { it.trim() }.join()
        plistXML.contains(propertyToXmlPlistKey("manifest") + valueToXmlPlistValue(expectedRawDistributionManifest))

        where:
        appURL = "testAppUrl"
        displayImageURL = "testDisplayImageURL"
        fullSizeImageURL = "testFullSizeImageURL"
        assetPackManifestURL = "testAssetPackManifestURL"
        expectedRawDistributionManifest = [
                'appURL'              : appURL,
                'displayImageURL'     : displayImageURL,
                'fullSizeImageURL'    : fullSizeImageURL,
                'assetPackManifestURL': assetPackManifestURL,
        ]
    }

    def "can set property provisioningProfiles"() {
        given: "empty options object"
        def emptyOptions = new ExportOptions()

        when:
        emptyOptions.provisioningProfiles = provisioningProfiles

        then:
        def plistXML = emptyOptions.toXMLPropertyList().readLines().collect { it.trim() }.join()
        plistXML.contains(propertyToXmlPlistKey("provisioningProfiles") + valueToXmlPlistValue(provisioningProfiles))

        where:
        provisioningProfiles = [
                "bundle.id.one"  : "profile1",
                "bundle.id.two"  : "profile2",
                "bundle.id.three": "profile3",
        ]
    }

    def "can append provisioning profiles"() {
        given: "export options with profiles configured"
        def exportOptions = new ExportOptions()
        exportOptions.provisioningProfiles = [
                "bundle.id.one": "profile1",
                "bundle.id.two": "profile2",
        ]

        when:
        exportOptions.provisionProfile("bundle.id.three", "profile3")

        then:
        def plistXML = exportOptions.toXMLPropertyList().readLines().collect { it.trim() }.join()
        plistXML.contains(propertyToXmlPlistKey("provisioningProfiles") + valueToXmlPlistValue(provisioningProfiles))

        where:
        provisioningProfiles = [
                "bundle.id.one"  : "profile1",
                "bundle.id.two"  : "profile2",
                "bundle.id.three": "profile3",
        ]
    }

    def "can append provisioning profiles on empty export options"() {
        given: "export options with profiles configured"
        def exportOptions = new ExportOptions()

        when:
        exportOptions.provisionProfile("bundle.id.three", "profile3")

        then:
        def plistXML = exportOptions.toXMLPropertyList().readLines().collect { it.trim() }.join()
        plistXML.contains(propertyToXmlPlistKey("provisioningProfiles") + valueToXmlPlistValue(provisioningProfiles))

        where:
        provisioningProfiles = ["bundle.id.three": "profile3"]
    }

    def "can fetch provisioning profile name with bundle id"() {
        given: "export options with profiles configured"
        def exportOptions = new ExportOptions()
        exportOptions.provisioningProfiles = [
                "bundle.id.one": "profile1",
                "bundle.id.two": "profile2",
        ]

        expect:
        exportOptions.provisionProfile("bundle.id.one") == "profile1"
        exportOptions.provisionProfile("unkown.bundle.id") == null
    }

    def "can fetch provisioning profile name with bundle id from empty export options"() {
        given: "export options with profiles configured"
        def exportOptions = new ExportOptions()

        expect:
        exportOptions.provisionProfile("bundle.id.one") == null
        exportOptions.provisionProfile("unkown.bundle.id") == null
    }
}
