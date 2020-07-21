package wooga.gradle.build.unity.secrets

import software.amazon.awssdk.auth.credentials.AwsCredentials

class BasicAWSCredentials implements AwsCredentials {

    final String accessKeyId
    final String secretAccessKey

    BasicAWSCredentials(String AWSAccessKeyId, String AWSSecretKey) {
        this.accessKeyId = AWSAccessKeyId
        this.secretAccessKey = AWSSecretKey
    }

    @Override
    String accessKeyId() {
        accessKeyId
    }

    @Override
    String secretAccessKey() {
        secretAccessKey
    }
}
