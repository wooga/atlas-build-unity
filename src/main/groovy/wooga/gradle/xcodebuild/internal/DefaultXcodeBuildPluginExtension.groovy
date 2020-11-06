package wooga.gradle.xcodebuild.internal

import org.gradle.api.Project
import wooga.gradle.xcodebuild.XcodeBuildPluginExtension

class DefaultXcodeBuildPluginExtension implements XcodeBuildPluginExtension {

    final Project project

    DefaultXcodeBuildPluginExtension(Project project) {
        this.project = project
    }
}
