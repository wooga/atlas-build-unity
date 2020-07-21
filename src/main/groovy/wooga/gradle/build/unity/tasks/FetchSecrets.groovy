/*
 * Copyright 2018-2020 Wooga GmbH
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

import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.build.unity.SecretSpec
import wooga.gradle.build.unity.secrets.Secret
import wooga.gradle.build.unity.secrets.SecretResolver
import wooga.gradle.build.unity.secrets.SecretResolverException
import wooga.gradle.build.unity.secrets.internal.Resolver
import wooga.gradle.build.unity.secrets.internal.Secrets
import wooga.gradle.unity.utils.GenericUnityAsset

import javax.crypto.spec.SecretKeySpec

class FetchSecrets extends DefaultTask implements SecretSpec {

    private GenericUnityAsset appConfig

    @Input
    final Property<String> appConfigSecretsKey

    void setAppConfigSecretsKey(String key) {
        appConfigSecretsKey.set(key)
    }

    SecretSpec appConfigSecretsKey(String key) {
        setAppConfigSecretsKey(key)
        return this
    }
    @Input
    final Property<SecretKeySpec> secretsKey

    void setSecretsKey(SecretKeySpec key) {
        secretsKey.set(key)
    }

    FetchSecrets setSecretsKey(String keyFile) {
        setSecretsKey(project.file(keyFile))
    }

    FetchSecrets setSecretsKey(File keyFile) {
        setSecretsKey(new SecretKeySpec(keyFile.bytes, "AES"))
    }

    @Override
    FetchSecrets secretsKey(SecretKeySpec key) {
        setSecretsKey(key)
    }

    @Override
    FetchSecrets secretsKey(String keyFile) {
        return setSecretsKey(keyFile)
    }

    @Override
    FetchSecrets secretsKey(File keyFile) {
        return setSecretsKey(keyFile)
    }

    @Optional
    @Input
    protected Class<Resolver> getResolverType() {
        if(resolver.present) {
            resolver.get().class as Class<Resolver>
        }
    }

    @Internal
    final Property<SecretResolver> resolver

    void setResolver(SecretResolver value) {
        resolver.set(value)
    }

    void resolver(SecretResolver value) {
        setResolver(value)
    }

    @InputFile
    final RegularFileProperty appConfigFile

    @OutputFile
    final RegularFileProperty secretsFile

    @Internal
    final Provider<String> appConfigName

    @Internal("loaded app config asset")
    protected GenericUnityAsset getAppConfig() {
        if(!appConfig) {
            appConfig = new GenericUnityAsset(appConfigFile.get().asFile)
            if(!appConfig.isValid()) {
                throw new StopExecutionException('provided appConfig is invalid')
            }
        }

        appConfig
    }

    @Internal("read from appConfig file")
    List<String> getSecretIds() {
        def appConfig = getAppConfig()
        if(appConfig.containsKey(appConfigSecretsKey.get())) {
            return appConfig[appConfigSecretsKey.get()] as List<String>
        }
        []
    }

    FetchSecrets() {
        appConfigFile = newInputFile()
        secretsFile = newOutputFile()
        appConfigName = appConfigFile.map { FilenameUtils.removeExtension(it.asFile.name) }
        resolver = project.objects.property(SecretResolver)
        secretsKey = project.objects.property(SecretKeySpec)
        appConfigSecretsKey = project.objects.property(String)
    }

    @TaskAction
    protected void fetchSecrets() {
        logger.info("Fetch secrets for appConfig: ${appConfigName.get()}")
        Secrets secrets = new Secrets()
        def resolver = resolver.getOrNull()
        def key = secretsKey.get()

        if(resolver) {
            for(String secretId in getSecretIds()) {
                logger.info("Fetch secret: ${secretId}")
                try {
                    Secret secret = resolver.resolve(secretId)
                    secrets.putSecret(secretId, secret, key)
                } catch(SecretResolverException e){
                    throw new ScriptException("unable to fetch secret ${secretId}", e)
                }
            }
        }
        this.secretsFile.get().asFile.text = secrets.encode()
    }
}
