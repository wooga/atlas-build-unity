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

import java.security.MessageDigest

class SecurityHelper {

    static class CA {
        final File key
        final File certificate
        final File config
        final String passphrase

        CA(File key, File certificate, File cnf, String passphrase) {
            this.key = key
            this.certificate = certificate
            this.config = cnf
            this.passphrase = passphrase
        }
    }

    static File createPrivateKey() {
        def tempOutputDir = File.createTempDir()
        def keyFile = new File(tempOutputDir, "key.pem")

        if (['openssl', 'genpkey', '-algorithm', 'RSA', '-out', keyFile.path].execute().waitFor() == 0) {
            return keyFile
        }
        null
    }

    static CA newCA(Map options = [:], File directory = null) {
        Map defaultOptions = [
                country         : "DE",
                state           : "Berlin",
                city            : "Berlin",
                organisationName: "Wooga Gmbh",
                organisationUnit: "Atlas",
                CAcommonName    : "Test CA certificate",
                emailAddress    : "jenkins@wooga.net",
                days            : "365",
        ]

        options = defaultOptions << options
        File cw = directory ? directory : File.createTempDir()
        File certs = new File(cw, "certs")
        certs.mkdirs()
        File crl = new File(cw, "crl")
        crl.mkdirs()
        File _private = new File(cw, "private")
        _private.mkdirs()
        File newCerts = new File(cw, "newcerts")
        newCerts.mkdirs()

        File caKey = new File(_private, "cakey.pem")
        File caReq = new File(cw, "careq.pem")
        File caCert = new File(cw, "cacert.pem")
        File caDataBase = new File(cw, "certs.db")
        caDataBase.createNewFile()

        def cnf = new File(cw, "ca.cnf")
        cnf << """
        # unnamed section of generic options
        default_md              = md5
         
        # default section for "req" command options
        [req]
        prompt                  = no
        distinguished_name      = my_req_dn_no_prompt

        [ca]
        default_ca              = my_ca_default

        [my_ca_default]
        default_md              = md5
        default_days            = 3000
        policy                  = policy_any 
        serial                  = ${cw.path}/certs.seq        
        new_certs_dir           = ${certs.path}
        database                = ${cw.path}/certs.db
        private_key             = ${caKey.path}
        certificate             = ${caCert.path}

        [my_req_dn_no_prompt]
        commonName              = ${options["CAcommonName"]}
        countryName             = ${options["country"]}
        stateOrProvinceName     = ${options["state"]}
        localityName            = ${options["city"]}
        organizationName        = ${options["organisationName"]}
        organizationalUnitName  = ${options["organisationUnit"]}
        emailAddress            = ${options["emailAddress"]}

        [usr_cert]
        basicConstraints        = CA:FALSE
        keyUsage                = digitalSignature
        extendedKeyUsage        = codeSigning  
        subjectKeyIdentifier    = hash
        authorityKeyIdentifier  = keyid,issuer
        subjectAltName          = email:move

        [v3_req]
        keyUsage                = digitalSignature
        extendedKeyUsage        = codeSigning

        [policy_any]
        countryName             = supplied
        stateOrProvinceName     = optional
        organizationName        = optional
        organizationalUnitName  = optional
        commonName              = supplied
        emailAddress            = optional
        
        ####################################################################
        # Same as above, but cert req already has SubjectAltName
        [ usr_cert_has_san ]
        basicConstraints = CA:false
        subjectKeyIdentifier = hash
        authorityKeyIdentifier = keyid,issuer
        
        ####################################################################
        # Extensions to use when signing a CA
        [ v3_ca ]
        subjectKeyIdentifier = hash
        authorityKeyIdentifier = keyid:always,issuer:always
        basicConstraints = CA:true
        subjectAltName=email:move
        
        ####################################################################
        # Same as above, but CA req already has SubjectAltName
        [ v3_ca_has_san ]
        subjectKeyIdentifier = hash
        authorityKeyIdentifier = keyid:always,issuer:always
        basicConstraints = CA:true
        """.stripIndent()

        def passphrase = "123456"
        if (['openssl', 'req',
             '-new',
             '-keyout', caKey.path,
             '-passout', "pass:${passphrase}",
             '-out', caReq.path,
             '-config', cnf.path
        ].execute().waitFor() == 0) {
            if (['openssl', 'ca', '-create_serial',
                 '-out', caCert, '-batch',
                 '-keyfile', caKey.path,
                 '-selfsign',
                 '-extensions',
                 'v3_ca',
                 '-passin', "pass:${passphrase}",
                 '-config', cnf.path,
                 '-infiles', caReq.path
            ].execute().waitFor() == 0) {
                return new CA(caKey, caCert, cnf, "123456")
            }
        }
        null
    }

