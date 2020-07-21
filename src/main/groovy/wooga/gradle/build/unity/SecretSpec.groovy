package wooga.gradle.build.unity

import org.gradle.api.provider.Property

import javax.crypto.spec.SecretKeySpec

interface SecretSpec<T extends SecretSpec> {

    Property<SecretKeySpec> getSecretsKey()
    void setSecretsKey(SecretKeySpec key)
    T setSecretsKey(String keyFile)
    T setSecretsKey(File keyFile)

    T secretsKey(SecretKeySpec key)
    T secretsKey(String keyFile)
    T secretsKey(File keyFile)
}
