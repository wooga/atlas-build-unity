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
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.Callable

class LockKeychainTask extends DefaultTask {

    enum LockAction {
        lock,
        unlock
    }

    private LockAction action
    private Object keychain

    @Input
    LockAction getAction() {
        action
    }

    void setLockAction(Object action) {
        this.action = action as LockAction
    }

    LockKeychainTask lockAction(Object action) {
        setLockAction(action)
        this
    }

    private Object password

    @Input
    String getPassword() {
        if(Callable.isInstance(password)) {
            return ((Callable)password).call().toString()
        }

        password.toString()
    }

    void setPassword(Object value) {
        password = value
    }

    LockKeychainTask password(Object password) {
        setPassword(password)
        this
    }

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        project.files(keychain)
    }

    @InputFile
    File getKeychain() {
        project.files(keychain).getSingleFile()
    }

    void setKeychain(Object keyChain) {
        keychain = keyChain
    }

    LockKeychainTask keychain(Object keyChain) {
        setKeychain(keyChain)
    }

    @TaskAction
    protected void unlock() {
        project.exec {
            executable "security"
            args "${getAction()}-keychain"
            if(getAction() == LockAction.unlock) {
                args "-p", getPassword()
            }
            args getKeychain()
        }
    }
}
