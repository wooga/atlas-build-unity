package wooga.gradle.build.unity.internal

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Provider
import wooga.gradle.build.unity.UnityBuildPluginExtension

class UnityInputFilesFactory {

    final Project project
    final UnityBuildPluginExtension extension
    private final Closure<Boolean> excludeIgnoredFiles

    UnityInputFilesFactory(Project project, UnityBuildPluginExtension extension) {
        this.project = project
        this.extension = extension
        this.excludeIgnoredFiles = { FileTreeElement element  ->
            return extension.ignoreFilesForExportUpToDateCheck.contains(element.file)
        }
    }

    private FileTree createAssetsFileTree(Provider<String> buildTarget) {
        def assetsFileTree = project.fileTree(extension.assetsDir)

        def includeSpec = { FileTreeElement element ->
            def path = element.getRelativePath().getPathString().toLowerCase()
            def name = element.name.toLowerCase()
            def status = true
            if (path.contains("plugins") && !((name == "plugins") || (name == "plugins.meta"))) {
                /*
                 Why can we use / here? Because {@code element} is a {@code FileTreeElement} object.
                 The getPath() method is not the same as {@code File.getPath()}
                 From the docs:

                 * Returns the path of this file, relative to the root of the containing file tree. Always uses '/' as the hierarchy
                 * separator, regardless of platform file separator. Same as calling <code>getRelativePath().getPathString()</code>.
                 *
                 * @return The path. Never returns null.
                 */
                if (buildTarget.isPresent()) {
                    status = path.contains("plugins/" + buildTarget.get())
                } else {
                    status = true
                }
            }
            return status
        }

        assetsFileTree.include(includeSpec)
        assetsFileTree.exclude(excludeIgnoredFiles)
        return assetsFileTree
    }

    private FileTree createProjectSettingsFileTree(DirectoryProperty projectDirectory) {
        def projectSettingsDir = projectDirectory.dir("ProjectSettings")
        def projectSettingsFileTree = project.fileTree(projectSettingsDir)
        projectSettingsFileTree.exclude(excludeIgnoredFiles)
        return projectSettingsFileTree
    }

    private FileTree createPackageManagerFileTree(DirectoryProperty projectDirectory) {
        def packageManagerDir = projectDirectory.dir("UnityPackageManager")
        def packageManagerDirFileTree = project.fileTree(packageManagerDir)
        packageManagerDirFileTree.exclude(excludeIgnoredFiles)
        return packageManagerDirFileTree
    }

    FileCollection unityProjectInputFiles(Provider<String> buildTarget, DirectoryProperty projectDirectory) {
        def assetsFileTree = createAssetsFileTree(buildTarget)
        def projectSettingsFileTree = createProjectSettingsFileTree(projectDirectory)
        def packageManagerDirFileTree = createPackageManagerFileTree(projectDirectory)

        return project.files(assetsFileTree, projectSettingsFileTree, packageManagerDirFileTree)
    }

    Provider<FileCollection> unityTaskInputFilesProvider(Provider<String> buildTarget,
                                                         DirectoryProperty projectDirectory) {
        return project.provider {
            unityProjectInputFiles(buildTarget, projectDirectory)
        }
    }


}
