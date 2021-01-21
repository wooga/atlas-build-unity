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

class FindCertificate extends SecurityCommand<String> implements MultiKeychainCommand<FindCertificate> {

    final String command = "find-certificate"

    String email

    FindCertificate withEmail(String value) {
        this.email = value
        this
    }

    String name

    FindCertificate withName(String value) {
        this.name = value
        this
    }

    Boolean printEmail

    FindCertificate printEmail(Boolean value = true) {
        this.printEmail = value
        this
    }

    Boolean printPem

    FindCertificate printPem(Boolean value = true) {
        this.printPem = value
        this
    }

    Boolean printHash

    FindCertificate printHash(Boolean value = true) {
        this.printHash = value
        this
    }

    Boolean allMatching

    FindCertificate allMatching(Boolean value = true) {
        this.allMatching = value
        this
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        if (allMatching) {
            arguments << "-a"
        }

        if (name) {
            arguments << "-c" << "${name}".toString()
        }

        if (email) {
            arguments << "-e" << "${email}".toString()
        }

        if (printHash) {
            arguments << "-Z"
        }

        if (printEmail) {
            arguments << "-m"
        }

        if (printPem) {
            arguments << "-p"
        }

        if (!keychains.empty) {
            validateKeychainsProperty(keychains)
            arguments.addAll(keychains.collect({ it.path }))
        }
        arguments
    }

    @Override
    protected String convertResult(String output) {
        output
    }
}
