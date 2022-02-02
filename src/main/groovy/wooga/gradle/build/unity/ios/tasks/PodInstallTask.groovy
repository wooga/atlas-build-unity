package wooga.gradle.build.unity.ios.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import wooga.gradle.build.unity.internal.ExecUtil

class PodInstallTask extends DefaultTask {
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
    protected getPodsDir() {
        projectDirectory.file("Pods")
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

    @TaskAction
    protected void install() {
        def executablePath = ExecUtil.getExecutable("pod")
        project.exec {
            executable executablePath
            args 'repo'
            args 'update'
        }

        project.exec {
            executable executablePath
            workingDir
            args 'install'
            args '--project-directory', projectDirectory.get().asFile.absolutePath
        }
    }
}
