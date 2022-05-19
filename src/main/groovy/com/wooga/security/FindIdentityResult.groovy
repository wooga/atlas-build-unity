package com.wooga.security

import java.util.regex.Matcher

class FindIdentityResult {
    final Map<String, Set<String>> matchingIdentities
    final Map<String, Set<String>> validIdentities

    final List<String> automaticSelectors = [
            "Mac App Distribution",
            "iOS Developer",
            "iPhone Developer",
            "iOS Distribution",
            "iPhone Distribution",
            "Developer ID Application",
            "Apple Distribution",
            "Mac Developer",
            "Apple Development"
    ]

    private Boolean findIdentity(Map<String, Set<String>> identities, String identitySelector) {
        //check if identitySelector is a SHA-1 fingerprint
        if (identities.containsKey(identitySelector.toUpperCase())) {
            return true
        }
        //check if identitySelector is a automatic selector
        if (automaticSelectors.contains(identitySelector)) {
            //if yes check if any identities match the selector
            return identities.any { _, _identities -> _identities.any { it.startsWith("${identitySelector}:") } }
        }

        //last check if any identity matches the identity selector
        identities.any { _, _identities -> _identities.any { it == identitySelector } }
    }

    Boolean hasValidIdentity(String identitySelector) {
        findIdentity(validIdentities, identitySelector)
    }

    Boolean hasIdentity(String identitySelector, Boolean valid = false) {
        if (valid) {
            return hasValidIdentity(identitySelector)
        }
        findIdentity(matchingIdentities, identitySelector)
    }

    FindIdentityResult(Map<String, Set<String>> matchingIdentities, Map<String, Set<String>> validIdentities) {
        this.matchingIdentities = matchingIdentities
        this.validIdentities = validIdentities
    }

    static FindIdentityResult fromOutPut(String output) {
        Boolean parseMatchingIdentities = false
        Boolean parseValidIdentities = false
        Map<String, Set<String>> matchingIdentities = new HashMap<String, Set<String>>()
        Map<String, Set<String>> validIdentities = new HashMap<String, Set<String>>()
        output.eachLine {
            def line = it.trim()
            if (line =~ /\d+\) ([A-Z0-9]{40}) "(.*?)"/) {
                Map<String, Set<String>> collection = parseMatchingIdentities ? matchingIdentities : validIdentities
                def fullId = Matcher.lastMatcher[0][2].toString()
                def partialId = fullId.replaceAll(/ \(.*\)$/, '')
                def fingerPrint = Matcher.lastMatcher[0][1].toString()

                if (!collection.containsKey(fingerPrint)) {
                    collection.put(fingerPrint, new HashSet<String>(2))
                }

                collection[fingerPrint].add(fullId)
                collection[fingerPrint].add(partialId)
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
