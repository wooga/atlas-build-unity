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

import org.yaml.snakeyaml.Yaml
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.UnityBuildPlugin
import wooga.gradle.build.unity.secrets.internal.EncryptionSpecHelper

class FetchSecretsTaskIntegrationSpec extends IntegrationSpec {
    def setup() {
        def assets = new File(projectDir, "Assets")
        def appConfigsDir = new File(assets, "CustomConfigs")
        appConfigsDir.mkdirs()

        def appConfig = ['MonoBehaviour': ['bundleId': 'net.wooga.test', 'batchModeBuildTarget': 'android']]
        def appConfigWithSecrets = ['MonoBehaviour': ['bundleId': 'net.wooga.test', 'batchModeBuildTarget': 'android', 'secrets': [
                'net_wooga_testCredential',
                'net_wooga_testCredential2',
                'net_wooga_testCredential3',
                'net_wooga_testCredential4'
        ]]]
        def appConfigWithEmptySecrets = ['MonoBehaviour': ['bundleId': 'net.wooga.test', 'batchModeBuildTarget': 'android', 'secrets': []]]

        ["legacyConfig": appConfig, "withSecrets": appConfigWithSecrets, "emptySecrets": appConfigWithEmptySecrets]
                .each { name, config ->
                    Yaml yaml = new Yaml()
                    def f = createFile("${name}.asset", appConfigsDir)
                    f << yaml.dump(config)
                }

        buildFile << """
            ${applyPlugin(UnityBuildPlugin)}

            import wooga.gradle.build.unity.secrets.internal.Resolver
            import wooga.gradle.build.unity.secrets.internal.DefaultSecret
            import wooga.gradle.build.unity.secrets.SecretResolver
            import wooga.gradle.build.unity.secrets.Secret

            task("fetchSecretsCustom", type: wooga.gradle.build.unity.tasks.FetchSecrets) {
                appConfigFile = file('Assets/CustomConfigs/withSecrets.asset')
            }
        """.stripIndent()
    }

    def "task is Up-To-Date when secret key is cached"() {
        given: "a secret key"
        def key = EncryptionSpecHelper.createSecretKey(this.class.name)
        def keyFile = createFile("secrets.key", projectDir)
        keyFile.bytes = key.encoded

        and: "the key configured"
        buildFile << """
            fetchSecretsCustom.secretsKey = "${escapedPath(keyFile.path)}"
        """

        and: "a fake resolver"
        buildFile << """
            fetchSecretsCustom.resolver = Resolver.withClosure {
                if(it == "net_wooga_testCredential2") {
                    return it.toUpperCase().bytes
                } else {
                    return it.toUpperCase()
                } 
            }
        """.stripIndent()

        and: "a future secrets file"
        def secretsFile = new File(projectDir, "build/secret/fetchSecretsCustom/secrets.yml")
        assert !secretsFile.exists()

        when:
        def result = runTasksSuccessfully("fetchSecretsCustom")

        then:
        !result.wasUpToDate("fetchSecretsCustom")

        when:
        result = runTasksSuccessfully("fetchSecretsCustom")

        then:
        result.wasUpToDate("fetchSecretsCustom")
        secretsFile.exists()

        when:
        secretsFile.delete()
        result = runTasksSuccessfully("fetchSecretsCustom")

        then:
        !result.wasUpToDate("fetchSecretsCustom")
    }

    def "task is not Up-To-Date when resolver changes"() {
        given: "a secret key"
        def key = EncryptionSpecHelper.createSecretKey(this.class.name)
        def keyFile = createFile("secrets.key", projectDir)
        keyFile.bytes = key.encoded

        and: "the key configured"
        buildFile << """
            fetchSecretsCustom.secretsKey = "${escapedPath(keyFile.path)}"
        """

        and: "a fake resolver"
        buildFile << """
            class Resolver1 implements SecretResolver {
                @Override
                Secret<?> resolve(String secretId) {
                    if(secretId == "net_wooga_testCredential2") {
                        return new DefaultSecret(secretId.toUpperCase().bytes)
                    } else {
                        return new DefaultSecret(secretId.toUpperCase())
                    } 
                }            
            }
            
            class Resolver2 implements SecretResolver {
                @Override
                Secret<?> resolve(String secretId) {
                    if(secretId == "net_wooga_testCredential3") {
                        return new DefaultSecret(secretId.toUpperCase().bytes)
                    } else {
                        return new DefaultSecret(secretId.toUpperCase())
                    } 
                }            
            }
            
            fetchSecretsCustom.resolver = new Resolver1()
        """.stripIndent()

        and: "a future secrets file"
        def secretsFile = new File(projectDir, "build/secret/fetchSecretsCustom/secrets.yml")
        assert !secretsFile.exists()

        when: "first run"
        def result = runTasksSuccessfully("fetchSecretsCustom")

        then:
        !result.wasUpToDate("fetchSecretsCustom")
        secretsFile.exists()

        when: "second run"
        result = runTasksSuccessfully("fetchSecretsCustom")

        then:
        result.wasUpToDate("fetchSecretsCustom")
        secretsFile.exists()

        when: "changing the resolver"
        buildFile << """
            fetchSecretsCustom.resolver = new Resolver2()
        """.stripIndent()
        result = runTasksSuccessfully("fetchSecretsCustom")

        then:
        !result.wasUpToDate("fetchSecretsCustom")
    }


    def "Second task can depend on secret output file"() {
        given: "a fake resolver"
        buildFile << """
            fetchSecretsCustom.resolver = Resolver.withClosure {
                if(it == "net_wooga_testCredential2") {
                    return it.toUpperCase().bytes
                } else {
                    return it.toUpperCase()
                } 
            }
        """.stripIndent()

        and: "a task to use the secrets"
        buildFile << """
            class Consumer extends DefaultTask {
                @InputFile
                final RegularFileProperty inputFile = newInputFile()
            
                @TaskAction
                void consume() {
                    def input = inputFile.get().asFile
                    def message = input.text
                }
            }
            
            task secretConsumer(type: Consumer) {
                inputFile = fetchSecretsCustom.secretsFile
            }
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("secretConsumer")

        then:
        result.wasExecuted("fetchSecretsCustom")
    }
}
