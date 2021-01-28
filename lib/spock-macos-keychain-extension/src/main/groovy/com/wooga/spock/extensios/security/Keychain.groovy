package com.wooga.spock.extensios.security

import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD])
@ExtensionAnnotation(KeychainExtension.class)
@interface Keychain {
    String fileName() default "test.keychain"
    String password() default "123456"
    int timeout() default -1
    boolean lockWhenSystemSleeps() default false
    boolean unlockKeychain() default false
}
