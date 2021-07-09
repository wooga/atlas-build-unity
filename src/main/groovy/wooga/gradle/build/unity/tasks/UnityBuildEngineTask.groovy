package wooga.gradle.build.unity.tasks

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.unity.UnityTask

import javax.crypto.spec.SecretKeySpec

class UnityBuildEngineTask extends UnityTask {

    private static final String DEFAULT_UNITY_METHOD = "Wooga.UnifiedBuildSystem.Editor.BuildEngine.BuildFromEnvironment"

    @Input
    final Property<String> build = project.objects.property(String)
    @Input
    final Property<String> unityMethodName = project.objects.property(String).convention(DEFAULT_UNITY_METHOD)
    @Input
    final Property<String> outputPath = project.objects.property(String).convention(new File(project.buildDir, "export").path)
    @Optional @InputFile
    final RegularFileProperty secretsFile = project.objects.fileProperty()
    @Optional @Input
    final Property<SecretKeySpec> secretsKey = project.objects.property(SecretKeySpec.class)
    @Optional @Input
    final ListProperty<Object> extraArgs = project.objects.listProperty(Object).convention(new ArrayList<String>())

    UnityBuildEngineTask() {
        Provider<File> outputDir = outputPath.map { path ->
            def pathFile = new File(path)
            return pathFile.isAbsolute() ? pathFile : new File(project.projectDir, path)
        }

        def customArgsProvider = project.provider {
            List<Object> args = [["outputPath": outputDir.get().path], *extraArgs.get()]
            createCustomArgs(build.get(), args)
        }

        def secrets = secretsFile.map { RegularFile secretsFile ->
            Secrets.decode(secretsFile.asFile.text)
        }

        def environmentSecrets = project.provider({
            generateSecretsEnvironment(secrets, secretsKey)
        }.memoize())

        environment.putAll(environmentSecrets)

        additionalArguments.add(customArgsProvider)
        additionalArguments.add("-executeMethod")
        additionalArguments.add(unityMethodName)
    }

    static String createCustomArgs(String buildName, List<Object> args) {
        def argsString = args.collect { buildArg -> ArgItems.createArgsString(buildArg) }.join(" ")
        return "-CustomArgs:build=${buildName} ${argsString}"
    }

    def generateSecretsEnvironment(Provider<Secrets> secrets, Property<SecretKeySpec> secretsKey) {
        if (secrets.present && secretsKey.present) {
            def s = secrets.get()
            def key = secretsKey.get()
            return s.encodeEnvironment(key)
        } else {
            return new Secrets.EnvironmentSecrets()
        }
    }
}