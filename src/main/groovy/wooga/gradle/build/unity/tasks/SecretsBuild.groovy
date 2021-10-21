package wooga.gradle.build.unity.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import wooga.gradle.secrets.SecretSpec
import wooga.gradle.secrets.internal.Secrets

import javax.crypto.spec.SecretKeySpec

abstract class SecretsBuild extends DefaultTask implements SecretSpec {

    private final DirectoryProperty dir

    @Internal
    DirectoryProperty getDir() {
        dir
    }

    protected final Property<SecretKeySpec> secretsKey

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

    protected final RegularFileProperty secretsFile

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

    SecretsBuild environment(Map<String, ?> environment) {
        this.environment.putAll(environment)
        this
    }

    SecretsBuild environment(String key, Object value) {
        this.environment.put(key, value)
        this
    }

    protected SecretsBuild() {
        dir = project.objects.directoryProperty()
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
}
