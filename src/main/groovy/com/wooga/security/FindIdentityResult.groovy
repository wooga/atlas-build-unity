package com.wooga.security

import java.util.regex.Matcher

class FindIdentityResult {
    final List<String> matchingIdentities
    final List<String> validIdentities

    Boolean hasValidIdentity(String identityName) {
        validIdentities.contains(identityName)
    }

    Boolean hasIdentity(String identityName, Boolean valid = false) {
        if (valid) {
            return hasValidIdentity(identityName)
        }
        matchingIdentities.contains(identityName)
    }

    FindIdentityResult(List<String> matchingIdentities, List<String> validIdentities) {
        this.matchingIdentities = matchingIdentities
        this.validIdentities = validIdentities
    }

    static FindIdentityResult fromOutPut(String output) {
        Boolean parseMatchingIdentities = false
        Boolean parseValidIdentities = false
        List<String> matchingIdentities = []
        List<String> validIdentities = []
        output.eachLine {
            def line = it.trim()
            if (line =~ /\d+\) ([A-Z0-9]{40}) "(.*?)"/) {
                def list = parseMatchingIdentities ? matchingIdentities : validIdentities
                def fullId = Matcher.lastMatcher[0][2].toString()
                def partialId = fullId.replaceAll(/ \(.*\)$/, '')
                list.push(fullId)
                list.push(partialId)
            } else if (line == "Matching identities") {
                parseMatchingIdentities = true
                parseValidIdentities = false
            } else if (line == "Valid identities") {
                parseMatchingIdentities = false
                parseValidIdentities = true
            }
        }

        new FindIdentityResult(matchingIdentities, validIdentities)
    }
}
