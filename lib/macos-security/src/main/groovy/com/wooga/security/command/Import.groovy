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
 * import inputfile [-k keychain] [-t type] [-f format] [-w] [-P passphrase]
 *      [options...]
 *             Import one or more items from inputfile into a keychain. If
 *             keychain isn't provided, items will be imported into the user's
 *             default keychain.
 *
 *             Options:
 *             -k keychain     Specify keychain into which item(s) will be
 *                             imported.
 *             -t type         Specify the type of items to import. Possible
 *                             types are cert, pub, priv, session, cert, and agg.
 *                             Pub, priv, and session refer to keys; agg is one
 *                             of the aggregate types (pkcs12 and PEM sequence).
 *                             The command can often figure out what item_type an
 *                             item contains based in the filename and/or
 *                             item_format.
 *             -f format       Specify the format of the exported data. Possible
 *                             formats are openssl, bsafe, raw, pkcs7, pkcs8,
 *                             pkcs12, x509, openssh1, openssh2, and pemseq. The
 *                             command can often figure out what format an item
 *                             is in based in the filename and/or item_type.
 *             -w              Specify that private keys are wrapped and must be
 *                             unwrapped on import.
 *             -x              Specify that private keys are non-extractable
 *                             after being imported.
 *             -P passphrase   Specify the unwrapping passphrase immediately. The
 *                             default is to obtain a secure passphrase via GUI.
 *             -a attrName attrValue
 *                             Specify optional extended attribute name and
 *                             value. Can be used multiple times. This is only
 *                             valid when importing keys.
 *             -A              Allow any application to access the imported key
 *                             without warning (insecure, not recommended!)
 *             -T appPath      Specify an application which may access the
 *                             imported key (multiple -T options are allowed)
 */

class Import extends SecurityCommand<Void> implements KeychainCommand<Import> {

    final String command = "import"

    static enum Type {
        Pub, Priv, Session, Cert, Agg

        @Override
        String toString() {
            return super.toString().toLowerCase()
        }
    }

    static enum Format {
        Openssl, Openssh1, Openssh2, Bsafe, Raw, Pkcs7, Pkcs8, Pkcs12, Netscape, Pemseq

        @Override
        String toString() {
            return super.toString().toLowerCase()
        }
    }
    File inputFile

    Import withInputFile(File value) {
        this.inputFile = value
        this
    }

    Type type

    Import withType(Type value) {
        this.type = value
        this
    }

    Format format

    Import withFormat(Format value) {
        this.format = value
        this
    }

    Boolean keysAreWrapped

    Import keysAreWrapped(Boolean value) {
        this.keysAreWrapped = value
        this
    }

    Boolean keysAreNonExtractable

    Import keysAreNonExtractable(Boolean value) {
        this.keysAreNonExtractable = value
        this
    }

    String passphrase

    Import withPassphrase(String value) {
        this.passphrase = value
        this
    }

    Map<String, String> attributes

    Import withAttribute(String key, String value) {
        if(!attributes) {
            attributes = [:]
        }
        attributes.put(key, value)
        this
    }

    Boolean allowGlobalAccess

    Import allowGlobalAccess(Boolean value) {
        this.allowGlobalAccess = value
        this
    }

    List<String> applicationAccess

    Import allowAccessFrom(String path) {
        if (!applicationAccess) {
            applicationAccess = []
        }
        applicationAccess.push(path)
        this
    }

    Import(File inputFile, File keychain, Map config = [:]) {
        this.inputFile = inputFile
        this.keychain = keychain

        this.type = config["type"] as Type
        this.format = config["format"] as Format
        this.applicationAccess = config["applicationAccess"] as List<String>
        this.allowGlobalAccess = config["allowGlobalAccess"]
        this.keysAreWrapped = config["keysAreWrapped"]
        this.keysAreNonExtractable = config["keysAreNonExtractable"]
        this.passphrase = config["passphrase"]
        this.attributes = config["attributes"] as Map<String, String>
    }

    @Override
    protected List<String> getArguments() {
        def arguments = []
        validateFileProperty(inputFile, "inputFile")
        arguments << inputFile.path

        arguments.addAll(getMandatoryKeychainArguments())

        if (type) {
            arguments << "-t" << type.toString()
        }

        if (format) {
            arguments << "-f" << format.toString()
        }

        if (keysAreWrapped) {
            arguments << "-w"
        }

        if (keysAreNonExtractable) {
            arguments << "-x"
        }

        if (this.passphrase) {
            arguments << "-P" << this.passphrase
        }

        attributes.each { key, value ->
            arguments << '-a' << key << value
        }

        if (allowGlobalAccess) {
            arguments << "-A"
        } else {
            arguments.addAll(applicationAccess.collect({ ["-T", it] }).flatten())
        }
        arguments
    }

    @Override
    protected Void convertResult(String output) {
    }

    @Override
    List<String> getSensitiveFlags() {
        ["-P"]
    }
}
