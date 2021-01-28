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

import com.wooga.security.command.LockKeychain
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

class SecurityLockKeychain extends AbstractInteractiveSecurityTask {
    @Optional
    @InputFiles
    final ConfigurableFileCollection keychains

    void setKeychains(List<File> value) {
        keychains.setFrom(value)
    }

    void setKeychains(Provider<List<RegularFile>> value) {
        keychains.setFrom(value)
    }

    SecurityLockKeychain keychains(List<File> value) {
        keychains.from(project.provider({ value }))
        this
    }

    SecurityLockKeychain keychains(Provider<List<RegularFile>> value) {
        keychains.from(value)
        this
    }

    void keychain(Provider<File> keychain) {

        keychains.from(keychain)
    }

    void keychain(File keychain) {
        keychains.from(keychain)
    }

    @Optional
    @Input
    final Property<Boolean> all

    void setAll(Boolean value) {
        all.set(value)
    }

    void setAll(Provider<Boolean> value) {
        all.set(value)
    }

    SecurityLockKeychain all(Boolean value) {
        setAll(value)
        this
    }

    SecurityLockKeychain all(Provider<Boolean> value) {
        setAll(value)
        this
    }

    SecurityLockKeychain() {
        keychains = project.layout.configurableFiles()
        all = project.objects.property(Boolean)
        all.set(null)

        outputs.upToDateWhen(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                false
            }
        })
    }

    @TaskAction
    protected void lock() {
        def command = new LockKeychain()
        if (all.present && all.get()) {
            command.all(true)
        } else {
            keychains.each { keychain ->
                command.withKeychain(keychain)
            }
        }

        command.execute()
    }
}
