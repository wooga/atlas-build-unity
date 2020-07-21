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

package wooga.gradle.build.unity.internal

import org.apache.commons.lang3.RandomStringUtils
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import wooga.gradle.build.unity.UnityBuildPluginConsts
import wooga.gradle.build.unity.UnityBuildPluginExtension
import wooga.gradle.build.unity.ios.internal.utils.PropertyUtils
import wooga.gradle.build.unity.secrets.SecretResolver
import wooga.gradle.unity.UnityPluginExtension

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.concurrent.Callable

class DefaultUnityBuildPluginExtension implements UnityBuildPluginExtension {

    protected final Project project

    final DirectoryProperty appConfigsDirectory
    final DirectoryProperty outputDirectoryBase
    final Property<String> toolsVersion
    final Property<String> version
    final Property<String> versionCode
    final Property<String> commitHash
    final Property<String> exportMethodName
    final Property<String> defaultAppConfigName
    final Provider<Directory> assetsDir
    final ConfigurableFileCollection ignoreFilesForExportUpToDateCheck
    final RegularFileProperty exportInitScript
    final Property<File> exportBuildDirBase
    final Property<Boolean> cleanBuildDirBeforeBuild

    final Property<SecretKeySpec> secretsKey
    final Property<SecretResolver> secretResolver

