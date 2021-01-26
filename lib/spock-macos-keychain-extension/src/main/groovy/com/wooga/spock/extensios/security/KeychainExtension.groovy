package com.wooga.spock.extensios.security

import com.wooga.spock.extensios.security.interceptor.KeychainFeatureInterceptor
import com.wooga.spock.extensios.security.interceptor.KeychainFieldInterceptor
import com.wooga.spock.extensios.security.interceptor.KeychainInterceptor
import com.wooga.spock.extensios.security.interceptor.SharedKeychainFieldInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo

class KeychainExtension extends AbstractAnnotationDrivenExtension<Keychain> {
    @Override
    void visitFeatureAnnotation(Keychain annotation, FeatureInfo feature) {
        def interceptor

        interceptor = new KeychainFeatureInterceptor(annotation)
        interceptor.install(feature)
    }

    @Override
    void visitFieldAnnotation(Keychain annotation, FieldInfo field) {
        KeychainInterceptor interceptor

        if(field.isShared()) {
            interceptor = new SharedKeychainFieldInterceptor(annotation)
        } else {
            interceptor = new KeychainFieldInterceptor(annotation)
        }

        interceptor.install(field)
    }
}
