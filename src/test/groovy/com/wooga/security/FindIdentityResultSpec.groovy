package com.wooga.security

import spock.lang.Specification
import spock.lang.Unroll

class FindIdentityResultSpec extends Specification {

    @Unroll
    def "FindIdentityResult hasIdentity checks for #valid identity when #reason"() {
        given:
        def r = FindIdentityResult.fromOutPut("""
        Policy: Code Signing
            Matching identities
            1) 8A3544869D91B8C6972E44BC67F344529D359B91 "iPhone Distribution: FooBar GmbH (F2LSBBAR88)"
                1 identities found
            
            Valid identities only
            1) 8A3544869D91B8C6972E44BC67F344529D359B91 "iPhone Distribution: FooBar GmbH (F2LSBBAR88)"
                1 valid identities found
        """.stripIndent())

        expect:
        r.hasIdentity(identity, false) == expectedResult

        where:
        identity                                        | mustBeValid | expectedResult | detail
        "iPhone Distribution: FooBar GmbH"              | false       | true           | "shortId"
        "iPhone Distribution: FooBar GmbH"              | true        | true           | "shortId"
        "iPhone Distribution: FooBar GmbH (F2LSBBAR88)" | false       | true           | "fullId"
        "iPhone Distribution: FooBar GmbH (F2LSBBAR88)" | true        | true           | "fullId"
        "iPhone Distribution: BarFoo GmbH"              | false       | false          | "shortId"
        "iPhone Distribution: BarFoo GmbH"              | true        | false          | "shortId"
        "iPhone Distribution: BarFoo GmbH (F2LSBBAR88)" | false       | false          | "fullId"
        "iPhone Distribution: BarFoo GmbH (F2LSBBAR88)" | true        | false          | "fullId"

        valid = (mustBeValid) ? "valid" : "any"
        reason = expectedResult ? "identity with ${detail} can be found" : "identity with ${detail} can not be found"
    }
}
