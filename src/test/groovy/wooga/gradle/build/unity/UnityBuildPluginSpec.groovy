/*
 * Copyright 2018 Wooga GmbH
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

package wooga.gradle.build.unity

import nebula.test.ProjectSpec
import org.gradle.api.DefaultTask
import org.gradle.launcher.daemon.protocol.Build
import org.sonarqube.gradle.SonarPropertyComputer
import org.sonarqube.gradle.SonarQubeExtension
import spock.lang.Ignore
import spock.lang.Unroll
import wooga.gradle.build.unity.internal.DefaultUnityBuildPluginExtension
import wooga.gradle.build.unity.tasks.UnityBuildPlayerTask
import wooga.gradle.dotnetsonar.SonarScannerExtension
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.unity.UnityPluginExtension

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class UnityBuildPluginSpec extends ProjectSpec {
    public static final String PLUGIN_NAME = 'net.wooga.build-unity'

    def 'Creates the [unity] extension'() {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(UnityBuildPlugin.EXTENSION_NAME)

        when:
        project.plugins.apply(PLUGIN_NAME)


        then:
        def extension = project.extensions.findByName(UnityBuildPlugin.EXTENSION_NAME)
        extension instanceof DefaultUnityBuildPluginExtension
    }

    @Unroll("creates the task #taskName")
    def 'Creates needed tasks'(String taskName, Class taskType) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        when:
        project.plugins.apply(PLUGIN_NAME)
        def task
        project.afterEvaluate {
            task = project.tasks.findByName(taskName)
        }

        then:
        project.evaluate()
        taskType.isInstance(task)

        where:
        taskName                              | taskType
        "publish"                             | DefaultTask
        "assemble"                            | DefaultTask
        "build"                               | DefaultTask
        "check"                               | DefaultTask
        "sonarqube"                           | DefaultTask
        "sonarBuildUnity"                     | BuildSolution
    }

    @Unroll
    def 'adds pluginToAdd #pluginToAdd'(String pluginToAdd) {
        given:
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.plugins.hasPlugin(pluginToAdd)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        project.plugins.hasPlugin(pluginToAdd)

        where:
        pluginToAdd << ['base', 'net.wooga.unity']
    }

    def "configures sonarqube extension"() {
        given: "project without plugin applied"
        assert !project.plugins.hasPlugin(PLUGIN_NAME)

        when: "applying atlas-build-unity plugin"
        project.plugins.apply(PLUGIN_NAME)
        project.evaluate()

        then:
        def sonarExt = project.extensions.getByType(SonarScannerExtension)
        def unityExt = project.extensions.getByType(UnityPluginExtension)
        and: "sonarqube extension is configured with defaults"
        def properties = sonarExt.computeSonarProperties(project)
        def assetsDir = unityExt.assetsDir.get().asFile.path
        def reportsDir = unityExt.reportsDir.get().asFile.path
        properties["sonar.exclusions"] == "${assetsDir}/Paket.Unity3D/**"
        properties["sonar.cpd.exclusions"] == "${assetsDir}/**/Tests/**"
        properties["sonar.coverage.exclusions"] == "${assetsDir}/**/Tests/**"
        properties["sonar.cs.nunit.reportsPaths"] == "${reportsDir}/**/*.xml"
        properties["sonar.cs.opencover.reportsPaths"] == "${reportsDir}/**/*.xml"
    }

    def "configures sonarBuildUnity task"() {
        given: "project without plugin applied"
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        and: "props file with fixes to run unity project on msbuild properly"


        when: "applying atlas-build-unity plugin"
        project.plugins.apply(PLUGIN_NAME)

        then:
        def unityExt = project.extensions.getByType(UnityPluginExtension)
        def buildTask = project.tasks.getByName("sonarBuildUnity") as BuildSolution
        buildTask.solution.get().asFile == new File(projectDir, "${project.name}.sln")
        buildTask.dotnetExecutable.getOrElse(null) == unityExt.dotnetExecutable.getOrElse(null)
        buildTask.environment.getting("FrameworkPathOverride").getOrElse(null) ==
                unityExt.monoFrameworkDir.map { it.asFile.absolutePath}.getOrElse(null)
        buildTask.extraArgs.get().any {
            it.startsWith("/p:CustomBeforeMicrosoftCommonProps=") &&
            it.endsWith(".project-fixes.props")
        }
    }
}
