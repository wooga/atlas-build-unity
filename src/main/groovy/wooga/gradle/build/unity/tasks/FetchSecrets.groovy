/*
 * Copyright 2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package wooga.gradle.build.unity.tasks


import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import wooga.gradle.secrets.Secret
import wooga.gradle.secrets.SecretResolverException
import wooga.gradle.secrets.internal.Resolver
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.secrets.tasks.SecretsTask

class FetchSecrets extends SecretsTask {

    @Input
    private final ListProperty<String> secretIds

    ListProperty<String> getSecretIds() {
        secretIds
    }

    void setSecretIds(Iterable<String> value) {
        secretIds.set(value)
    }

    void setSecretIds(String... value) {
        secretIds.set(value.toList())
    }

    FetchSecrets secretIds(String... value) {
        secretIds.addAll(project.provider({ value.toList() }))
    }

    FetchSecrets secretIds(Iterable<String> value) {
        secretIds.addAll(project.provider({ value.toList() }))
    }

    FetchSecrets secretId(String value) {
        secretIds.add(value)
    }

    @Optional
    @Input
    protected Class<Resolver> getResolverType() {
        if(resolver.present) {
            resolver.get().class as Class<Resolver>
        }
    }

    @OutputFile
    private final RegularFileProperty secretsFile

    RegularFileProperty getSecretsFile(){
        secretsFile
    }

    FetchSecrets() {
        secretIds = project.objects.listProperty(String)
        secretsFile = project.objects.fileProperty()
    }

    @TaskAction
    protected void fetchSecrets() {
        logger.info("Fetch secrets ${secretIds.get().join(", ")}")
        Secrets secrets = new Secrets()
        def resolver = resolver.getOrNull()
        def key = secretsKey.get()

        if(resolver) {
            for(String secretId in secretIds.get()) {
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
