package wooga.gradle.fastlane

class FastlaneSpec extends IntegrationSpec {
    File fastlaneMock
    File fastlaneMockPath

    def setupFastlaneMock() {
        fastlaneMockPath = File.createTempDir("fastlane", "mock")

        def path = System.getenv("PATH")
        environmentVariables.clear("PATH")
        String newPath = "${fastlaneMockPath}${File.pathSeparator}${path}"
        environmentVariables.set("PATH", newPath)
        assert System.getenv("PATH") == newPath


        fastlaneMock = createFile("fastlane", fastlaneMockPath)
        fastlaneMock.executable = true
        fastlaneMock << """
            #!/usr/bin/env bash
            echo \$@
            env
        """.stripIndent()
    }

    def setup() {
        setupFastlaneMock()
    }
}
