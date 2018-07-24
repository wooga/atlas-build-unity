/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package wooga.gradle.build.unity.ios

import org.junit.ClassRule
import org.junit.contrib.java.lang.system.ProvideSystemProperty
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties
import wooga.gradle.build.unity.ios.internal.utils.SecurityUtil

@RestoreSystemProperties
class KeychainLookupListSpec extends Specification {

    @Shared
    File newHome = File.createTempDir("user", "home")

    @Shared
    @ClassRule
    public ProvideSystemProperty myPropertyHasMyValue = new ProvideSystemProperty("user.home", newHome.path)
            .and("keychain.noflush", "yes")

    @Shared
    KeychainLookupList subject = new KeychainLookupList()


    def setup() {
        SecurityUtil.getKeychainConfigFile().parentFile.mkdirs()
        SecurityUtil.getKeychainConfigFile().delete()
    }

    def cleanup() {
        SecurityUtil.getKeychainConfigFile().delete()
    }

    def "if keychain config doesn't exist it will be created"() {
        given: "non existing keychain config file"
        assert !SecurityUtil.getKeychainConfigFile().exists()

        when:
        subject.add(new File("path/to//test.keychain"))

        then:
        SecurityUtil.getKeychainConfigFile().exists()
    }

    def "empty list has size 0"() {
        given: "a empty lookup list"

        expect:
        subject.size() == 0
        subject.isEmpty()
    }

    def "populated list has size != 0"() {
        given: "a populated list"
        subject.add(new File("path/to/test.keychain"))
        subject.add(new File("path/to/test2.keychain"))

        expect:
        subject.size() != 0
        !subject.isEmpty()
    }

    def "adds a single keychain to the lookup list"() {
        given: "a empty lookup list"
        assert subject.size() == 0

        when:
        def result = subject.add(new File("path/to/test.keychain"))

        then:
        result
        subject.size() == 1
        subject.contains(new File("path/to/test.keychain"))

        when:
        result = subject.add(new File("path/to/test2.keychain"))

        then:
        result
        subject.size() == 2
        subject.contains(new File("path/to/test2.keychain"))
        subject.contains(new File("path/to/test.keychain"))
    }

    def "adds multiple keychains to the lookup list"() {
        given: "a empty lookup list"
        assert subject.size() == 0

        when:
        def result = subject.addAll([new File("path/to/test.keychain"), new File("path/to/test2.keychain")])

        then:
        result
        subject.size() == 2
        subject.contains(new File("path/to/test.keychain"))
        subject.contains(new File("path/to/test2.keychain"))

        when:
        result = subject.addAll([new File("path/to/test3.keychain"), new File("path/to/test4.keychain")])

        then:
        result
        subject.size() == 4
        subject.contains(new File("path/to/test.keychain"))
        subject.contains(new File("path/to/test2.keychain"))
        subject.contains(new File("path/to/test3.keychain"))
        subject.contains(new File("path/to/test4.keychain"))
    }

    @Unroll
    def "doesn't add duplicate entries when #message"() {
        given: "lookup list with one entry"
        subject.add(new File("~/path/to/test.keychain"))
        assert subject.size() == 1

        when:
        def result = subject.add(new File(fileToAdd))

        then:
        !result
        subject.size() == 1

        where:
        fileToAdd                                       | message
        "~/path/to/test.keychain"                       | "path is equal"
        "~/path/../path/to/test.keychain"               | "resolved path is equal"
        "${newHome.path}/path/../path/to/test.keychain" | "expanded ~/ path is equal"
    }

    @Unroll
    def "#method throws #error when #message"() {
        when:
        subject.invokeMethod(method, testObject)

        then:
        thrown(error)

        where:
        method        | testObject | error                     | message
        "contains"    | null       | NullPointerException      | "object is null"
        "indexOf"     | null       | NullPointerException      | "object is null"
        "lastIndexOf" | null       | NullPointerException      | "object is null"
        "contains"    | "test"     | ClassCastException        | "object is not a java.io.File"
        "remove"      | "test"     | ClassCastException        | "object is not a java.io.File"
        "indexOf"     | "test"     | ClassCastException        | "object is not a java.io.File"
        "lastIndexOf" | "test"     | ClassCastException        | "object is not a java.io.File"
        "add"         | "test"     | ClassCastException        | "object is not a java.io.File"
        "get"         | 22         | IndexOutOfBoundsException | "index is out of bounds"
    }

