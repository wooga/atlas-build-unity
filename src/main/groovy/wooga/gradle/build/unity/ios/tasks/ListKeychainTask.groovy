/*
 * Copyright 2017 the original author or authors.
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

package wooga.gradle.build.unity.ios.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GUtil
import wooga.gradle.build.unity.ios.KeychainLookupList

class ListKeychainTask extends DefaultTask {

    enum Action {
        add,
        remove
    }

    private Action action
    private List<Object> keychains = new ArrayList<Object>()
    protected KeychainLookupList lookupList = new KeychainLookupList()

    @Input
    Action getAction() {
        action
    }

    void setAction(Object action) {
        this.action = action as Action
    }

    ListKeychainTask action(Object action) {
        setAction(action)
        this
    }

    @SkipWhenEmpty
    @InputFiles
    FileCollection getKeychains() {
        project.files(*keychains.toArray())
    }

    void setKeychains(Iterable<?> keychains) {
        this.keychains.removeAll()
        this.keychains(keychains)
    }

    ListKeychainTask keychains(Object... keychains) {
        this.keychains.addAll(Arrays.asList(keychains))
        this
    }

    ListKeychainTask keychains(Iterable<?> keychains) {
        GUtil.addToCollection(this.keychains, keychains)
        this
    }

    ListKeychainTask keychain(Object keychain) {
        keychains.add(keychain)
        this
    }

    ListKeychainTask() {
        super()

        outputs.upToDateWhen(new Spec<ListKeychainTask>() {
            @Override
            boolean isSatisfiedBy(ListKeychainTask element) {
                if(getAction() == Action.add) {
                    def filesToCheck = getKeychains().files.findAll {it.exists()}
                    return lookupList.containsAll(filesToCheck)
                }
                else if (getAction() == Action.remove) {
                    return !getKeychains().any {lookupList.contains(it)}
                }
            }
        })
    }

    @TaskAction
    protected list() {
        def keychains = getKeychains().files

        if (getAction() == Action.add) {
            lookupList.addAll(keychains)
        } else {
            lookupList.removeAll(keychains)
        }
    }
}
