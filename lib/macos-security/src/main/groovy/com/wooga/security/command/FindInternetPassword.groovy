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

class FindInternetPassword extends SecurityCommand<String>
        implements InternetPassword<FindInternetPassword>, FindPassword<FindInternetPassword> {
    String command = "find-internet-password"

    FindInternetPassword(String account, String server, Map config = [:]) {
        this.account = account
        this.server = server

        this.domain = config['domain']
        this.protocol = config['protocol']
        this.port = config['port'] as Integer
        this.authenticationType = config['authenticationType']
        this.path = config['path']
        this.kind = config['kinds']
        this.type = config['type']
        this.comment = config['comment']
        this.label = config['label']
        this.creator = config['creator']
        this.printPassword = config['printPassword']
        this.printPasswordOnly = config['printPasswordOnly']
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        validateStringProperty(account, "account")
        validateStringProperty(server, "server")
        arguments.addAll(getInternetPasswordArguments())
        arguments.addAll(getFindPasswordArguments())
        arguments
    }

    @Override
    protected String convertResult(String output) {
        output.trim()
    }
}