    // need to unroll these cases by hand because `invokeMethod` calls them with:
    // - non null default buildArguments
    // or
    // - doesn't know which overload to call

    def "retainAll throws UnsupportedOperationException"() {
        when:
        subject.retainAll(null)

        then:
        thrown(UnsupportedOperationException)
    }

    def "set(int,File) throws UnsupportedOperationException"() {
        when:
        subject.set(0, new File('some/file'))

        then:
        thrown(UnsupportedOperationException)
    }

    def "add(int,File) throws UnsupportedOperationException"() {
        when:
        subject.add(0, new File('some/file'))

        then:
        thrown(UnsupportedOperationException)
    }

    def "remove(int) throws UnsupportedOperationException"() {
        when:
        subject.remove(0)

        then:
        thrown(UnsupportedOperationException)
    }

    def "addAll(int, Collection<? extends File>) throws UnsupportedOperationException"() {
        when:
        subject.addAll(0, [new File('some/file')])

        then:
        thrown(UnsupportedOperationException)
    }


    def "remove throws NullPointerException when object is null"() {
        when:
        subject.remove(null)

        then:
        thrown(NullPointerException)
    }

    def "add throws NullPointerException when object is null"() {
        when:
        subject.add(null)

        then:
        thrown(NullPointerException)
    }

    def "addAll throws NullPointerException when object is null"() {
        when:
        subject.addAll(null as Collection)

        then:
        thrown(NullPointerException)
    }

    def "addAll throws NullPointerException when one or more objects in list are null"() {
        when:
        subject.addAll([new File('some/file'), null])

        then:
        thrown(NullPointerException)
    }

    def "removeAll throws NullPointerException when object is null"() {
        when:
        subject.removeAll(null)

        then:
        thrown(NullPointerException)
    }

    def "containsAll throws NullPointerException when object is null"() {
        when:
        subject.containsAll(null)

        then:
        thrown(NullPointerException)
    }

    def "containsAll throws ClassCastException when one or more objects in list are not of type java.io.File"() {
        when:
        subject.containsAll([new File('some/file'), "a String"])

        then:
        thrown(ClassCastException)
    }

    def "toArray throws NullPointerException when object is null"() {
        when:
        subject.toArray(null)

        then:
        thrown(NullPointerException)
    }

