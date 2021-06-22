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

import com.sun.org.apache.xpath.internal.operations.Bool
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
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
import wooga.gradle.secrets.internal.Secrets

import javax.crypto.spec.SecretKeySpec

class GradleBuild extends DefaultTask implements SecretSpec {

    private final DirectoryProperty dir = project.objects.directoryProperty()

    @Internal
    DirectoryProperty getDir() {
        dir
    }

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

    private final Property<SecretKeySpec> secretsKey

    @Optional
    @Input
    Property<SecretKeySpec> getSecretsKey() {
        secretsKey
    }

    void setSecretsKey(SecretKeySpec key) {
        secretsKey.set(key)
    }

    GradleBuild setSecretsKey(String keyFile) {
        setSecretsKey(project.file(keyFile))
    }

    GradleBuild setSecretsKey(File keyFile) {
        setSecretsKey(new SecretKeySpec(keyFile.bytes, "AES"))
    }

    @Override
    GradleBuild secretsKey(SecretKeySpec key) {
        setSecretsKey(key)
    }

    @Override
    GradleBuild secretsKey(String keyFile) {
        return setSecretsKey(keyFile)
    }

    @Override
    GradleBuild secretsKey(File keyFile) {
        return setSecretsKey(keyFile)
    }

    private final RegularFileProperty secretsFile

    @Optional
    @InputFile
    RegularFileProperty getSecretsFile() {
        secretsFile
    }

    protected final Provider<Secrets> secrets

    protected final Provider<Secrets.EnvironmentSecrets> environmentSecrets

    final Map<String, Object> environment

    @Internal
    Map<String, Object> getEnvironment() {
        environment
    }

    void setEnvironment(Map<String, ?> environment) {
        this.environment.clear()
        this.environment.putAll(environment)
    }

    GradleBuild environment(Map<String, ?> environment) {
        this.environment.putAll(environment)
        this
    }

    GradleBuild environment(String key, Object value) {
        this.environment.put(key, value)
        this
    }

    GradleBuild() {
        initScript = project.objects.fileProperty()
        buildDirBase = project.objects.property(File)
        projectCacheDir = buildDirBase.map({it -> new File(it, ".gradle")})
        cleanBuildDirBeforeBuild = project.objects.property(Boolean)

        secretsKey = project.objects.property(SecretKeySpec.class)
        secretsFile = project.objects.fileProperty()
        secrets = secretsFile.map(new Transformer<Secrets, RegularFile>() {
            @Override
            Secrets transform(RegularFile secretsFile) {
                Secrets.decode(secretsFile.asFile.text)
            }
        })

        environmentSecrets = project.provider({
            if (secrets.present && secretsKey.present) {
                def s = secrets.get()
                def key = secretsKey.get()
                return s.encodeEnvironment(key)
            } else {
                new Secrets.EnvironmentSecrets()
            }
        })

        environment = [:]
        environment.putAll(System.getenv())
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

            if(initScript.isPresent()) {
                tempInitScript << initScript.get().getAsFile().text
            } else {
                tempInitScript << getClass().getResource('/buildUnityExportInit.gradle').text
            }

            if (buildDirBase.isPresent() || cleanBuildDirBeforeBuild) {
                if (buildDirBase.isPresent()) {
                    def buildBase = buildDirBase.get()
                    def projectCacheDir = projectCacheDir.get()
                    def projectDir = dir.get().asFile

                    if(!buildBase.isAbsolute()) {
                        buildBase = new File(projectDir, buildBase.path)
                    }

                    if(!projectCacheDir.isAbsolute()) {
                        projectCacheDir = new File(projectDir, projectCacheDir.path)
                    }

                    // provide new build base as property to be picked up by custom init script
                    args << "-Pexport.buildDirBase=${buildBase.getPath()}".toString()
                    args << "--project-cache-dir=${projectCacheDir.getPath()}".toString()
                }

                if(cleanBuildDirBeforeBuild) {
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
