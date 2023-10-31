package wooga.gradle.build.unity.tasks


import org.gradle.api.file.*
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import wooga.gradle.build.unity.internal.BuildRequestArguments
import wooga.gradle.build.unity.models.BuildRequestOption
import wooga.gradle.build.unity.models.UnityBuildEngineSpec
import wooga.gradle.secrets.SecretSpec
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.unity.UnityTask

import javax.crypto.spec.SecretKeySpec

/**
 * The base task that can run builds via the Unified Build System, our Unity-side package for executing builds in the Unity Editor.
 */
class BuildUnityTask extends UnityTask implements SecretSpec, UnityBuildEngineSpec {

    BuildUnityTask() {
        additionalArguments.addAll(project.provider {
            def args  = new BuildRequestArguments(project.providers)
            appendBuildArguments(args)
            def list = args.getArguments().get()
            list
        })
        environment.putAll(environmentSecrets)

    }

    protected void appendBuildArguments(BuildRequestArguments args) {
        Provider<Directory> logDir = gradleDirectoryFrom(logPath)
        args.with {
            addArg(BuildRequestOption.build.flag, build)
            addArg(BuildRequestOption.configPath.flag, configPath)
            addArg(BuildRequestOption.config.flag, config)
            addArg(BuildRequestOption.outputPath.flag, outputDirectory.asFile.map { out -> out.path })
            addArg(BuildRequestOption.logPath.flag, logDir.map { out -> out.asFile.path })
        }
    }

    @Internal
    Provider<Secrets.EnvironmentSecrets> getEnvironmentSecrets() {
        def secrets = secretsFile.map { RegularFile secretsFile ->
            Secrets.decode(secretsFile.asFile.text)
        }
        def environmentSecrets = project.provider({
            generateSecretsEnvironment(secrets, secretsKey)
        }.memoize())
        environmentSecrets
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