    def "toArray throws ArrayStoreException when object is not a java.io.File[] array"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))

        when:
        subject.toArray(new String[0])

        then:
        thrown(ArrayStoreException)
    }

    @Unroll
    def "contains checks if resolved path are equal when #message"() {
        given: "lookup list with one entry"
        subject.add(new File("~/path/to/test.keychain"))
        assert subject.size() == 1

        expect:
        subject.contains(new File(fileToCheck))

        where:
        fileToCheck                                     | message
        "~/path/to/test.keychain"                       | "path is equal"
        "~/path/../path/to/test.keychain"               | "resolved path is equal"
        "${newHome.path}/path/../path/to/test.keychain" | "expanded ~/ path is equal"
    }

    @Unroll
    def "containsAll checks if all keychains provided are in the list"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        expect:
        subject.containsAll(check) == expectedValue

        where:
        check                                                                       || expectedValue
        [new File("~/path/to/test.keychain")]                                       || true
        [new File("~/path/to/test.keychain"), new File("~/path/to/test2.keychain")] || true
        [new File("~/path/to/test.keychain"), new File("~/path/to/test4.keychain")] || false
        [new File("~/path/to/test4.keychain")]                                      || false
    }

    @Unroll
    def "clear delete all items"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        when:
        subject.clear()

        then:
        subject.isEmpty()
    }

    @Unroll
    def "removes items from the list when #message"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        when:
        def result = subject.remove(new File(fileToRemove))

        then:
        result
        subject.size() == 2
        !subject.contains(new File(fileToRemove))

        where:
        fileToRemove                                    | message
        "~/path/to/test.keychain"                       | "path is equal"
        "~/path/../path/to/test.keychain"               | "resolved path is equal"
        "${newHome.path}/path/../path/to/test.keychain" | "expanded ~/ path is equal"
    }

    @Unroll
    def "removes multiple items from the list"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        when:
        def result = subject.removeAll(filesToRemove)

        then:
        result == hasChanges
        subject.size() == expectedSize
        !subject.containsAll(filesToRemove)

        where:
        filesToRemove                                                               || expectedSize | hasChanges
        [new File("~/path/to/test.keychain")]                                       || 2            | true
        [new File("~/path/to/test.keychain"), new File("~/path/to/test2.keychain")] || 1            | true
        [new File("~/path/to/test.keychain"), new File("~/path/to/test4.keychain")] || 2            | true
        [new File("~/path/to/test4.keychain")]                                      || 3            | false
    }

    @Unroll
    def "creates #type over items"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        and: "a check counter"
        def checkList = []

        when:
        def iter = subject.invokeMethod(method, null)
        while (iter.hasNext()) {
            checkList.add(iter.next())
        }

        then:
        checkList.size() == subject.size()
        subject.containsAll(checkList)

        where:
        type            | method
        "iterator"      | "iterator"
        "list iterator" | "listIterator"
    }

    @Unroll
    def "list iterator can move in both directions"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        and: "a check counter"
        def checkList = []

        when:
        def iter = subject.listIterator(3)
        while (iter.hasPrevious()) {
            iter.previousIndex()
            checkList.add(iter.previous())
        }

        then:
        checkList.size() == subject.size()
        subject.containsAll(checkList)

        when:
        checkList.clear()
        iter = subject.listIterator(0)
        while (iter.hasNext()) {
            iter.nextIndex()
            checkList.add(iter.next())
        }

        then:
        checkList.size() == subject.size()
        subject.containsAll(checkList)
    }

    def "list iterator doesn't support set(File)"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        when:
        def iter = subject.listIterator()
        iter.next()
        iter.set(new File("some/file"))

        then:
        thrown(UnsupportedOperationException)
    }

    def "list iterator doesn't support add(File)"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        when:
        def iter = subject.listIterator()
        iter.next()
        iter.add(new File("some/file"))

        then:
        thrown(UnsupportedOperationException)
    }

    @Unroll
    def "#type can modify lookup list"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        when:
        def iter = subject.invokeMethod(method, null)
        while (iter.hasNext()) {
            if (iter.next() == new File("~/path/to/test2.keychain")) {
                iter.remove()
            }
        }

        then:
        subject.size() == 2
        !subject.contains(new File("~/path/to/test2.keychain"))

        where:
        type            | method
        "iterator"      | "iterator"
        "list iterator" | "listIterator"
    }

    def "creates Object[] array copy of items"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        when:
        def arr = subject.toArray()

        then:
        arr.length == subject.size()
        subject.containsAll(arr)

        when:
        arr = subject.toArray()
        subject.remove(new File("~/path/to/test2.keychain"))

        then:
        arr.length != subject.size()
        !subject.containsAll(arr)
    }

    @Unroll
    def "creates File[] array copy of items with #testArr"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        and:
        def expectSameSize = testArr.length <= subject.size()

        when:
        def arr = subject.toArray(testArr)

        then:
        (arr.length == subject.size()) == expectSameSize
        subject.containsAll(arr.findAll {it != null})

        when:
        arr = subject.toArray()
        subject.remove(new File("~/path/to/test2.keychain"))

        then:
        arr.length != subject.size()
        !subject.containsAll(arr.findAll {it != null})

        where:
        testArr << [new File[0], new File[3], new File[5]]
    }

    def "retrieves item by index"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        expect:
        subject[1] == new File("~/path/to/test2.keychain")
    }

    @Unroll
    def "#method returns #message"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        expect:
        subject.invokeMethod(method, item) == expectedIndex

        where:
        method        | item                                 | message                               || expectedIndex
        "indexOf"     | new File("~/path/to/test2.keychain") | "index when item is contained in list" | 1
        "indexOf"     | new File("~/path/to/test4.keychain") | "-1 when item can not be found"        | -1
        "lastIndexOf" | new File("~/path/to/test2.keychain") | "index when item is contained in list" | 1
        "lastIndexOf" | new File("~/path/to/test4.keychain") | "-1 when item can not be found"        | -1
    }

    def "creates a faulty sublist"() {
        given: "lookup list with multiple entries"
        subject.add(new File("~/path/to/test.keychain"))
        subject.add(new File("~/path/to/test2.keychain"))
        subject.add(new File("~/path/to/test3.keychain"))
        assert subject.size() == 3

        expect:
        subject.subList(1,2).size() == 1
    }

}
