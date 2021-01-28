spock-macos-keychain-extension
==============================

[![Coverage Status](https://coveralls.io/repos/github/wooga/spock-github-extension/badge.svg?branch=master)](https://coveralls.io/github/wooga/spock-github-extension?branch=master)

@Keychain
---------

Used on a `MacOsKeychain` property or feature method this annotation will cause a test keychain to be created and injected before each feature method.
If the field is `@Shared` the keychain is only deleted after all feature methods have run. You can have has many such fields as you like in a single spec.

### Example

```groovy
class ExampleSpec extends Specification {
    @Shared
    @Keychain
    MacOsKeychain testKeychain

    def "create new pull request"() {
        given:
        testKeychain.unlock()

        when:
        
        then:
    }
}
```

It is also possible to inject a test repository directly into a feature method

```groovy
    @Keychain
    @Unroll
    def "can reset repo for each iteration"(String message, int expectedCommitCount, MacOsKeychain keychain) {
        given: "an unlocked keychain"
        keychain.unlock() 
    
        expect:
        //

        cleanup:

        where:
        message    | expectedCommitCount
        "commit 1" | 2
        "commit 2" | 2
        "commit 3" | 2

        and:
        keychain = null
    }
```

LICENSE
=======

Copyright 2021 Wooga GmbH

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

