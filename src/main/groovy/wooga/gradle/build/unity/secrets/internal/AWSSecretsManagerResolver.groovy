package wooga.gradle.build.unity.secrets.internal

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException
import wooga.gradle.build.unity.secrets.Secret
import wooga.gradle.build.unity.secrets.SecretResolver
import wooga.gradle.build.unity.secrets.SecretResolverException

class AWSSecretsManagerResolver implements SecretResolver {

    private final SecretsManagerClient secretsManager

    AWSSecretsManagerResolver(SecretsManagerClient client) {
        secretsManager = client
    }

    AWSSecretsManagerResolver(AwsCredentialsProvider credentials, Region region) {
        this(SecretsManagerClient.builder().credentialsProvider(credentials).region(region).build())
    }

    AWSSecretsManagerResolver(Region region) {
        this(SecretsManagerClient.builder().region(region).build())
    }

    AWSSecretsManagerResolver() {
        this(SecretsManagerClient.builder().build())
    }

    @Override
    Secret<?> resolve(String secretId) {
        GetSecretValueRequest request = GetSecretValueRequest.builder().secretId(secretId).build() as GetSecretValueRequest
        GetSecretValueResponse response = null
        Secret<?> secret = null
        try {
            response = secretsManager.getSecretValue(request)
        } catch (ResourceNotFoundException e) {
            throw new SecretResolverException("Unable to resolve secret with id ${secretId}", e)
        }

        if (response.secretString()) {
            return new DefaultSecret(response.secretString())
        }

        new DefaultSecret(response.secretBinary().asByteArray())
    }
}
