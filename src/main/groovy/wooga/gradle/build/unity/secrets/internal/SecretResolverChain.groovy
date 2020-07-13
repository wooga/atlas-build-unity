package wooga.gradle.build.unity.secrets.internal

import wooga.gradle.build.unity.secrets.Secret
import wooga.gradle.build.unity.secrets.SecretResolver
import wooga.gradle.build.unity.secrets.SecretResolverException

class SecretResolverChain implements SecretResolver, List<SecretResolver> {

    @Delegate
    private final List<SecretResolver> resolverChain

    SecretResolverChain(Iterable<SecretResolver> resolver) {
        resolverChain = []
        addAll(resolver)
    }

    SecretResolverChain() {
        this([])
    }

    void setResolverChain(Iterable<SecretResolver> resolver) {
        clear()
        addAll(resolver)
    }

    void setResolverChain(SecretResolver... resolver) {
        setResolverChain(resolver.toList())
    }

    @Override
    Secret<?> resolve(String secretId) {
        if(empty) {
            throw new SecretResolverException("No secret resolvers configured.")
        }

        Secret secret = null

        for(SecretResolver resolver in resolverChain) {
            try {
                secret = resolver.resolve(secretId)
                if(secret) {
                    break
                }
            }
            catch(SecretResolverException ignored) {}
        }

        if(!secret) {
            throw new SecretResolverException("Unable to resolve secret with id ${secretId}")
        }

        secret
    }
}
