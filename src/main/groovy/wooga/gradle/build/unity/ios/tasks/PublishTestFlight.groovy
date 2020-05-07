package wooga.gradle.build.unity.ios.tasks

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.Callable

class PublishTestFlight extends ConventionTask {
    private Object ipa

    @SkipWhenEmpty
    @InputFiles
    protected FileCollection getInputFiles() {
        project.files(ipa)
    }

    @InputFile
    /**
     * -i, --ipa STRING     Path to the ipa file to upload (PILOT_IPA)
     */
    File getIpa() {
        project.files(ipa).singleFile
    }

    void setIpa(Object value) {
        ipa = value
    }

    PublishTestFlight ipa(Object ipa) {
        setIpa(ipa)
        this
    }

    private Object appIdentifier

    @Optional
    @Input
    String getAppIdentifier() {
        convertToString(appIdentifier)
    }

    void setAppIdentifier(Object value) {
        appIdentifier = value
    }

    PublishTestFlight appIdentifier(Object appIdentifier) {
        setAppIdentifier(appIdentifier)
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

    PublishTestFlight username(Object username) {
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

    PublishTestFlight password(Object password) {
        setPassword(password)
        this
    }

    private Object devPortalTeamId

    @Optional
    @Input
    String getDevPortalTeamId() {
        convertToString(devPortalTeamId)
    }

    void setDevPortalTeamId(Object value) {
        devPortalTeamId = value
    }

    PublishTestFlight devPortalTeamId(Object value) {
        setDevPortalTeamId(value)
        this
    }

    private Object itcProvider

    @Optional
    @Input
    String getItcProvider() {
        convertToString(itcProvider)
    }

    void setItcProvider(Object value) {
        itcProvider = value
    }

    PublishTestFlight itcProvider(Object value) {
        setItcProvider(value)
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

    PublishTestFlight teamId(Object value) {
        setTeamId(value)
        this
    }

    private Object teamName

    @Optional
    @Input
    String getTeamName() {
        convertToString(teamName)
    }

    void setTeamName(Object value) {
        teamName = value
    }

    PublishTestFlight teamName(Object value) {
        setTeamName(value)
        this
    }

    private Object skipSubmission

    @Optional
    @Input
    /**
     * Skip the distributing action of pilot and only upload the ipa file (PILOT_SKIP_SUBMISSION)
     */
    Boolean getSkipSubmission() {
        convertToBoolean(skipSubmission)
    }

    void setSkipSubmission(Object value) {
        skipSubmission = value
    }

    PublishTestFlight skipSubmission(Object value) {
        setSkipSubmission(value)
        this
    }

    private Object skipWaitingForBuildProcessing

    @Optional
    @Input
    /**
     * Don't wait for the build to process.
     *
     * -z, --skip_waiting_for_build_processing [VALUE] If set to true, the changelog won't be set, `distribute_external`
     * option won't work and no build will be distributed to testers.
     * (You might want to use this option if you are using this action on CI and have to pay for
     * 'minutes used' on your CI plan) (PILOT_SKIP_WAITING_FOR_BUILD_PROCESSING)
     */
    Boolean getSkipWaitingForBuildProcessing() {
        convertToBoolean(skipWaitingForBuildProcessing)
    }

    void setSkipWaitingForBuildProcessing(Object value) {
        skipWaitingForBuildProcessing = value
    }

    PublishTestFlight skipWaitingForBuildProcessing(Object value) {
        setSkipWaitingForBuildProcessing(value)
        this
    }

    PublishTestFlight() {
        super()
        outputs.upToDateWhen {false}
    }

    /**
     * Finds path to executable in PATH.
     *
     * This function is aimed to make the whole task testable.
     * The tests can override the PATH environment variable and
     * point to a mock executable.
     *
     * @param executableName the name of the executable to find in PATH
     * @return path to executable or executableName
     */
    private static String getExecutable(String executableName) {
        def path = System.getenv("PATH").split(File.pathSeparator)
                .collect {path -> new File(path, "fastlane")}
                .find {path -> path.exists() && path.isFile() && path.canExecute()}
        path? path.path : executableName
    }

    @TaskAction
    protected void publishTestFlight() {
        def executablePath = getExecutable("fastlane")
        project.exec {
            executable executablePath
            args "pilot", "upload"
            def pw = getPassword()

            if (pw) {
                environment('FASTLANE_PASSWORD', pw)
            }

            if (getUsername()) {
                args "--username", getUsername()
            }

            if(getDevPortalTeamId()) {
                args "--dev_portal_team_id", getDevPortalTeamId()
            }

            if(getTeamId()) {
                args "--team_id", getTeamId()
            }

            if(getTeamName()) {
                args "--team_name", getTeamName()
            }

            if(getAppIdentifier()) {
                args "--app_identifier", getAppIdentifier()
            }

            if(getItcProvider()) {
                args "--itc_provider", getItcProvider()
            }

            args "--skip_submission", getSkipSubmission()
            args "--skip_waiting_for_build_processing", getSkipWaitingForBuildProcessing()

            args "--ipa", getIpa().path
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
