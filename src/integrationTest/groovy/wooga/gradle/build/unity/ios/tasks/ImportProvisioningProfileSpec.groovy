/*
 * Copyright 2019 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.build.unity.ios.tasks

import org.gradle.internal.impldep.org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import spock.lang.Issue
import spock.lang.Requires
import wooga.gradle.build.IntegrationSpec

@Requires({ os.macOs })
class ImportProvisioningProfileSpec extends IntegrationSpec {

    File fastlaneMock
    File fastlaneMockPath

    def setupFastlaneMock() {
        fastlaneMockPath = File.createTempDir("fastlane","mock")

        def path = System.getenv("PATH")
        environmentVariables.clear("PATH")
        String newPath = "${fastlaneMockPath}${File.pathSeparator}${path}"
        environmentVariables.set("PATH", newPath)
        assert System.getenv("PATH") == newPath


        fastlaneMock = createFile("fastlane", fastlaneMockPath)
        fastlaneMock.executable = true
        String osName = System.getProperty("os.name").toLowerCase()
        if (osName.contains("windows")) {
            fastlaneMock << """
                @echo off
                echo %*
            """.stripIndent()
        }
        else
        {
            fastlaneMock << """
                #!/usr/bin/env bash
                echo \$@
            """.stripIndent()
        }
    }

    def setup() {
        buildFile << """
            task customImportProfiles(type: wooga.gradle.build.unity.ios.tasks.ImportProvisioningProfile) {
                appIdentifier = "com.test.testapp"
                teamId = "fakeTeamId"
                profileName = "signing.mobileprovisioning"
                destinationDir = file("build")
            }
        """.stripIndent()

        setupFastlaneMock()
    }

    @Issue("https://github.com/wooga/atlas-build-unity/issues/38")
    def "task :#taskToRun is never up-to-date"() {
        given: "call import tasks once"
        def r = runTasks(taskToRun)

        when: "no parameter changes"
        def result = runTasksSuccessfully(taskToRun)

        then:
        !result.wasUpToDate(taskToRun)

        where:
        taskToRun = "customImportProfiles"
    }
}
