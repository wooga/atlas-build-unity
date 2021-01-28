package com.wooga.spock.extensios.security.interceptor

import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo

@InheritConstructors
class SharedKeychainFieldInterceptor extends KeychainFieldInterceptor {
    @Override
    void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
        createKeychain(invocation)
        invocation.proceed()
    }

    @Override
    void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        try {
            invocation.proceed()
        } finally {
            destroyKeychain(invocation)
        }
    }

    @Override
    void install(FieldInfo info) {
        this.info = info
        final spec = info.getParent().getTopSpec()
        spec.setupSpecInterceptors.add(this)
        spec.cleanupSpecInterceptors.add(this)
    }
}
