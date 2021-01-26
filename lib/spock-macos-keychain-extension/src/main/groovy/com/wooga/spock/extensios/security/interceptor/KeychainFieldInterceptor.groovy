package com.wooga.spock.extensios.security.interceptor

import com.wooga.security.MacOsKeychain
import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import spock.lang.Specification

@InheritConstructors
class KeychainFieldInterceptor extends KeychainInterceptor<FieldInfo> {

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        try {
            invocation.proceed()
        } finally {
            destroyKeychain(invocation)
        }
    }

    void install(FieldInfo info) {
        this.info = info
        final spec = info.parent.getTopSpec()
        spec.setupInterceptors.add(this)
        spec.cleanupInterceptors.add(this)
    }

    @Override
    MacOsKeychain createKeychain(IMethodInvocation invocation) {
        def keychain = super.createKeychain(invocation)
        def spec = getSpec(invocation)
        info.writeValue(spec, keychain)
        keychain
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        createKeychain(invocation)
        invocation.proceed()
    }

    void destroyKeychain(IMethodInvocation methodInvocation) {
        MacOsKeychain keychain = getKeychain(methodInvocation)
        keychain.delete()
    }

    protected Specification getSpec(IMethodInvocation invocation) {
        ((info.shared) ? invocation.sharedInstance : invocation.instance) as Specification
    }

    protected MacOsKeychain getKeychain(IMethodInvocation invocation) {
        final specInstance = getSpec(invocation)
        info.readValue(specInstance) as MacOsKeychain
    }
}
