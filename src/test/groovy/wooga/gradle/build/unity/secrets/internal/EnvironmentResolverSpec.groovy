package wooga.gradle.build.unity.secrets.internal

import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables

class EnvironmentResolverSpec extends SecretsResolverSpec<EnvironmentResolver> {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

    @Override
    EnvironmentResolver getSubject() {
        new EnvironmentResolver()
    }

    @Override
    void createSecret(String secretId, byte[] secretValue) {
        def f = File.createTempFile(secretId, "secret")
        f.bytes = secretValue
        f.deleteOnExit()

        environmentVariables.set(secretId.toUpperCase(), f.path)
    }

    @Override
    void createSecret(String secretId, String secretValue) {
        environmentVariables.set(secretId.toUpperCase(), secretValue)
    }

    @Override
    void deleteSecret(String secretId) {
        def v = System.getenv(secretId)
        if (v && new File(v).exists()) {
            new File(v).delete()
        }
        environmentVariables.clear(secretId.toUpperCase())
    }
}
