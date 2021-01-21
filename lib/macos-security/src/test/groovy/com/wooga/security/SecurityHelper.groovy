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

package com.wooga.security

class SecurityHelper {
    static def createKeychain(File location, String password) {
        ["security", "create-keychain", "-p", password, location.path].execute().waitFor()
    }

    static def unlockKeychain(File location, String password) {
        ["security", "unlock-keychain", "-p", password, location.path].execute().waitFor()
    }

    static String getKeychainSettingsRaw(File location) {
        def args = ["security", "show-keychain-info", location.path]
        def p = args.execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        p.consumeProcessOutput(out, err)
        if (p.waitFor() == 0) {
            return err.toString()
        }
        null
    }

    static MacOsKeychainSettings getKeychainSettings(File location) {
        MacOsKeychainSettings.fromOutput(getKeychainSettingsRaw(location))
    }

    static String findPrivateKey(File location, String name) {
        def args = ["security", "find-key", "-t", "private", "-l", name, location]
        def p = args.execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        p.consumeProcessOutput(out, err)
        if (p.waitFor() == 0) {
            return out.toString()
        }
        null
    }

    static Boolean keychainHasPrivateKey(File location, String name) {
        findPrivateKey(location, name) != null
    }

    static String findGenericPassword(File location, String account, String service, Map<String, String> queryArgs = [:]) {
        def args = ["security", "find-generic-password", "-a", account, "-s", service]
        args.addAll(queryArgs.collect { k, v -> [k, v] }.flatten())
        args << location
        def p = args.execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        p.consumeProcessOutput(out, err)
        if (p.waitFor() == 0) {
            return out.toString()
        }
        null
    }

    static Boolean keychainHasGenericPassword(File location, String account, String service) {
        findGenericPassword(location, account, service) != null
    }

    static String findInternetPassword(File location, String account, String server, Map<String, String> queryArgs = [:]) {
        def args = ["security", "find-internet-password", "-a", account, "-s", server]
        args.addAll(queryArgs.collect { k, v -> [k, v] }.flatten())
        args << location
        def p = args.execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        p.consumeProcessOutput(out, err)
        if (p.waitFor() == 0) {
            return out.toString()
        }
        null
    }

    static Boolean keychainHasInternetPassword(File location, String account, String server) {
        findInternetPassword(location, account, server) != null
    }

    static Boolean keychainHasPassword(File location, String account, String serviceOrServer) {
        keychainHasGenericPassword(location, account, serviceOrServer) || keychainHasInternetPassword(location, account, serviceOrServer)
    }

    static void addGenericPassword(File location, String account, String service, String password) {
        ["security", "add-generic-password", "-a", account, "-s", service, "-w", password, location.path].execute().waitFor()
    }

    static void addInternetPassword(File location, String account, String server, String password) {
        ["security", "add-internet-password", "-a", account, "-s", server, "-w", password, location.path].execute().waitFor()
    }

    static String findCertificate(File location, Map<String, String> queryArgs) {
        def args = ["security", "find-certificate"]
        args.addAll(queryArgs.collect { k, v -> [k, v] }.flatten())
        args << location
        def p = args.execute()
        def out = new StringBuffer()
        def err = new StringBuffer()
        p.consumeProcessOutput(out, err)
        if (p.waitFor() == 0) {
            return out.toString()
        }
        null
    }

    static String findCertificateWithName(File location, String name) {
        findCertificate(location, ["-c": name])
    }

    static String findCertificateWithEmail(File location, String email) {
        findCertificate(location, ["-e": email])
    }

    static Boolean keychainHasCertificateWithName(File location, String name) {
        findCertificateWithName(location, name) != null
    }

    static Boolean keychainHasCertificateWithEmail(File location, String email) {
        findCertificateWithEmail(location, email) != null
    }

    static File createPrivateKey() {
        def tempOutputDir = File.createTempDir()
        def keyFile = new File(tempOutputDir, "key.pem")

        if (['openssl', 'genpkey', '-algorithm', 'RSA', '-out', keyFile.path].execute().waitFor() == 0) {
            return keyFile
        }
        null
    }

    static File createSelfSignedCertificate(Map options = [:], File privateKey, String passphrase) {
        Map defaultOptions = [
                country         : "DE",
                state           : "Berlin",
                city            : "Berlin",
                organisationName: "Wooga Gmbh",
                organisationUnit: "Atlas",
                commonName      : "Test CA certificate",
                emailAddress    : "jenkins@wooga.net",
                days            : "365",
        ]

        options = defaultOptions << options
        def tempOutputDir = File.createTempDir()
        def cert = new File(tempOutputDir, "ca.crt")
        def cnf = new File(tempOutputDir, "ca.cnf")
        cnf << """
        # unnamed section of generic options
        default_md = md5
        
        # default section for "req" command options
        [req]
        prompt = no
        distinguished_name  = my_req_dn_no_prompt
        
        [my_req_dn_no_prompt]
        commonName             = ${options["commonName"]}
        countryName            = ${options["country"]}
        stateOrProvinceName    = ${options["state"]}
        localityName           = ${options["city"]}
        organizationName       = ${options["organisationName"]}
        organizationalUnitName = ${options["organisationUnit"]}
        emailAddress           = ${options["emailAddress"]}
        """

        def r = ['openssl', 'req',
                 '-new',
                 '-x509',
                 '-outform', 'PEM',
                 '-key', privateKey,
                 '-out', cert.path,
                 '-days', options["days"],
                 '-config', cnf.path,
        ].execute().waitFor()

        if (r == 0) {
            return cert
        }
        null
    }

    static File createTestCertificatePkcs12(Map options = [:], String password) {
        def key = createPrivateKey()

        if (!key) {
            return null
        }
        def cert = createSelfSignedCertificate(options, key, password)

        if (!cert) {
            return null
        }
        createTestCertificatePkcs12(options, key, cert, password)
    }

    static File createTestCertificatePkcs12(Map options = [:], File key, File cert, String password) {
        Map defaultOptions = [privateKeyName: "Test Certificate"]
        options = defaultOptions << options
        def tempOutputDir = File.createTempDir()
        def p12 = new File(tempOutputDir, "cert.p12")
        def args = ['openssl', 'pkcs12',
                    '-export',
                    '-in', cert.path,
                    '-inkey', key.path,
                    '-out', p12.path,
                    '-name', options['privateKeyName'],
                    '-passout', "pass:${password}"
        ]
        if (args.execute().waitFor() == 0) {
            return p12
        }
        null
    }

    static def importPK12(File input, String passphrase, File location) {
        ["security", "import", input.path, "-k", location.path, "-P", passphrase].execute().waitFor()
    }
}
