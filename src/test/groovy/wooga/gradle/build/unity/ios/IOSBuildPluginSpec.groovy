package wooga.gradle.build.unity.ios

import nebula.test.ProjectSpec
import spock.lang.Requires
import spock.lang.Unroll
import wooga.gradle.build.unity.ios.internal.DefaultIOSBuildPluginExtension

@Requires({os.macOs})
class IOSBuildPluginSpec extends ProjectSpec {
    public static final String PLUGIN_NAME = 'net.wooga.build-unity-ios'

    def 'Creates the [iosBuild] extension'() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(IOSBuildPlugin.EXTENSION_NAME)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(IOSBuildPlugin.EXTENSION_NAME)
        extension instanceof DefaultIOSBuildPluginExtension
    }

    @Unroll
    def 'extension returns #defaultValue value for property #property'() {
        given:
        project.plugins.apply(PLUGIN_NAME)

        and: "the extension"
        DefaultIOSBuildPluginExtension extension = project.extensions.findByName(IOSBuildPlugin.EXTENSION_NAME) as DefaultIOSBuildPluginExtension

        expect:
        extension.getProperty(property) == defaultValue

        where:
        property                | defaultValue
        "keychainPassword"      | null
        "certificatePassphrase" | null
        "appIdentifier"         | null
        "teamId"                | null
        "scheme"                | null
        "configuration"         | null
        "provisioningName"      | null
        "adhoc"                 | false
    }
}
