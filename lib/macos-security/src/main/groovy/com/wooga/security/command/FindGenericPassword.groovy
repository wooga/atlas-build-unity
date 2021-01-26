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
 * find-generic-password [-h] [-a account] [-s service] [-options...] [-g]
 *      [-keychain...]
 *             Find a generic password item.
 *
 *             -a account      Match account string
 *             -c creator      Match creator (four-character code)
 *             -C type         Match type (four-character code)
 *             -D kind         Match kind string
 *             -G value        Match value string (generic attribute)
 *             -j comment      Match comment string
 *             -l label        Match label string
 *             -s service      Match service string
 *             -g              Display the password for the item found
 *             -w              Display the password(only) for the item found
 */

class FindGenericPassword extends SecurityCommand<String>
        implements GenericPassword<FindGenericPassword>,
                FindPassword<FindGenericPassword> {
    String command = "find-generic-password"

    FindGenericPassword(String account, String service, Map config = [:]) {
        this.account = account
        this.service = service

        this.value = config['value']
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
        validateStringProperty(service, "service")
        arguments.addAll(getGenericPasswordArguments())
        arguments.addAll(getFindPasswordArguments())
        arguments
    }

    @Override
    protected String convertResult(String output) {
        output.trim()
    }
}
