package wooga.gradle.build.unity.ios

import com.wooga.gradle.test.IntegrationSpec

abstract class IOSBuildIntegrationSpec extends IntegrationSpec {

    abstract String getSubjectUnderTestName()

    abstract String getSubjectUnderTestTypeName()

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }

    static wrapValueFallback = { Object rawValue, String type, Closure<String> fallback ->
        switch (type) {
            default:
                return rawValue.toString()
        }
    }
}
