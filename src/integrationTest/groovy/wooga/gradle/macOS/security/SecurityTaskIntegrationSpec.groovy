package wooga.gradle.macOS.security

import org.gradle.api.Task

import java.lang.reflect.ParameterizedType

abstract class SecurityTaskIntegrationSpec<T extends Task> extends SecurityIntegrationSpec {
    Class<T> getSubjectUnderTestClass() {
        if (!_sutClass) {
            try {
                this._sutClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                        .getActualTypeArguments()[0];
            }
            catch (Exception e) {
                this._sutClass = (Class<T>) null
            }
        }
        _sutClass
    }
    private Class<T> _sutClass

    String getSubjectUnderTestName() {
        "${subjectUnderTestClass.simpleName.uncapitalize()}Test"
    }

    String getSubjectUnderTestTypeName() {
        subjectUnderTestClass.getTypeName()
    }

    void appendToSubjectTask(String... lines) {
        buildFile << """
        $subjectUnderTestName {
            ${lines.join('\n')}
        }
        """.stripIndent()
    }
}
