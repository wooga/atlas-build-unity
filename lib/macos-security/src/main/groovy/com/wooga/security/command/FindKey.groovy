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
/*
 * find-key [options...] [keychain...]
 *             Search the keychain for keys.
 *
 *             -a application-label
 *                             Match "application label" string
 *             -c creator      Match creator (four-character code)
 *             -d              Match keys that can decrypt
 *             -D description  Match "description" string
 *             -e              Match keys that can encrypt
 *             -j comment      Match comment string
 *             -l label        Match label string
 *             -r              Match keys that can derive
 *             -s              Match keys that can sign
 *             -t type         Type of key to find: one of "symmetric", "public",
 *                             or "private"
 *             -u              Match keys that can unwrap
 *             -v              Match keys that can verify
 *             -w              Match keys that can wrap
 */

class FindKey extends SecurityCommand<String> implements MultiKeychainCommand<FindKey> {

    final String command = "find-key"

    enum Type {
        Symmetric, Public, Private

        @Override
        String toString() {
            return super.toString().toLowerCase()
        }
    }

    String applicationLabel

    FindKey withApplicationLabel(String value) {
        this.applicationLabel = value
        this
    }

    String creator

    FindKey withCreator(String value) {
        this.creator = value
        this
    }

    Boolean matchKeysThatCanDecrypt

    FindKey matchKeysThatCanDecrypt(Boolean value) {
        this.matchKeysThatCanDecrypt = value
        this
    }

    String description

    FindKey withDescription(String value) {
        this.description = value
        this
    }

    Boolean matchKeysThatCanEncrypt

    FindKey matchKeysThatCanEncrypt(Boolean value) {
        this.matchKeysThatCanEncrypt = value
        this
    }

    String comment

    FindKey withComment(String value) {
        this.comment = value
        this
    }

    String label

    FindKey withLabel(String value) {
        this.label = value
        this
    }

    Boolean matchKeysThatCanDerive

    FindKey matchKeysThatCanDerive(Boolean value) {
        this.matchKeysThatCanDerive = value
        this
    }

    Boolean matchKeysThatCanSign

    FindKey matchKeysThatCanSign(Boolean value) {
        this.matchKeysThatCanSign = value
        this
    }

    Type type

    FindKey withType(Type value) {
        this.type = value
        this
    }

    Boolean matchKeysThatCanUnwrap

    FindKey matchKeysThatCanUnwrap(Boolean value) {
        this.matchKeysThatCanUnwrap = value
        this
    }

    Boolean matchKeysThatCanVerify

    FindKey matchKeysThatCanVerify(Boolean value) {
        this.matchKeysThatCanVerify = value
        this
    }

    Boolean matchKeysThatCanWrap

    FindKey matchKeysThatCanWrap(Boolean value) {
        this.matchKeysThatCanWrap = value
        this
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        if (applicationLabel) {
            arguments << "-a" << applicationLabel
        }

        if (creator) {
            arguments << "-c" << creator
        }

        if (matchKeysThatCanDecrypt) {
            arguments << "-d"
        }

        if (description) {
            arguments << "-D" << description
        }

        if (matchKeysThatCanEncrypt) {
            arguments << "-e"
        }

        if (comment) {
            arguments << "-j" << comment
        }

        if (label) {
            arguments << "-l" << label
        }

        if (matchKeysThatCanDerive) {
            arguments << "-r"
        }

        if (matchKeysThatCanSign) {
            arguments << "-s"
        }

        if (type) {
            arguments << "-t" << type.toString()
        }

        if (matchKeysThatCanUnwrap) {
            arguments << "-u"
        }
        if (matchKeysThatCanVerify) {
            arguments << "-v"
        }

        if (matchKeysThatCanWrap) {
            arguments << "-w"
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
