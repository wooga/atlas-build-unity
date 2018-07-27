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
import org.gradle.util.GUtil
import wooga.gradle.build.unity.ios.XCAction

class XCodeArchiveTask extends ConventionTask {

    private Object projectPath
    private Object buildKeychain
    private Object provisioningProfile

    private Object destinationDir
    private String customName

    private Boolean clean

    @Input
    Boolean getClean() {
        clean
    }

    void setClean(Boolean value) {
        clean = value
    }

    XCodeArchiveTask clean(Boolean clean) {
        setClean(clean)
        this
    }

    @Optional
    @InputFiles
    protected FileCollection getInputFiles() {
        def files = [projectPath, buildKeychain, provisioningProfile].findAll { it != null }
        project.files(*files.toArray())
    }

    @Optional
    @Input
    Set<XCAction> getBuildActions() {
        def s = new HashSet<XCAction>()
        s << XCAction.archive

        if (getClean()) {
            s << XCAction.clean
        }
        s
    }

    @Optional
    @InputDirectory
    File getProjectPath() {
        project.files(projectPath).getSingleFile()
    }

    void setProjectPath(Object path) {
        projectPath = path
    }

    XCodeArchiveTask projectPath(Object path) {
        setExportOptionsPlist(path)
    }

    @Optional
    @InputFile
    File getBuildKeychain() {
        def files = project.files(buildKeychain)
        def fileList = files.files
        if (fileList) {
            return files.getSingleFile()
        }
        null
    }

    void setBuildKeychain(Object keyChain) {
        buildKeychain = keyChain
    }

    XCodeArchiveTask buildKeychain(Object keyChain) {
        setBuildKeychain(keyChain)
    }

    @Optional
    @InputFile
    File getProvisioningProfile() {
        def files = project.files(provisioningProfile)
        def fileList = files.files
        if (fileList) {
            return files.getSingleFile()
        }
        null
    }

    void setProvisioningProfile(Object profile) {
        provisioningProfile = profile
    }

    XCodeArchiveTask provisioningProfile(Object profile) {
        setProvisioningProfile(profile)
    }

    private String scheme

    @Optional
    @Input
    String getScheme() {
        scheme
    }

    void setScheme(String scheme) {
        this.scheme = scheme
    }

    XCodeArchiveTask scheme(String scheme) {
        setScheme(scheme)
        this
    }

    private String configuration

    @Input
    String getConfiguration() {
        configuration
    }

    void setConfiguration(String value) {
        configuration = value
    }

    XCodeArchiveTask configuration(String configuration) {
        setConfiguration(configuration)
        this
    }

    @Internal("Represented as part of archivePath")
    String getArchiveName() {
        if (customName != null) {
            return customName
        }
        String name = GUtil.elvis(getBaseName(), "") + maybe(getBaseName(), getAppendix())
        name += maybe(name, getVersion().toString())
        name += maybe(name, getClassifier())
        name += GUtil.isTrue(getExtension()) ? "." + getExtension() : ""
        return name
    }

    private static String maybe(String prefix, String value) {
        if (GUtil.isTrue(value)) {
            if (GUtil.isTrue(prefix)) {
                return "-".concat(value)
            } else {
                return value
            }
        }
        return ""
    }

    private String baseName

    @Internal("Represented as part of archivePath")
    String getBaseName() {
        baseName
    }

    void setBaseName(String value) {
        baseName = value
    }

    XCodeArchiveTask baseName(String baseName) {
        setBaseName(baseName)
        this
    }

    private String appendix

    @Internal("Represented as part of archivePath")
    String getAppendix() {
        appendix
    }

    void setAppendix(String value) {
        appendix = value
    }

    XCodeArchiveTask appendix(String appendix) {
        setAppendix(appendix)
        this
    }

    private String version

    @Internal("Represented as part of archivePath")
    String getVersion() {
        version
    }

    void setVersion(String value) {
        version = value
    }

    XCodeArchiveTask version(String version) {
        setVersion(version)
        this
    }

    private String extension

    @Internal("Represented as part of archivePath")
    String getExtension() {
        extension
    }

    void setExtension(String value) {
        extension = value
    }

    XCodeArchiveTask extension(String extension) {
        setExtension(extension)
        this
    }

    private String classifier

    @Internal("Represented as part of archivePath")
    String getClassifier() {
        classifier
    }

    void setClassifier(String value) {
        classifier = value
    }

    XCodeArchiveTask classifier(String classifier) {
        setClassifier(classifier)
        this
    }

    @OutputDirectory
    File getArchivePath() {
        new File(getDestinationDir(), getArchiveName())
    }

    @Internal("Represented as part of archivePath")
    File getDestinationDir() {
        project.file(destinationDir)
    }

    void setDestinationDir(Object destinationDir) {
        this.destinationDir = destinationDir
    }

    XCodeArchiveTask destinationDir(Object destinationDir) {
        setDestinationDir(destinationDir)
        this
    }

    @TaskAction
    protected executeXcodeBuild() {
        List<String> arguments = new ArrayList<String>()
        arguments << "xcodebuild"

        getBuildActions().each {
            arguments << it.toString()
        }

        if (getProjectPath()) {
            arguments << "-project" << getProjectPath().getPath()
        }

        if (getScheme()) {
            arguments << "-scheme" << getScheme()
        }

        if (getConfiguration()) {
            arguments << "-configuration" << getConfiguration()
        }

        if (getBuildKeychain()) {
            arguments << "OTHER_CODE_SIGN_FLAGS=--keychain ${getBuildKeychain()}"
        }

        arguments << "-archivePath" << getArchivePath().getPath()

        def derivedDataPath = new File(project.buildDir, "derivedData")
        derivedDataPath.mkdirs()

        arguments << "-derivedDataPath" << derivedDataPath.getPath()

        project.exec {
            executable "/usr/bin/xcrun"
            args = arguments
        }
    }
}