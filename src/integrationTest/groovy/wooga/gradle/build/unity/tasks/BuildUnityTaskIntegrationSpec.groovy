package wooga.gradle.build.unity.tasks

import com.wooga.gradle.test.TaskIntegrationSpec
import wooga.gradle.build.UnityIntegrationSpec

class BuildUnityTaskIntegrationSpec<T extends BuildUnityTask> extends UnityIntegrationSpec
    implements TaskIntegrationSpec<T> {

    @Override
    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}Test"
    }
}
