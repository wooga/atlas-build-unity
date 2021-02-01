/*
 * Copyright 2018-2020 Wooga GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wooga.security

import com.wooga.security.command.DefaultKeychain
import com.wooga.security.command.ListKeychains

class KeychainLookupList implements List<File>, Set<File> {

    private class KeychainLookupIterator implements Iterator<File> {
        private Iterator<File> innerIterator
        private File current

        KeychainLookupIterator(Iterator<File> innerIterator) {
            this.innerIterator = innerIterator
        }

        @Override
        boolean hasNext() {
            return innerIterator.hasNext()
        }

        @Override
        File next() {
            current = innerIterator.next()
            return current
        }

        @Override
        void remove() {
            remove(current)
        }
    }

    private class KeychainLookupListIterator implements ListIterator<File> {
        private ListIterator<File> innerIterator
        private File current

        KeychainLookupListIterator(ListIterator<File> innerIterator) {
            this.innerIterator = innerIterator
        }

        @Override
        boolean hasNext() {
            return innerIterator.hasNext()
        }

        @Override
        File next() {
            current = innerIterator.next()
            return current
        }

        @Override
        void remove() {
            remove(current)
        }

        @Override
        boolean hasPrevious() {
            return innerIterator.hasPrevious()
        }

        @Override
        File previous() {
            current = innerIterator.previous()
            return current
        }

        @Override
        int nextIndex() {
            return innerIterator.nextIndex()
        }

        @Override
        int previousIndex() {
            return innerIterator.previousIndex()
        }

        @Override
        void set(File file) {
            throw new UnsupportedOperationException()
        }

        @Override
        void add(File file) {
            throw new UnsupportedOperationException()
        }
    }

    private final Domain domain

    KeychainLookupList(Domain domain) {
        this.domain = domain
    }

    KeychainLookupList() {
        this(Domain.user)
    }

    private static List<File> listKeychains(Domain domain) {
        new ListKeychains().withDomain(domain).execute()
    }

    private static void setKeychains(Iterable<File> keychains, Domain domain) {
        new ListKeychains().withDomain(domain).withKeychains(keychains).setKeychainSearchList().execute()
    }

    @Override
    int size() {
        listKeychains(domain).size()
    }

    @Override
    boolean isEmpty() {
        size() == 0
    }

    @Override
    boolean contains(Object o) {
        Objects.requireNonNull(o)

        if (!File.isInstance(o)) {
            throw new ClassCastException("expect object of type java.io.File")
        }

        File keychain = o as File
        listKeychains(domain).contains(canonical(keychain))
    }

    @Override
    Iterator<File> iterator() {
        new KeychainLookupIterator(listKeychains(domain).iterator())
    }

    @Override
    Object[] toArray() {
        listKeychains(domain).toArray()
    }

    @Override
    def <T> T[] toArray(T[] a) {
        listKeychains(domain).toArray(a)
    }

    @Override
    boolean add(File keychain) {
        Objects.requireNonNull(keychain)
        keychain = canonical(keychain)
        def keychains = listKeychains(domain)
        if (!keychains.contains(keychain) && keychains.add(keychain)) {
            setKeychains(keychains, domain)
            return true
        }
        false
    }

    @Override
    boolean remove(Object o) {
        Objects.requireNonNull(o)

        if (!File.isInstance(o)) {
            throw new ClassCastException("expect object of type java.io.File")
        }

        File k = canonical(o as File)
        def keychains = listKeychains(domain)
        if (keychains.remove(k)) {
            setKeychains(keychains, domain)
            return true
        }
        false
    }

    @Override
    boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c as Object)
        def keychains = c.collect {
            if (!File.isInstance(it)) {
                throw new ClassCastException("expect object of type java.io.File")
            }
            canonical(it as File)
        }
        listKeychains(domain).containsAll(keychains)
    }

    @Override
    boolean addAll(Collection<? extends File> c) {
        Objects.requireNonNull(c)
        def keychains = listKeychains(domain)
        if (keychains.addAll(c)) {
            setKeychains(keychains, domain)
            return true
        }
        false
    }

    @Override
    boolean addAll(int index, Collection<? extends File> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c as Object)
        def keychainsToRemove = c.collect {
            if (!File.isInstance(it)) {
                throw new ClassCastException("expect object of type java.io.File")
            }
            canonical(it as File)
        }
        def keychains = listKeychains(domain)
        def result = keychains.removeAll(keychainsToRemove)
        new ListKeychains().withDomain(domain).withKeychains(keychains).setKeychainSearchList().execute()
        result
    }

    @Override
    boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    void clear() {
        reset()
    }

    void reset() {
        resetKeychains(domain)
    }

    @Override
    File get(int index) {
        listKeychains(domain).get(index)
    }

    @Override
    File set(int index, File element) {
        throw new UnsupportedOperationException()
    }

    @Override
    void add(int index, File element) {
        throw new UnsupportedOperationException()
    }

    @Override
    File remove(int index) {
        throw new UnsupportedOperationException()
    }

    @Override
    int indexOf(Object o) {
        Objects.requireNonNull(o)

        if (!File.isInstance(o)) {
            throw new ClassCastException("expect object of type java.io.File")
        }

        listKeychains(domain).indexOf(canonical(o as File))
    }

    @Override
    int lastIndexOf(Object o) {
        Objects.requireNonNull(o)

        if (!File.isInstance(o)) {
            throw new ClassCastException("expect object of type java.io.File")
        }

        listKeychains(domain).lastIndexOf(canonical(o as File))
    }

    @Override
    ListIterator<File> listIterator() {
        listIterator(0)
    }

    @Override
    ListIterator<File> listIterator(int index) {
        new KeychainLookupListIterator(listKeychains(domain).listIterator(index))
    }

    @Override
    List<File> subList(int fromIndex, int toIndex) {
        listKeychains(domain).subList(fromIndex, toIndex)
    }

    File getLoginKeyChain() {
        getLoginKeyChain(domain)
    }

    File getDefaultKeyChain() {
        getDefaultKeyChain(domain)
    }

    static File getLoginKeyChain(Domain domain) {
        new DefaultKeychain().withDomain(domain).execute()
    }

    static File getDefaultKeyChain(Domain domain) {
        new DefaultKeychain().withDomain(domain).execute()
    }

    static String expandPath(String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1)
        }
        path
    }

    static File expandPath(File path) {
        new File(expandPath(path.path))
    }

    static File canonical(File keychain) {
        expandPath(keychain).canonicalFile
    }

    static void resetKeychains(Domain domain = Domain.user) {
        def rawValue = System.getenv().get("ATLAS_BUILD_UNITY_IOS_DEFAULT_KEYCHAINS")
        def defaultKeyChains
        if (rawValue) {
            defaultKeyChains = rawValue.split(File.pathSeparator).collect { canonical(new File(it)) }
        } else {
            defaultKeyChains = [getLoginKeyChain(domain), getDefaultKeyChain(domain)].unique()
        }
        setKeychains(defaultKeyChains, domain)
    }
}
