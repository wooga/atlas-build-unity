/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.build.unity.ios.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.*

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class KeychainTask extends ConventionTask {

    private String extension

    @Internal
    String getExtension() {
        extension
    }

    void setExtension(String value) {
        extension = value
    }

    KeychainTask extension(String extension) {
        setExtension(extension)
        this
    }

    private String baseName

    @Internal
    String getBaseName() {
        baseName
    }

    void setBaseName(String value) {
        baseName = value
    }

    KeychainTask baseName(String baseName) {
        setBaseName(baseName)
        this
    }

    @Input
    String getKeychainName() {
        getBaseName() + "." + getExtension()
    }

    private String password

    @Input
    String getPassword() {
        password
    }

    void setPassword(String value) {
        password = value
    }

    KeychainTask password(String password) {
        setPassword(password)
        this
    }

    private String certificatePassword

    @Input
    String getCertificatePassword() {
        certificatePassword
    }

    void setCertificatePassword(String value) {
        certificatePassword = value
    }

    KeychainTask certificatePassword(String certificatePassword) {
        setCertificatePassword(certificatePassword)
        this
    }

    @SkipWhenEmpty
    @InputFiles
    FileCollection certificates

    private File destinationDir

    @Internal
    File getDestinationDir() {
        destinationDir
    }

    void setDestinationDir(File value) {
        destinationDir = value
    }

    KeychainTask destinationDir(File destinationDir) {
        setDestinationDir(destinationDir)
        this
    }

    @OutputFiles
    protected FileCollection getOutputFiles() {
        project.fileTree(getDestinationDir()) {it.include(getKeychainName())}
    }

    File getOutputPath() {
        getOutputFiles().singleFile
    }

    @Internal
    File getTempKeychainPath() {
        new File(temporaryDir, getKeychainName())
    }

    @Internal
    File getTempLockFile() {
        MessageDigest digest = MessageDigest.getInstance("SHA-1")
        digest.update(getKeychainName().getBytes("ASCII"))
        byte[] passwordDigest = digest.digest()
        String hexString = passwordDigest.collect { String.format('%02x', it) }.join()
        new File(temporaryDir, ".fl${hexString.substring(0,8).toUpperCase()}")
    }

    @TaskAction
    protected void keychain() {

        if(getTempKeychainPath().exists()) {
            getTempKeychainPath().delete()
        }

        List<String> commands = new ArrayList()
        commands << "create-keychain -p '${getPassword()}' ${getTempKeychainPath()}"
        commands << "unlock-keychain -p '${getPassword()}' ${getTempKeychainPath()}"
        commands << "set-keychain-settings ${getTempKeychainPath()}"

        certificates.files.each { File file ->
            def keychain = getTempKeychainPath()
            def password = getCertificatePassword()
            commands << ""
            commands << "import $file -k $keychain -P '$password' -f pkcs12 -t cert -T /usr/bin/codesign"
            commands << ""
        }

        logger.info("Run scurity tasks:")
        logger.info(commands.join("\n"))

        project.exec {
            executable "security"
            args "-i"
            standardInput = new ByteArrayInputStream(commands.join("\n").getBytes(StandardCharsets.UTF_8))
        }

        def extension = getExtension()
        //move
        project.sync {
            from temporaryDir
            include "*.$extension"
            into getDestinationDir()
        }

        //delete
        getTempLockFile().deleteOnExit()
        getTempLockFile().delete()
    }
}