    DefaultUnityBuildPluginExtension(final Project project) {
        this.project = project

        appConfigsDirectory = project.layout.directoryProperty()
        outputDirectoryBase = project.layout.directoryProperty()
        toolsVersion = project.objects.property(String.class)
        version = project.objects.property(String.class)
        versionCode = project.objects.property(String.class)
        commitHash = project.objects.property(String.class)
        exportMethodName = project.objects.property(String.class)
        defaultAppConfigName = project.objects.property(String.class)
        assetsDir = project.layout.directoryProperty()
        ignoreFilesForExportUpToDateCheck = project.layout.configurableFiles()
        exportInitScript = project.layout.fileProperty()
        exportBuildDirBase = project.objects.property(File)
        cleanBuildDirBeforeBuild = project.objects.property(Boolean)

        exportMethodName.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv().get(UnityBuildPluginConsts.EXPORT_METHOD_NAME_ENV_VAR) ?:
                        project.properties.getOrDefault(UnityBuildPluginConsts.EXPORT_METHOD_NAME_OPTION, UnityBuildPluginConsts.DEFAULT_EXPORT_METHOD_NAME)
            }
        }))

        defaultAppConfigName.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv()[UnityBuildPluginConsts.DEFAULT_APP_CONFIG_NAME_ENV_VAR] ?:
                        project.properties.get(UnityBuildPluginConsts.DEFAULT_APP_CONFIG_NAME_OPTION)
            }
        }))

        commitHash.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv().get(UnityBuildPluginConsts.BUILD_COMMIT_HASH_ENV_VAR) ?:
                        project.properties.get(UnityBuildPluginConsts.BUILD_COMMIT_HASH_OPTION, null)
            }
        }))

        toolsVersion.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv().get(UnityBuildPluginConsts.BUILD_TOOLS_VERSION_ENV_VAR) ?:
                        project.properties.get(UnityBuildPluginConsts.BUILD_TOOLS_VERSION_OPTION, null)
            }
        }))

        version.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                def version = PropertyUtils.convertToString(project.version)
                if(!version || version == "unspecified") {
                    return System.getenv().get(UnityBuildPluginConsts.BUILD_VERSION_ENV_VAR) ?:
                            project.properties.get(UnityBuildPluginConsts.BUILD_VERSION_OPTION, version) as String
                }
                version
            }
        }))

        versionCode.set(project.provider(new Callable<String>() {
            @Override
            String call() throws Exception {
                System.getenv().get(UnityBuildPluginConsts.BUILD_VERSION_CODE_ENV_VAR) ?:
                        project.properties.get(UnityBuildPluginConsts.BUILD_VERSION_CODE_OPTION, null) as String
            }
        }))

        outputDirectoryBase.set(project.layout.buildDirectory.dir(UnityBuildPluginConsts.DEFAULT_EXPORT_DIRECTORY_NAME))
        appConfigsDirectory.set(assetsDir.dir(UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_DIRECTORY))

        assetsDir.set(project.provider(new Callable<Directory>() {
            @Override
            Directory call() throws Exception {
                UnityPluginExtension unity = project.extensions.getByType(UnityPluginExtension)
                def assetDir = project.layout.directoryProperty()
                assetDir.set(project.file(unity.assetsDir))
                assetDir.get()
            }
        }))

        exportInitScript.set(project.provider(new Callable<RegularFile>() {
            @Override
            RegularFile call() throws Exception {
                String exportInitScriptPath = System.getenv().get(UnityBuildPluginConsts.EXPORT_INIT_SCRIPT_ENV_VAR) ?:
                        project.properties.get(UnityBuildPluginConsts.EXPORT_INIT_SCRIPT_OPTION, null)

                if (exportInitScriptPath) {
                    def property = project.layout.fileProperty()
                    property.set(new File(exportInitScriptPath))
                    return property.get()
                }
                return null
            }
        }))

        exportBuildDirBase.set(project.provider(new Callable<Directory>() {
            @Override
            Directory call() throws Exception {
                String exportBuildDirBasePath = System.getenv().get(UnityBuildPluginConsts.EXPORT_BUILD_DIR_BASE_ENV_VAR) ?:
                        project.properties.get(UnityBuildPluginConsts.EXPORT_BUILD_DIR_BASE_OPTION, null)

                if (exportBuildDirBasePath) {
                    def property = project.layout.directoryProperty()
                    property.set(new File(exportBuildDirBasePath))
                    return property.get()
                }
                return null
            }
        }))

        cleanBuildDirBeforeBuild.set(project.provider(new Callable<Boolean>() {
            @Override
            Boolean call() throws Exception {
                String rawValue = System.getenv().get(UnityBuildPluginConsts.CLEAN_BUILD_DIR_BEFORE_BUILD_ENV_VAR) ?:
                        project.properties.get(UnityBuildPluginConsts.CLEAN_BUILD_DIR_BEFORE_BUILD_OPTION, null)

                if(rawValue) {
                    rawValue = rawValue.toString().toLowerCase()
                    rawValue = (rawValue == "1" || rawValue == "yes") ? "true" : false
                    return rawValue
                }

                return false
            }
        }))

        secretsKey = project.objects.property(SecretKeySpec.class)

        secretsKey.set(new MemoisationProvider<SecretKeySpec>(project.provider({
            String keyPath = System.getenv().get(UnityBuildPluginConsts.SECRETS_KEY_ENV_VAR) ?:
                    project.properties.get(UnityBuildPluginConsts.SECRETS_KEY_OPTION, null)

            if (keyPath) {
                return new SecretKeySpec(new File(keyPath).bytes, "AES")
            }

            KeySpec spec = new PBEKeySpec(secretsKeyPassword().chars, secretsKeySalt(), UnityBuildPluginConsts.SECRETS_KEY_ITERATION, UnityBuildPluginConsts.SECRETS_KEY_LENGTH);
            // AES-256
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] key = f.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, "AES");
        })))

        secretResolver = project.objects.property(SecretResolver)
    }

    void setSecretsKey(SecretKeySpec key) {
        secretsKey.set(key)
    }

    DefaultUnityBuildPluginExtension setSecretsKey(String keyFile) {
        setSecretsKey(project.file(keyFile))
    }

    DefaultUnityBuildPluginExtension setSecretsKey(File keyFile) {
        setSecretsKey(new SecretKeySpec(keyFile.bytes, "AES"))
    }

    @Override
    DefaultUnityBuildPluginExtension secretsKey(SecretKeySpec key) {
        setSecretsKey(key)
    }

    @Override
    DefaultUnityBuildPluginExtension secretsKey(String keyFile) {
        return setSecretsKey(keyFile)
    }

    @Override
    DefaultUnityBuildPluginExtension secretsKey(File keyFile) {
        return setSecretsKey(keyFile)
    }

    protected static String secretsKeyPassword() {
        RandomStringUtils.random(20)
    }

    protected static byte[] secretsKeySalt() {
        SecureRandom random = new SecureRandom()
        byte[] salt = new byte[16]
        random.nextBytes(salt)
        salt
    }

    @Override
    FileCollection getAppConfigs() {
        project.fileTree(getAppConfigsDirectory()) {
            it.include UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_INCLUDE_PATTERN
            it.exclude UnityBuildPluginConsts.DEFAULT_APP_CONFIGS_EXCLUDE_PATTERN
        }
    }
}
