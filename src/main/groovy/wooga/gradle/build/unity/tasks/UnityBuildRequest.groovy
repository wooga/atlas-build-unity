package wooga.gradle.build.unity.tasks

class UnityBuildRequest extends UnityBuildEngineTask {

    UnityBuildRequest() {
        def args = super.defaultArgs()
        super.setupExecution(args)
    }
}
