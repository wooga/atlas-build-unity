package wooga.gradle.build.unity.ios

import com.wooga.gradle.test.IntegrationSpec
import wooga.gradle.xcodebuild.config.ExportOptions

abstract class IOSBuildIntegrationSpec extends IntegrationSpec {

    abstract String getSubjectUnderTestName()

    abstract String getSubjectUnderTestTypeName()

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        switch (type) {
            case "ExportOptions.DistributionManifest":
                def rawOptions = (ExportOptions.DistributionManifest) rawValue
                def appUrl = wrapValueBasedOnType(rawOptions.appURL, String)
                def displayImageURL = wrapValueBasedOnType(rawOptions.displayImageURL, String)
                def fullSizeImageURL = wrapValueBasedOnType(rawOptions.fullSizeImageURL, String)
                def assetPackManifestURL = wrapValueBasedOnType(rawOptions.assetPackManifestURL, String)

                return "${ExportOptions.DistributionManifest.name}.distributionManifest(${appUrl}, ${displayImageURL}, ${fullSizeImageURL}, ${assetPackManifestURL})"
            default:
                return rawValue.toString()
        }
    }
}
