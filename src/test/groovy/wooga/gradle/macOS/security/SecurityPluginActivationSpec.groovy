package wooga.gradle.macOS.security

import nebula.test.PluginProjectSpec

class SecurityPluginActivationSpec  extends PluginProjectSpec {
    @Override
    String getPluginName() {
        return 'net.wooga.macos-security'
    }
}
