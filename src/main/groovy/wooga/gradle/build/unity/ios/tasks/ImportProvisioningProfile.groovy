/*
 * Copyright 2018 Wooga GmbH
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
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import wooga.gradle.build.unity.internal.ExecUtil

import java.util.concurrent.Callable

class ImportProvisioningProfile extends ConventionTask {

    private Object appIdentifier

    @Optional
    @Input
    String getAppIdentifier() {
        convertToString(appIdentifier)
    }

    void setAppIdentifier(Object value) {
        appIdentifier = value
    }

    ImportProvisioningProfile appIdentifier(Object appIdentifier) {
        setAppIdentifier(appIdentifier)
        this
    }


    private Object teamId

    @Optional
    @Input
    String getTeamId() {
        convertToString(teamId)
    }

    void setTeamId(Object value) {
        teamId = value
    }

    ImportProvisioningProfile teamId(Object teamId) {
        setTeamId(teamId)
        this
    }

    private Object username

    @Optional
    @Input
    String getUsername() {
        convertToString(username)
    }

    void setUsername(Object value) {
        username = value
    }

    ImportProvisioningProfile username(Object username) {
        setUsername(username)
        this
    }

    private Object password

    @Optional
    @Input
    String getPassword() {
        convertToString(password)
    }

    void setPassword(Object value) {
        password = value
    }

    ImportProvisioningProfile password(Object password) {
        setPassword(password)
        this
    }

    private String profileName

    @Input
    String getProfileName() {
        profileName
    }

    void setProfileName(String value) {
        profileName = value
    }

    ImportProvisioningProfile profileName(String profileName) {
        setProfileName(profileName)
        this
    }

    private Object provisioningName

    @Optional
    @Input
    String getProvisioningName() {
        convertToString(provisioningName)
    }

    void setProvisioningName(Object value) {
        provisioningName = value
    }

    ImportProvisioningProfile provisioningName(Object provisioningName) {
        setProvisioningName(provisioningName)
        this
    }

    private Object adhoc

    @Optional
    @Input
    Boolean getAdhoc() {
        convertToBoolean(adhoc)
    }

    void setAdhoc(Object value) {
        adhoc = value
    }

    ImportProvisioningProfile adhoc(Object value) {
        setAdhoc(value)
        this
    }


    private Object destinationDir

    @Input
    File getDestinationDir() {
        if(!destinationDir) {
            return null
        }

        project.file(destinationDir)
    }

    void setDestinationDir(Object value) {
        destinationDir = value
    }

    ImportProvisioningProfile destinationDir(Object destinationDir) {
        setDestinationDir(destinationDir)
        this
    }

    File getMobileProvisioningProfile() {
        new File(getDestinationDir(), getProfileName())
    }

    @OutputFiles
    protected FileCollection getOutputFiles() {
        project.files(new File(getDestinationDir(), getProfileName()))
    }

    ImportProvisioningProfile() {
        super()

        onlyIf(new Spec<ImportProvisioningProfile>() {
            @Override
            boolean isSatisfiedBy(ImportProvisioningProfile task) {
                return task.getTeamId() && task.getAppIdentifier()
            }
        })

        outputs.upToDateWhen {false}
    }



    @TaskAction
    protected void importProfiles() {
        def executablePath = ExecUtil.getExecutable("fastlane")
        project.exec {
            executable executablePath
            args "sigh"
            def pw = getPassword()

            if (pw) {
                environment('FASTLANE_PASSWORD', pw)
            }

            if (getUsername()) {
                args "--username", getUsername()
            }

            args "--team_id", getTeamId()
            args "--app_identifier", getAppIdentifier()

            def provisioningName = getProvisioningName()
            if (provisioningName) {
                args "--provisioning_name", provisioningName
            }

            args "--adhoc", getAdhoc().toString()
            args "--filename", getProfileName()
            args "--output_path", getDestinationDir()
        }
    }

    private static Boolean convertToBoolean(Object value) {
        if (!value) {
            return false
        }

        if (value instanceof Callable) {
            value = ((Callable) value).call()
        }

        value
    }

    private static String convertToString(Object value) {
        if (!value) {
            return null
        }

        if (value instanceof Callable) {
            value = ((Callable) value).call()
        }

        value.toString()
    }
}
