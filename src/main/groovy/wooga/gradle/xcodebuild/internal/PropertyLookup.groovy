package wooga.gradle.xcodebuild.internal

class PropertyLookup {
    final String env
    final String property
    final String defaultValue

    PropertyLookup(String env, String property, String defaultValue) {
        this.env = env
        this.property = property
        this.defaultValue = defaultValue
    }
}
