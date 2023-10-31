package wooga.gradle.build.unity.models

/**
 * Command line options parsed by the Unified Build System
 */
enum BuildRequestOption {

    build,
    configPath,
    config,
    version("--build-version"),
    versionCode("--build-versionCode"),
    outputPath,
    logPath,
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
