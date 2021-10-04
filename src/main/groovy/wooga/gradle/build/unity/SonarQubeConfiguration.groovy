package wooga.gradle.build.unity

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.sonarqube.gradle.SonarQubeExtension
import org.sonarqube.gradle.SonarQubeProperties
import wooga.gradle.dotnetsonar.tasks.BuildSolution
import wooga.gradle.unity.UnityPlugin
import wooga.gradle.unity.UnityPluginExtension

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class SonarQubeConfiguration {

    private Project project
    public String sonarTaskName
    public String buildTaskName


    SonarQubeConfiguration(Project project) {
        this.project = project
        this.sonarTaskName = "sonarqube"
        this.buildTaskName = "sonarBuildUnity"
    }

    void configure(UnityPluginExtension unityExt,
                   SonarQubeExtension sonarExt) {
        def unityTestTask = project.tasks.named(UnityPlugin.Tasks.test.toString())
        def createSolutionTask = project.tasks.named(UnityPlugin.Tasks.generateSolution.toString())

        unityExt.enableTestCodeCoverage = true
        def sonarBuild = project.tasks.register(buildTaskName, BuildSolution) {task ->
            task.dependsOn(createSolutionTask)
            task.mustRunAfter(unityTestTask)
            setupSonarBuildUnityDefaults(task, unityExt)
        }
        project.tasks.register(sonarTaskName) {task ->
            task.dependsOn(unityTestTask, sonarBuild)
            task.mustRunAfter(unityTestTask)
        }
        project.afterEvaluate { //needs to be done after evaluate to be able to fill reportsDir property
            def assetsDir = unityExt.assetsDir.get().asFile.path
            def reportsDir = unityExt.reportsDir.get().asFile.path
            sonarExt.properties(sonarqubeUnityDefaults(assetsDir, reportsDir))
        }
    }
    private void setupSonarBuildUnityDefaults(BuildSolution task, UnityPluginExtension unityExt) {
        def propsFixResource = SonarQubeConfiguration.class.getResourceAsStream("/atlas-build-unity.project-fixes.props")
        def propsFixTmpFile = File.createTempFile("atlas-build-unity", ".project-fixes.props")
        propsFixResource.withStream {inputStream ->
            Files.copy(inputStream, Paths.get(propsFixTmpFile.absolutePath), StandardCopyOption.REPLACE_EXISTING)
        }

        task.solution.convention(project.layout.projectDirectory.file("${project.name}.sln"))
        task.dotnetExecutable.convention(unityExt.dotnetExecutable)
        task.addEnvironment("FrameworkPathOverride",
                unityExt.monoFrameworkDir.map{it.asFile.absolutePath} )
        task.extraArgs.add("/p:CustomBeforeMicrosoftCommonProps=${propsFixTmpFile.absolutePath}")

    }

    private static Action<? extends SonarQubeProperties> sonarqubeUnityDefaults(String assetsDir, String reportsDir) {
        return {
            addPropertyIfNotExists(it, "sonar.cpd.exclusions", "${assetsDir}/**/Tests/**")
            addPropertyIfNotExists(it, "sonar.coverage.exclusions", "${assetsDir}/**/Tests/**")
            addPropertyIfNotExists(it, "sonar.exclusions", "${assetsDir}/Paket.Unity3D/**")
            addPropertyIfNotExists(it, "sonar.cs.nunit.reportsPaths", "${reportsDir}/**/*.xml")
            addPropertyIfNotExists(it, "sonar.cs.opencover.reportsPaths", "${reportsDir}/**/*.xml")
        }
    }

    private static void addPropertyIfNotExists(SonarQubeProperties properties, String key, Object value) {
        if(!properties.properties.containsKey(key)) {
            properties.property(key, value)
        }
    }
}
