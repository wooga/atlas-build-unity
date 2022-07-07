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
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import wooga.gradle.macOS.security.SecurityMultikeychainOperationSpec

class SecurityLockKeychain extends AbstractInteractiveSecurityTask implements SecurityMultikeychainOperationSpec {

    @Optional
    @InputFiles
    @Override
    ConfigurableFileCollection getKeychains() {
        wooga_gradle_macOS_security_SecurityMultikeychainOperationSpec__keychains
    }

    private final Property<Boolean> all = objects.property(Boolean)

    @Optional
    @Input
    Property<Boolean> getAll() {
        all
    }

    void setAll(Provider<Boolean> value) {
        all.set(value)
    }

    void setAll(Boolean value) {
        all.set(value)
    }

    SecurityLockKeychain() {
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
