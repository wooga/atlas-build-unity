#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@1.x') _

withCredentials([string(credentialsId: 'atlas_build_unity_coveralls_token', variable: 'coveralls_token')]) {
    buildGradlePlugin plaforms: ['osx','windows'], coverallsToken: coveralls_token, testEnvironment:[]
}
