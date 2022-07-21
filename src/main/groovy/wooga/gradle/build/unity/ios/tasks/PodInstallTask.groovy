package wooga.gradle.build.unity.ios.tasks

import com.wooga.gradle.io.ExecSpec
import com.wooga.gradle.io.LogFileSpec
import com.wooga.gradle.io.ProcessExecutor
import com.wooga.gradle.io.ProcessOutputSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*

class PodInstallTask extends DefaultTask implements ExecSpec, LogFileSpec, ProcessOutputSpec {
    private final DirectoryProperty projectDirectory = project.objects.directoryProperty()

    @Internal
    DirectoryProperty getProjectDirectory() {
        projectDirectory
    }

    void setProjectDirectory(Provider<Directory> value) {
        projectDirectory.set(value)
    }

    void setProjectDirectory(File value) {
        projectDirectory.set(value)
    }

    @InputFiles
    @SkipWhenEmpty
    protected FileCollection getInputFiles() {
        def podFile = projectDirectory.file("Podfile")
        def inputFiles = [podFile]

        if (podFile.get().asFile.exists()) {
            inputFiles << projectDirectory.file("Podfile.lock")
        }

        project.files(inputFiles.findAll { it.get().asFile.exists() }.toArray())
    }

    @OutputDirectory
    protected Provider<Directory> getPodsDir() {
        projectDirectory.dir("Pods")
    }

    private final Property<String> xcodeWorkspaceFileName = project.objects.property(String)

    @Input
    Property<String> getXcodeWorkspaceFileName() {
        xcodeWorkspaceFileName
    }

    void setXcodeWorkspaceFileName(Provider<String> value) {
        xcodeWorkspaceFileName.set(value)
    }

    void setXcodeWorkspaceFileName(String value) {
        xcodeWorkspaceFileName.set(value)
    }

    private final Property<String> xcodeProjectFileName = project.objects.property(String)

    @Input
    Property<String> getXcodeProjectFileName() {
        xcodeProjectFileName
    }

    void setXcodeProjectFileName(Provider<String> value) {
        xcodeProjectFileName.set(value)
    }

    void setXcodeProjectFileName(String value) {
        xcodeProjectFileName.set(value)
    }


    @OutputDirectory
    Provider<Directory> getXcodeWorkspacePath() {
        projectDirectory.dir(xcodeWorkspaceFileName)
    }

    @InputDirectory
    Provider<Directory> getXcodeProjectPath() {
        projectDirectory.dir(xcodeProjectFileName)
    }

    PodInstallTask() {
        executableName.convention("pod")
    }

    @TaskAction
    protected void install() {
        ProcessExecutor.from(this)
                .withArguments(['repo', 'update'])
                .withOutputLogFile(this, this)
                .execute()
                .assertNormalExitValue()

        ProcessExecutor.from(this)
                .withArguments('install', "--project-directory=${projectDirectory.get().asFile.absolutePath}")
                .execute()
                .assertNormalExitValue()
    }
}
