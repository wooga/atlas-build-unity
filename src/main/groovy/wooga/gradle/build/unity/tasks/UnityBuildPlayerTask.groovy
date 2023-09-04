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
import org.gradle.api.Transformer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.build.unity.UnityBuildPlugin
import wooga.gradle.build.unity.models.UnityBuildSpec
import wooga.gradle.build.unity.models.VersionSpec
import wooga.gradle.secrets.internal.Secrets
import wooga.gradle.secrets.SecretSpec
import wooga.gradle.unity.UnityTask
import wooga.gradle.unity.utils.GenericUnityAssetFile

import javax.crypto.spec.SecretKeySpec

/**
 * Deprecated in favor of UnityBuildEnginePlayerTask from UBS 1.2 onwards.
 */
@Deprecated
class UnityBuildPlayerTask extends UnityTask implements SecretSpec, UnityBuildSpec, VersionSpec {

    static String BUILD_TARGET_KEY = UnityBuildPlugin.appConfigBuildTarget

    private final ConfigurableFileCollection inputFiles

    @SkipWhenEmpty
    @InputFiles
    ConfigurableFileCollection getInputFiles() {
        inputFiles
    }

    private final RegularFileProperty appConfigFile

    @InputFile
    RegularFileProperty getAppConfigFile() {
        appConfigFile
    }

    private final DirectoryProperty outputDirectoryBase

    @Internal("Base path of outputFiles")
    DirectoryProperty getOutputDirectoryBase() {
        outputDirectoryBase
    }

    private final Provider<Directory> outputDirectory

    @Internal //TODO: rethink this to get advantage from gradle UP-TO-DATE checks.
    Provider<Directory> getOutputDirectory() {
        outputDirectory
    }

    private final Provider<String> appConfigName

    @Internal
    Provider<String> getAppConfigName() {
        appConfigName
    }

    private final RegularFileProperty secretsFile

    @Optional
    @InputFile
    RegularFileProperty getSecretsFile() {
        secretsFile
    }

    protected final Provider<Secrets> secrets

    @Internal
    protected Provider<Secrets> getSecrets() {
        secrets
    }

    protected final Provider<Secrets.EnvironmentSecrets> environmentSecrets

    @Internal
    protected Provider<Secrets.EnvironmentSecrets> getEnvironmentSecrets() {
        environmentSecrets
    }

    @Internal("loaded app config asset")
    protected Provider<GenericUnityAssetFile> getAppConfig() {
        appConfigFile.map({ new GenericUnityAssetFile(it.asFile) })
    }

    @Internal("read from appConfig file")
    Provider<String> getBuildPlatform() {
        getAppConfig().map({
            it[BUILD_TARGET_KEY]
        }).map({ it.toString().toLowerCase() })
    }

    UnityBuildPlayerTask() {
        inputFiles = project.objects.fileCollection()
        appConfigFile = project.objects.fileProperty()
        appConfigName = appConfigFile.map { FilenameUtils.removeExtension(it.asFile.name) }

        outputDirectoryBase = project.objects.directoryProperty()

        def outputPath = appConfigName.map(new Transformer<String, String>() {
            @Override
            String transform(String configName) {
                configName + "/project"
            }
        })

        outputDirectory = project.objects.directoryProperty()
        outputDirectory.set(outputDirectoryBase.dir(outputPath))
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
        }.memoize())

        environment.putAll(environmentSecrets)
        buildTarget.set(getBuildPlatform())

        def customArgsProvider = project.provider({
            String customArgs = "-CustomArgs:appConfig=${appConfigFile.get().asFile.path};"
            customArgs += "outputPath=${outputDirectory.get().asFile.path};"
            customArgs += "version=${version.get()};"

            if (versionCode.present) {
                customArgs += "versionCode=${versionCode.get()};"
            }

            if (toolsVersion.present) {
                customArgs += "toolsVersion=${toolsVersion.get()};"
            }

            if (commitHash.present) {
                customArgs += "commitHash=${commitHash.get()};"
            }

            customArgs += customArguments.get().collect({ key, value -> "${key}=${value};" }).join()
            customArgs
        })

        additionalArguments.add(customArgsProvider)
        additionalArguments.add("-executeMethod")
        additionalArguments.add(exportMethodName)
    }

    @Override
    void exec() {
        File out = outputDirectory.get().asFile
        out.mkdirs()
        try {
            super.exec()
        } finally {
            environmentSecrets.get().clear()
        }
    }
}
