package com.wooga.spock.extensios.security.interceptor

import com.wooga.security.MacOsKeychain
import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo

import java.lang.reflect.Parameter

@InheritConstructors
class KeychainFeatureInterceptor extends KeychainInterceptor<FeatureInfo> {

    private MacOsKeychain currentKeychain

    private static void injectKeychain(IMethodInvocation invocation, MacOsKeychain keychain) {
        Map<Parameter, Integer> parameters = [:]
        invocation.method.reflection.parameters.eachWithIndex { parameter, i ->
            parameters << [(parameter): i]
        }
        parameters = parameters.findAll { MacOsKeychain.equals it.key.type }

        // enlarge arguments array if necessary
        def lastMyInjectableParameterIndex = parameters*.value.max()
        lastMyInjectableParameterIndex = lastMyInjectableParameterIndex == null ?
                0 :
                lastMyInjectableParameterIndex + 1

        if (invocation.arguments.length < lastMyInjectableParameterIndex) {
            def newArguments = new Object[lastMyInjectableParameterIndex]
            System.arraycopy invocation.arguments, 0, newArguments, 0, invocation.arguments.length
            invocation.arguments = newArguments
        }

        parameters.each { parameter, i ->
            if (!invocation.arguments[i]) {
                invocation.arguments[i] = keychain
            }
        }
    }

    //execute feature
    @Override
    void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {
        currentKeychain = createKeychain(invocation)
        injectKeychain(invocation, currentKeychain)
        try {
            invocation.proceed()
        } finally {
            currentKeychain.delete()
        }
    }

    //NEW ITERATION
    @Override
    void interceptIterationExecution(IMethodInvocation invocation) throws Throwable {
        invocation.proceed()
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        invocation.proceed()
        invocation.spec.setupInterceptors.remove(this)
    }

    //SETUP FEATURE
    @Override
    void interceptFeatureExecution(IMethodInvocation invocation) throws Throwable {
        invocation.spec.addSetupInterceptor(this)
        invocation.proceed()
    }

    @Override
    void install(FeatureInfo info) {
        info.addInterceptor(this)
        info.addIterationInterceptor(this)
        info.featureMethod.addInterceptor(this)
    }
}
