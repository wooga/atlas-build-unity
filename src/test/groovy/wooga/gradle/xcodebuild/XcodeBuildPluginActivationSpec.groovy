package wooga.gradle.xcodebuild

import nebula.test.PluginProjectSpec

class XcodeBuildPluginActivationSpec extends PluginProjectSpec {
    @Override
    String getPluginName() {
        return 'net.wooga.xcodebuild'
    }
}
