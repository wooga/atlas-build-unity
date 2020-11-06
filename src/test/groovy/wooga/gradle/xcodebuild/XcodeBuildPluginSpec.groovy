package wooga.gradle.xcodebuild

import nebula.test.ProjectSpec
import wooga.gradle.build.unity.UnityBuildPlugin
import wooga.gradle.build.unity.internal.DefaultUnityBuildPluginExtension

class XcodeBuildPluginSpec extends ProjectSpec {
    public static final String PLUGIN_NAME = 'net.wooga.xcodebuild'

    def 'Creates the [xcodebuild] extension'() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(XcodeBuildPlugin.EXTENSION_NAME)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(UnityBuildPlugin.EXTENSION_NAME)
        extension instanceof DefaultUnityBuildPluginExtension
    }
}
