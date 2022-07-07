package wooga.gradle.macOS.security.tasks


import wooga.gradle.macOS.security.SecurityTaskIntegrationSpec

abstract class InteractiveSecurityTaskIntegrationSpec<T extends AbstractInteractiveSecurityTask> extends SecurityTaskIntegrationSpec<T> {
    String keychainPassword = "123456"
}
