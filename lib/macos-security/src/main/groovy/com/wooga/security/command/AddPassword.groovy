/*
 * Copyright 2021 Wooga GmbH
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

package com.wooga.security.command

trait AddPassword<T extends SecurityCommand> extends KeychainCommand<T> {

    String password

    T withPassword(String value) {
        this.password = value
        this as T
    }

    Boolean allowGlobalAccess

    T allowGlobalAccess(Boolean value) {
        this.allowGlobalAccess = value
        this as T
    }

    List<String> applicationAccess

    T allowAccessFrom(String path) {
        if(!applicationAccess) {
            applicationAccess = []
        }
        applicationAccess.push(path)
        this as T
    }

    Boolean updateItem

    T updateItem(Boolean value = true) {
        this.updateItem = value
        this as T
    }

    Boolean hexPassword

    T hexPassword(Boolean value) {
        this.hexPassword = value
        this as T
    }

    List<String> getAddPasswordArguments() {
        def arguments = []
        SecurityCommand.validateStringProperty(password, "password")
        if (hexPassword) {
            arguments << "-X" << password
        } else {
            arguments << "-w" << password
        }

        if (allowGlobalAccess) {
            arguments << "-A"
        } else {
            arguments.addAll(applicationAccess.collect({ ["-T", it] }).flatten())
        }

        if (updateItem) {
            arguments << "-U"
        }

        arguments.addAll(getOptionalKeychainArgument())
        arguments
    }
}
