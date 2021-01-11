/*
 * Copyright 2018-2020 Wooga GmbH
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

package wooga.gradle.macOS.security.tasks

import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import wooga.gradle.macOS.security.internal.SecurityKeychainSettingsSpec

class SecurityCreateKeychain extends AbstractInteractiveSecurityTask implements SecurityKeychainSettingsSpec {

    final Property<String> fileName

    void setFileName(String value) {
        fileName.set(value)
    }

    void setFileName(Provider<String> value) {
        fileName.set(value)
    }

    SecurityCreateKeychain fileName(String value) {
        setFileName(value)
        this
    }

    SecurityCreateKeychain fileName(Provider<String> value) {
        setFileName(value)
        this
    }

    final Property<String> baseName

    void setBaseName(String value) {
        baseName.set(value)
    }

    void setBaseName(Provider<String> value) {
        baseName.set(value)
    }

    SecurityCreateKeychain baseName(String value) {
        setBaseName(value)
        this
    }

    SecurityCreateKeychain baseName(Provider<String> value) {
        setBaseName(value)
        this
    }

    final Property<String> extension

    void setExtension(String value) {
        extension.set(value)
    }

    void setExtension(Provider<String> value) {
        extension.set(value)
    }

    SecurityCreateKeychain extension(String value) {
        setExtension(value)
        this
    }

    SecurityCreateKeychain extension(Provider<String> value) {
        setExtension(value)
        this
    }

    final DirectoryProperty destinationDir

    void setDestinationDir(File value) {
        destinationDir.set(value)
    }

    void setDestinationDir(Provider<Directory> value) {
        destinationDir.set(value)
    }

    SecurityCreateKeychain destinationDir(File value) {
        setDestinationDir(value)
        this
    }

    SecurityCreateKeychain destinationDir(Provider<Directory> value) {
        setDestinationDir(value)
        this
    }

    @OutputFile
    final Provider<RegularFile> keychain

    @Internal
    final Provider<RegularFile> tempLockFile

    @Internal
    final Provider<List<String>> securityCommands

    SecurityCreateKeychain() {
        baseName = project.objects.property(String)
        extension = project.objects.property(String)
        fileName = project.objects.property(String)

        fileName.set(baseName.map({
            if (extension.present) {
                return it + "." + extension.get()
            }
            it
        }))

        destinationDir = project.layout.directoryProperty()
        keychain = destinationDir.file(fileName)
        tempLockFile = destinationDir.file(fileName.map({
            getTempKeychainFileName(it)
        }))

       securityCommands = project.provider({
            List<String> commands = new ArrayList()
            commands << "create-keychain -p '${password.get()}' ${keychain.get()}"
            commands << "unlock-keychain -p '${password.get()}' ${keychain.get()}"
            def keychainSettingsArgs = getKeychainSettingsArguments()
            if(!keychainSettingsArgs.isEmpty()) {
                commands << "set-keychain-settings ${getKeychainSettingsArguments().join(" ")} ${keychain.get()}"
            }

            commands
        })
    }
}
