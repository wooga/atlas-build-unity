package wooga.gradle.build.unity.tasks

class UnityBuildEngineTask extends AbstractUnityBuildEngineTask {

    UnityBuildEngineTask() {
        def args = super.defaultArgs()
        super.setupExecution(args)
    }
}