package wooga.gradle.build.unity.models

/**
 * Command line options parsed by the Unified Build System
 */
enum BuildRequestOption {

    build,
    configPath,
    config,
    outputPath,
    logPath,

    version("--build-version"),
    versionCode("--build-version-code"),
    toolsVersion,
    commitHash

    final String flag

    BuildRequestOption() {
        this.flag = "--${name()}"
    }

    BuildRequestOption(String flag) {
        this.flag = flag
    }

    @Override
    String toString() {
        flag
    }
}
