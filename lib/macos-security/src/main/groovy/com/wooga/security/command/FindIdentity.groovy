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
 * find-identity [-h] [-p policy] [-s string] [-v] [keychain...]
 *             Find an identity (certificate + private key) satisfying a given
 *             policy. If no policy arguments are provided, the X.509 basic pol-
 *             icy is assumed. If no keychain arguments are provided, the default
 *             search list is used.
 *
 *             Options:
 *             -p policy       Specify policy to evaluate (multiple -p options
 *                             are allowed). Supported policies: basic, ssl-
 *                             client, ssl-server, smime, eap, ipsec, ichat,
 *                             codesigning, sys-default, sys-kerberos-kdc
 *             -s string       Specify optional policy-specific string (e.g. a
 *                             DNS hostname for SSL, or RFC822 email address for
 *                             S/MIME)
 *             -v              Show valid identities only (default is to show all
 *                             identities)
 */

class FindIdentity extends SecurityCommand<String> implements MultiKeychainCommand<FindIdentity> {

    final String command = "find-identity"

    enum Policy {
        Basic, SslClient, SslServer, Smime, Eap, Ipsec, Ichat, Codesigning, SysDefault, SysKerberosKdc

        @Override
        String toString() {
            return super.toString().replaceAll(/([a-z])([A-Z])/, { _, String before, String character -> "${before}-${character.toLowerCase()}" }).toLowerCase()
        }
    }

    List<Policy> policies = []

    FindIdentity withPolicy(Policy value) {
        this.policies.add(value)
        this
    }

    String policySpecifier

    FindIdentity withPolicySpecifier(String value) {
        this.policySpecifier = value
        this
    }

    Boolean validIdentities

    FindIdentity validIdentities(Boolean value) {
        this.validIdentities = value
        this
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []

        policies.each {
            arguments << "-p" << it.toString()
        }

        if (policySpecifier) {
            arguments << "-s" << policySpecifier
        }

        if (validIdentities) {
            arguments << "-v"
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
