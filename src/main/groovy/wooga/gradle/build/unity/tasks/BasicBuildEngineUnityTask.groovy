package wooga.gradle.build.unity.tasks

class BasicBuildEngineUnityTask extends BuildEngineUnityTask {

    BasicBuildEngineUnityTask() {
        def args = super.defaultArgs()
        super.setupExecution(args)
    }
}