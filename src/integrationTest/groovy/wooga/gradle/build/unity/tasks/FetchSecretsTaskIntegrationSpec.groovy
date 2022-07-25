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


import spock.lang.Unroll
import wooga.gradle.build.IntegrationSpec
import wooga.gradle.build.unity.UnityBuildPlugin
import wooga.gradle.secrets.Secret
import wooga.gradle.secrets.SecretResolver
import wooga.gradle.secrets.SecretsPlugin
import wooga.gradle.secrets.internal.DefaultSecret
import wooga.gradle.secrets.internal.EncryptionSpecHelper
import wooga.gradle.secrets.internal.Resolver

import static com.wooga.gradle.PlatformUtils.escapedPath

class FetchSecretsTaskIntegrationSpec extends IntegrationSpec {
    def setup() {
        buildFile << """
            ${applyPlugin(UnityBuildPlugin)}

            import ${Resolver.name}
            import ${DefaultSecret.name}
            import ${SecretResolver.name}
            import ${Secret.name}

            task("fetchSecretsCustom", type: ${FetchSecrets.name}) {
                secretIds = [   
                                'net_wooga_testCredential',
                                'net_wooga_testCredential2',
                                'net_wooga_testCredential3',
                                'net_wooga_testCredential4'
                            ]
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
            fetchSecretsCustom.secretsKey = file("${escapedPath(keyFile.path)}")
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
            fetchSecretsCustom.secretsKey = file("${escapedPath(keyFile.path)}")
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
                final RegularFileProperty inputFile = project.objects.fileProperty()
            
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


    @Unroll
    def "can set property #property with #method"() {
        given: "a custom fetch secrets task"
        buildFile << """
            task("fetchSecretsCustom2", type: ${FetchSecrets.name})
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("secretIds: " + fetchSecretsCustom2.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            fetchSecretsCustom2.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "secretIds: " + expectedValue.toString())

        where:
        property    | method         | rawValue           | type
        "secretIds" | "secretIds"    | ["Test1"]          | "List<String>"
        "secretIds" | "secretId"     | "Test1"            | "String"
        "secretIds" | "secretIds"    | ["Test1", "Test2"] | "List<String>"
        "secretIds" | "secretIds"    | ["Test1", "Test2"] | "String..."
        "secretIds" | "setSecretIds" | ["Test1", "Test2"] | "List<String>"
        "secretIds" | "setSecretIds" | ["Test1", "Test2"] | "String..."
        value = wrapValueBasedOnType(rawValue, type)
        expectedValue = [rawValue].flatten()
    }

    @Unroll
    def "#method will #setType value"() {
        given: "a custom fetch secrets task"
        buildFile << """
            task("fetchSecretsCustom2", type: ${FetchSecrets.name}) {
                secretIds = ['secret1']
            }
        """.stripIndent()

        and: "a task to read back the value"
        buildFile << """
            task("readValue") {
                doLast {
                    println("secretIds: " + fetchSecretsCustom2.${property}.get())
                }
            }
        """.stripIndent()

        and: "a set property"
        buildFile << """
            fetchSecretsCustom2.${method}($value)
        """.stripIndent()

        when:
        def result = runTasksSuccessfully("readValue")

        then:
        outputContains(result, "secretIds: " + expectedValue.toString())

        where:
        property    | method         | rawValue           | type           | append | expectedValue
        "secretIds" | "secretIds"    | ["Test1"]          | "List<String>" | true   | ['secret1', 'Test1']
        "secretIds" | "secretId"     | "Test1"            | "String"       | true   | ['secret1', 'Test1']
        "secretIds" | "secretIds"    | ["Test1", "Test2"] | "List<String>" | true   | ['secret1', 'Test1', 'Test2']
        "secretIds" | "secretIds"    | ["Test1", "Test2"] | "String..."    | true   | ['secret1', 'Test1', 'Test2']
        "secretIds" | "setSecretIds" | ["Test1", "Test2"] | "List<String>" | false  | ['Test1', 'Test2']
        "secretIds" | "setSecretIds" | ["Test1", "Test2"] | "String..."    | false  | ['Test1', 'Test2']
        setType = (append) ? 'append' : 'replace'
        value = wrapValueBasedOnType(rawValue, type)
    }
}
