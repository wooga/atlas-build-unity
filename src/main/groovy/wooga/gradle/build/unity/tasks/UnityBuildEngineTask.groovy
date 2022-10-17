package wooga.gradle.build.unity.tasks


import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.build.unity.UBSVersion
import wooga.gradle.build.unity.internal.BuildEngineArgs
import wooga.gradle.build.unity.models.UBSCompatibility
import wooga.gradle.build.unity.models.UnityBuildEngineSpec
import wooga.gradle.secrets.SecretSpec
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.unity.UnityTask

import javax.crypto.spec.SecretKeySpec


abstract class UnityBuildEngineTask extends UnityTask implements SecretSpec, UnityBuildEngineSpec {

    UnityBuildEngineTask() {
    }

    protected BuildEngineArgs defaultArgs() {
        ubsCompatibilityVersion.convention(UBSVersion.v100)
        Provider<Directory> logDir = gradleDirectoryFrom(logPath)

        def secrets = secretsFile.map { RegularFile secretsFile ->
            Secrets.decode(secretsFile.asFile.text)
        }
        def environmentSecrets = project.provider({
            generateSecretsEnvironment(secrets, secretsKey)
        }.memoize())


        def buildEngineArgs = new BuildEngineArgs(project.providers, exportMethodName)
        buildEngineArgs.with {
            addArg("--build", build)
            addArg("--configPath", configPath)
            addArg("--config", config)
            addArg("--outputPath", outputDirectory.map { out -> out.asFile.path })
            addArg("--logPath", logDir.map { out -> out.asFile.path })
            addRawArgs(customArguments)
            addEnvs(environmentSecrets)
        }
        return buildEngineArgs
    }

    protected void setupExecution(BuildEngineArgs unityArgs) {
        environment.putAll(unityArgs.environment)
        unityArgs.argsProviders.each { Provider<List<String>> argsProvider ->
            additionalArguments.addAll(argsProvider)
        }
        additionalArguments.add("-executeMethod")
        additionalArguments.add(unityArgs.method)
    }

    protected Secrets.EnvironmentSecrets generateSecretsEnvironment(Provider<Secrets> secrets,
                                                                    Property<SecretKeySpec> secretsKey) {
        if (secrets.present && secretsKey.present) {
            def s = secrets.get()
            def key = secretsKey.get()
            return s.encodeEnvironment(key)
        } else {
            return new Secrets.EnvironmentSecrets()
        }
    }

    protected Provider<Directory> gradleDirectoryFrom(Property<String> pathProperty) {
        return pathProperty.map { path ->
            project.layout.projectDirectory.dir(path)
        }
    }


}
