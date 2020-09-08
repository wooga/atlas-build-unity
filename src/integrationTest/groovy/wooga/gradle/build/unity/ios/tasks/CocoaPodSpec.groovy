package wooga.gradle.build.unity.ios.tasks

import wooga.gradle.build.IntegrationSpec

class CocoaPodSpec extends IntegrationSpec {
    File podMock
    File podMockPath

    def setupPodMock() {
        podMockPath = File.createTempDir("pod", "mock")

        def path = System.getenv("PATH")
        environmentVariables.clear("PATH")
        String newPath = "${podMockPath}${File.pathSeparator}${path}"
        environmentVariables.set("PATH", newPath)
        assert System.getenv("PATH") == newPath

        podMock = createFile("pod", podMockPath)
        podMock.executable = true
        podMock << """
            #!/usr/bin/env bash
            echo \$@
            env
        """.stripIndent()
    }

    def setup() {
        setupPodMock()
    }
}
