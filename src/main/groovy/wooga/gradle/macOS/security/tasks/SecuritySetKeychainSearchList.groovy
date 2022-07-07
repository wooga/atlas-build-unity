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


import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import wooga.gradle.macOS.security.SecurityMultikeychainOperationSpec

class SecuritySetKeychainSearchList extends AbstractSecurityKeychainSearchListTask implements SecurityMultikeychainOperationSpec {

    enum Action {
        add,
        remove
    }

    private final Property<Action> action

    @Input
    Property<Action> getAction() {
        action
    }

    void setAction(String value) {
        action.set(Action.valueOf(value))
    }

    void setAction(Action value) {
        action.set(value)
    }

    void setAction(Provider<Action> value) {
        action.set(value)
    }

    @SkipWhenEmpty
    @InputFiles
    @Override
    ConfigurableFileCollection getKeychains() {
        wooga_gradle_macOS_security_SecurityMultikeychainOperationSpec__keychains
    }

    SecuritySetKeychainSearchList() {
        action = project.objects.property(Action)

        onlyIf(new Spec<Task>() {
            @Override
            boolean isSatisfiedBy(Task task) {
                //Don't skip if keychains are empty. We want gradle to return NO-SOURCE skip reason
                if (keychains.isEmpty()) {
                    return true
                }
                switch (action.get()) {
                    case Action.add:
                        def filesToCheck = keychains.files.findAll { it.exists() }
                        return !searchList.containsAll(filesToCheck)
                    case Action.remove:
                        return keychains.files.any { searchList.contains(it) }
                }
            }
        })
    }

    void shutdown() {
        if(this.action.get() == Action.remove) {
            if(!this.didWork) {
                System.err.println("task ${this.name} did not run yet. Force execution")
                list()
            } else {
                System.err.println("task ${this.name} did run.")
            }
            System.err.flush()
        }
    }

    @TaskAction
    protected list() {
        def keychains = getKeychains().files
        switch (action.get()) {
            case Action.add:
                searchList.addAll(keychains)
                break
            case Action.remove:
                searchList.removeAll(keychains)
                break
        }
    }
}
