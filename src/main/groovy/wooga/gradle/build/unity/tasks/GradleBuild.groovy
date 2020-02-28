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

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
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

class GradleBuild extends DefaultTask {

    @Internal
    final DirectoryProperty dir = project.layout.directoryProperty()

    @Input
    final ListProperty<String> tasks = project.objects.listProperty(String.class)

    @Optional
    @InputFile
    final RegularFileProperty initScript

    @Optional
    @Internal
    final DirectoryProperty buildDirBase

    @Internal
    final Property<Boolean> cleanBuildDirBeforeBuild

    @Optional
    @Internal
    private final Provider<Directory> projectCacheDir

    @Input
    final ListProperty<String> buildArguments = project.objects.listProperty(String.class)

    @Input
    final Property<String> gradleVersion = project.objects.property(String.class)

    GradleBuild() {
        initScript = project.layout.fileProperty()
        buildDirBase = project.layout.directoryProperty()
        projectCacheDir = buildDirBase.dir('.gradle')
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

            if (buildDirBase.isPresent() || cleanBuildDirBeforeBuild) {
                tempInitScript << getClass().getResource('/buildUnityExportInit.gradle').text

                if (buildDirBase.isPresent()) {
                    def buildBase = buildDirBase.get().getAsFile()
                    // provide new build base as property to be picked up by custom init script
                    args << "-Pexport.buildDirBase=${buildBase.getAbsoluteFile().getPath()}".toString()
                    args << "--project-cache-dir=${projectCacheDir.get().getAsFile().getPath()}".toString()
                }

                if(cleanBuildDirBeforeBuild) {
                    args << "-Pexport.deleteBuildDirBeforeBuild=1"
                }
            }

            if (initScript.isPresent()) {
                tempInitScript << getClass().getResource('/exportMarker.gradle').text
                tempInitScript << initScript.get().getAsFile().text
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

        try {
            connection.newBuild()
                    .forTasks(*tasks.get().toArray(new String[0]))
                    .withArguments(args)
                    .setColorOutput(false)
                    .setStandardOutput(System.out)
                    .setStandardError(System.err)
                    .run()
        } finally {
            connection.close()
        }
    }
}
