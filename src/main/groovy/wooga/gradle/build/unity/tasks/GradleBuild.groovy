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

package wooga.gradle.build.unity.tasks

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import wooga.gradle.secrets.SecretSpec

class GradleBuild extends SecretsBuild implements SecretSpec {

    private final ListProperty<String> tasks = project.objects.listProperty(String.class)

    @Input
    ListProperty<String> getTasks() {
        tasks
    }

    private final RegularFileProperty initScript

    @Optional
    @InputFile
    RegularFileProperty getInitScript() {
        initScript
    }

    private final Property<File> buildDirBase

    @Internal
    Property<File> getBuildDirBase() {
        buildDirBase
    }

    private final Property<Boolean> cleanBuildDirBeforeBuild

    @Internal
    Property<Boolean> getCleanBuildDirBeforeBuild() {
        cleanBuildDirBeforeBuild
    }

    private final Provider<File> projectCacheDir

    private final ListProperty<String> buildArguments = project.objects.listProperty(String.class)

    @Input
    ListProperty<String> getBuildArguments() {
        buildArguments
    }

    private final Property<String> gradleVersion = project.objects.property(String.class)

    @Input
    Property<String> getGradleVersion() {
        gradleVersion
    }

    GradleBuild() {
        super()
        initScript = project.objects.fileProperty()
        buildDirBase = project.objects.property(File)
        projectCacheDir = buildDirBase.map({ it -> new File(it, ".gradle") })
        cleanBuildDirBeforeBuild = project.objects.property(Boolean)
    }

    @TaskAction
    protected exec() {
        def args = []
        args.addAll(buildArguments.get())

        Boolean cleanBuildDirBeforeBuild = (cleanBuildDirBeforeBuild.isPresent() && cleanBuildDirBeforeBuild.get())
        if (buildDirBase.isPresent() || initScript.isPresent() || cleanBuildDirBeforeBuild) {
            // we need to create a temp init script
            def tempInitScript = new File(getTemporaryDir(), 'initScript.groovy')
            tempInitScript.text = ""

            if (initScript.isPresent()) {
                tempInitScript << initScript.get().getAsFile().text
            } else {
                tempInitScript << getClass().getResource('/buildUnityExportInit.gradle').text
            }

            if (buildDirBase.isPresent() || cleanBuildDirBeforeBuild) {
                if (buildDirBase.isPresent()) {
                    def buildBase = buildDirBase.get()
                    def projectCacheDir = projectCacheDir.get()
                    def projectDir = dir.get().asFile

                    if (!buildBase.isAbsolute()) {
                        buildBase = new File(projectDir, buildBase.path)
                    }

                    if (!projectCacheDir.isAbsolute()) {
                        projectCacheDir = new File(projectDir, projectCacheDir.path)
                    }

                    // provide new build base as property to be picked up by custom init script
                    args << "-Pexport.buildDirBase=${buildBase.getPath()}".toString()
                    args << "--project-cache-dir=${projectCacheDir.getPath()}".toString()
                }

                if (cleanBuildDirBeforeBuild) {
                    args << "-Pexport.deleteBuildDirBeforeBuild=1"
                }
            }

            args << "--init-script=${tempInitScript.getPath()}".toString()
        }

        if (!['--debug', '--info', '--quiet'].any { args.contains(it) }) {
            def startParameter = this.project.gradle.startParameter
            switch (startParameter.logLevel) {
                case LogLevel.DEBUG:
                    args << '--debug'
                    break
                case LogLevel.INFO:
                    args << '--info'
                    break
                case LogLevel.QUIET:
                    args << '--quiet'
                    break
            }
        }

        ProjectConnection connection = GradleConnector.newConnector()
                .forProjectDirectory(dir.get().asFile)
                .useGradleVersion(gradleVersion.get())
                .connect()

        def secrets = environmentSecrets.get()
        environment(secrets)

        try {
            connection.newBuild()
                    .forTasks(*tasks.get().toArray(new String[0]))
                    .withArguments(args)
                    .setEnvironmentVariables(environment.collectEntries { k, v -> [(k): v.toString()] } as Map<String, String>)
                    .setColorOutput(false)
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
        } finally {
            secrets.clear()
            connection.close()
        }
    }
}
