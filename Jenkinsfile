#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([
                    string(credentialsId: 'atlas_build_unity_coveralls_token', variable: 'coveralls_token'),
                    string(credentialsId: 'aws.secretsmanager.integration.accesskey', variable: 'accesskey'),
                    string(credentialsId: 'aws.secretsmanager.integration.secretkey', variable: 'secretkey'),
                    string(credentialsId: 'atlas_plugins_sonar_token', variable: 'sonar_token'),
                    string(credentialsId: 'atlas_plugins_snyk_token', variable: 'SNYK_TOKEN')
                ])
{
    def env = [
            'windows' : [
                    "ATLAS_AWS_INTEGRATION_ACCESS_KEY=${accesskey}",
                    "ATLAS_AWS_INTEGRATION_SECRET_KEY=${secretkey}",
            ],
            'linux': [
                    "ATLAS_AWS_INTEGRATION_ACCESS_KEY=${accesskey}",
                    "ATLAS_AWS_INTEGRATION_SECRET_KEY=${secretkey}",
            ],
            'macos': [
                    "ATLAS_AWS_INTEGRATION_ACCESS_KEY=${accesskey}",
                    "ATLAS_AWS_INTEGRATION_SECRET_KEY=${secretkey}",
                    "ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC=YES",
            ]
    ]

    def testLabels = [
        'macos': 'xcode'
    ]
    buildGradlePlugin platforms: ['macos','windows', 'linux'], coverallsToken: coveralls_token, sonarToken: sonar_token, testEnvironment:env, testLabels: testLabels
}