    static File createCodeSigningCertificateRequest(Map options = [:], File privateKey) {
        Map defaultOptions = [
                commonName: "Test code signing certificate",
                days      : "365",
        ]
        options = defaultOptions << options

        def tempOutputDir = File.createTempDir()
        def cnf = new File(tempOutputDir, "csr.cnf")
        cnf << """
        [v3_req]
        keyUsage = digitalSignature
        extendedKeyUsage = codeSigning

        # default section for "req" command options
        [req]
        default_bits           = 2048                
        encrypt_key            = yes                  
        default_md             = sha256               
        utf8                   = yes                  
        string_mask            = utf8only             
        req_extensions         = codesign_reqext      
        prompt                 = no   
        distinguished_name     = my_req_dn_no_prompt
        
        [my_req_dn_no_prompt]
        commonName             = ${options["commonName"]}
        countryName            = DE

        [codesign_reqext]
        keyUsage               = critical,digitalSignature
        extendedKeyUsage       = critical,codeSigning
        subjectKeyIdentifier   = hash
        """.stripIndent()

        def csr = new File(tempOutputDir, "ca.csr")
        def r = ['openssl', 'req',
                 '-new',
                 '-outform', 'PEM',
                 '-key', privateKey,
                 '-out', csr.path,
                 '-days', options["days"],
                 '-config', cnf.path,
        ].execute().waitFor()

        if (r == 0) {
            return csr
        }
        null
    }

    static File signCSR(File csr, CA ca) {
        def tempOutputDir = File.createTempDir()
        def cert = new File(tempOutputDir, "user.crt")
        def r = ['openssl', 'ca', '-batch',
                 '-policy', 'policy_any',
                 '-config', ca.config.path,
                 '-extensions', 'usr_cert',
                 '-out', cert.path,
                 '-passin', "pass:${ca.passphrase}",
                 '-infiles', csr.path].execute().waitFor()
        if (r == 0) {
            return cert
        }
        null
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

    static TestCertificate createTestCodeSigningCertificatePkcs12(Map options = [:], String password) {
        def ca = newCA(options)
        if (!ca) {
            return null
        }
        def key = createPrivateKey()

        if (!key) {
            return null
        }

        def csr = createCodeSigningCertificateRequest(options, key)

        if (!csr) {
            return null
        }

        def cert = signCSR(csr, ca)
        if (!cert) {
            return null
        }
        def p12 = createTestCertificatePkcs12(options, key, cert, password)

        new TestCertificate(p12, cert, csr, key)
    }

    static class TestCertificate {
        final File pkcs12
        final File cert
        final File csr
        final File privateKey
        final String certFingerprint

        TestCertificate(File pkcs12, File cert, File csr, File privateKey) {
            this.pkcs12 = pkcs12
            this.cert = cert
            this.csr = csr
            this.privateKey = privateKey
            this.certFingerprint = createFingerPrint(cert)
        }

        private static String createFingerPrint(File cert) {
            File outfile = new File(cert.parentFile, cert.name + ".der")
            def args = ['openssl', 'x509',
                        '-outform', 'der',
                        '-in', cert.path,
                        '-out', outfile.path
            ]
            if (args.execute().waitFor() == 0) {
                return outfile.bytes.digest("SHA-1")
            }
        }
    }
}
