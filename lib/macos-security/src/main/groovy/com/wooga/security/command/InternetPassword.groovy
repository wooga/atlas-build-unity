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

trait InternetPassword<T extends SecurityCommand> extends KeychainItem<T> {

    String domain

    T withDomain(String value) {
        this.domain = value
        this as T
    }

    String path

    T withPath(String value) {
        this.path = value
        this as T
    }

    Integer port

    T withPort(Integer value) {
        this.port = value
        this as T
    }

    String protocol

    T withProtocol(String value) {
        this.protocol = value
        this as T
    }

    String server

    T withServer(String value) {
        this.server = value
        this as T
    }

    String authenticationType

    T withAuthenticationType(String value) {
        this.authenticationType = value
        this as T
    }

    List<String> getInternetPasswordArguments() {
        def arguments = baseItemArguments

        if (server) {
            arguments << "-s" << server
        }

        if (domain) {
            arguments << "-d" << domain
        }

        if (path) {
            arguments << "-p" << path
        }

        if (port) {
            arguments << "-P" << port.toString()
        }

        if (protocol) {
            arguments << "-r" << protocol
        }

        if (authenticationType) {
            arguments << "-t" << authenticationType
        }

        arguments
    }
}
