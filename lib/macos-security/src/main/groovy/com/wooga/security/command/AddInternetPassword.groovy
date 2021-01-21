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
 * add-internet-password [-h] [-a account] [-s server] [-w password]
 *      [options...] [keychain]
 *             Add an internet password item.
 *
 *             -a account      Specify account name (required)
 *             -c creator      Specify item creator (optional four-character
 *                             code)
 *             -C type         Specify item type (optional four-character code)
 *             -d domain       Specify security domain string (optional)
 *             -D kind         Specify kind (default is "application password")
 *             -j comment      Specify comment string (optional)
 *             -l label        Specify label (if omitted, service name is used as
 *                             default label)
 *             -p path         Specify path string (optional)
 *             -P port         Specify port number (optional)
 *             -r protocol     Specify protocol (optional four-character SecPro-
 *                             tocolType, e.g. "http", "ftp ")
 *             -s server       Specify server name (required)
 *             -t authenticationType
 *                             Specify authentication type (as a four-character
 *                             SecAuthenticationType, default is "dflt")
 *             -w password     Specify password to be added. Put at end of com-
 *                             mand to be prompted (recommended)
 *             -A              Allow any application to access this item without
 *                             warning (insecure, not recommended!)
 *             -T appPath      Specify an application which may access this item
 *                             (multiple -T options are allowed)
 *             -U              Update item if it already exists (if omitted, the
 *                             item cannot already exist)
 *             -X password     Specify password data to be added as a hexadecimal
 *                             string
 *
 *             By default, the application which creates an item is trusted to
 *             access its data without warning.  You can remove this default
 *             access by explicitly specifying an empty app pathname: -T "". If
 *             no keychain is specified, the password is added to the default
 *             keychain.
 */

class AddInternetPassword extends SecurityCommand<Void>
        implements InternetPassword<AddInternetPassword>,
                AddPassword<AddInternetPassword> {
    String command = "add-internet-password"

    AddInternetPassword(String account, String server, String password, Map config = [:]) {
        this.account = account
        this.server = server
        this.password = password

        this.domain = config['domain']
        this.protocol = config['protocol']
        this.port = config['port'] as Integer
        this.authenticationType = config['authenticationType']
        this.path = config['path']
        this.hexPassword = config['hexPassword']
        this.kind = config['kinds']
        this.type = config['type']
        this.comment = config['comment']
        this.label = config['label']
        this.creator = config['creator']
        this.allowGlobalAccess = config['allowGlobalAccess']
        this.applicationAccess = config['applicationAccess'] as List<String>
        this.updateItem = config['updateItem']
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        validateStringProperty(server, "server")
        validateStringProperty(account, "account")
        arguments.addAll(getInternetPasswordArguments())
        arguments.addAll(getAddPasswordArguments())

        arguments
    }

    @Override
    List<String> getSensitiveFlags() {
        ["-w", "-X"]
    }

    @Override
    protected Void convertResult(String output) {
    }
}
