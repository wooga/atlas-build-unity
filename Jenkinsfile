#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'atlas_build_unity_coveralls_token', variable: 'coveralls_token')]) {
    buildGradlePlugin plaforms: ['osx','windows', 'linux'], coverallsToken: coveralls_token, testEnvironment:["ATLAS_BUILD_UNITY_IOS_EXECUTE_KEYCHAIN_SPEC=YES"]
}
