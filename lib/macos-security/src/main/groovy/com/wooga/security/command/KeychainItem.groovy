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

trait KeychainItem<T extends SecurityCommand> {

    String account

    T withAccount(String value) {
        this.account = value
        this as T
    }

    String creator

    T withCreator(String value) {
        this.creator = value
        this as T
    }

    String type

    T withType(String value) {
        this.type = value
        this as T
    }

    String kind

    T withKind(String value) {
        this.kind = value
        this as T
    }

    String comment

    T withComment(String value) {
        this.comment = value
        this as T
    }

    String label

    T withLabel(String value) {
        this.label = value
        this as T
    }

    List<String> getBaseItemArguments() {
        def arguments = []

        if (account) {
            arguments << "-a" << account
        }
        if (creator) {
            arguments << "-c" << creator
        }

        if (type) {
            arguments << "-C" << type
        }

        if (kind) {
            arguments << "-D" << kind
        }

        if (comment) {
            arguments << "-j" << comment
        }

        if (label) {
            arguments << "-l" << label
        }

        arguments
    }
}
