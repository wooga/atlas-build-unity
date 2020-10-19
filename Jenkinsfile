#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([
                    string(credentialsId: 'atlas_build_unity_coveralls_token', variable: 'coveralls_token'),
                    string(credentialsId: 'aws.secretsmanager.integration.accesskey', variable: 'accesskey'),
                    string(credentialsId: 'aws.secretsmanager.integration.secretkey', variable: 'secretkey'),
                ])
{
    def env = [
            "ATLAS_AWS_INTEGRATION_ACCESS_KEY=${accesskey}",
            "ATLAS_AWS_INTEGRATION_SECRET_KEY=${secretkey}",
            "ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC=YES"
    ]
    buildGradlePlugin plaforms: ['osx','windows', 'linux'], coverallsToken: coveralls_token, testEnvironment:env
}
