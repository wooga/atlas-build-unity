#!groovy
@Library('github.com/wooga/atlas-jenkins-pipeline@0.0.3') _

pipeline {
    agent none

    stages {
        stage('Preparation') {
            agent any

            steps {
                sendSlackNotification "STARTED", true
            }
        }

        stage('check') {
            parallel {
                stage('Windows') {
                    agent {
                        label 'windows&&atlas'
                    }

                    environment {
                        COVERALLS_REPO_TOKEN                = credentials('atlas_plugins_coveralls_token')
                        TRAVIS_JOB_NUMBER                   = "${BUILD_NUMBER}.WIN"
                    }

                    steps {
                        gradleWrapper "check"
                    }

                    post {
                        success {
                            gradleWrapper "jacocoTestReport coveralls"
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'build/reports/jacoco/test/html',
                                reportFiles: 'index.html',
                                reportName: 'Coverage',
                                reportTitles: ''
                            ])
                        }

                        always {
                            junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                        }
                    }
                }

                stage('macOS') {
                    agent {
                        label 'osx&&atlas&&secondary'
                    }

                    environment {
                        COVERALLS_REPO_TOKEN                = credentials('atlas_plugins_coveralls_token')
                        TRAVIS_JOB_NUMBER                   = "${BUILD_NUMBER}.MACOS"
                    }

                    steps {
                        gradleWrapper "check"
                    }

                    post {
                        success {
                            gradleWrapper "jacocoTestReport coveralls"
                            publishHTML([
                                allowMissing: true,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'build/reports/jacoco/test/html',
                                reportFiles: 'index.html',
                                reportName: 'Coverage',
                                reportTitles: ''
                            ])
                        }

                        always {
                            junit allowEmptyResults: true, testResults: 'build/test-results/**/*.xml'
                        }
                    }
                }
            }

            post {
                always {
                    sendSlackNotification currentBuild.result, true
                }
            }
        }
    }
}
