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

import wooga.gradle.build.unity.ios.internal.utils.SecurityUtil
import xmlwise.Plist

class KeychainLookupList implements List<File> {

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

    private static String DLDBSearchList = 'DLDBSearchList'
    private static String DbName = 'DbName'

    @Override
    int size() {
        def l = innerLookUpList()
        if (l) {
            return l.size()
        }
        return 0
    }

    @Override
    boolean isEmpty() {
        return size() == 0
    }

    @Override
    boolean contains(Object o) {
        Objects.requireNonNull(o)

        if (!File.isInstance(o)) {
            throw new ClassCastException("expect object of type java.io.File")
        }

        File keychain = o as File

        return SecurityUtil.keychainIsAdded(keychain)
    }

    @Override
    Iterator<File> iterator() {
        def l = innerLookUpList() ?: new ArrayList<File>()
        new KeychainLookupIterator(l.iterator())
    }

    @Override
    Object[] toArray() {
        return toArray(new Object[0])
    }

    @Override
    <T> T[] toArray(T[] a) {
        Objects.requireNonNull(a)

        def l = innerLookUpList() ?: new ArrayList<File>()

        if (a.length < l.size()) {
            return (T[]) Arrays.copyOf(l.toArray(), l.size(), a.getClass())
        }

        System.arraycopy(l.toArray(), 0, a, 0, l.size())
        if (a.size() > l.size()) {
            a[l.size()] = null
        }

        return a
    }

    @Override
    boolean add(File keychain) {
        SecurityUtil.addKeychain(keychain)
    }

    @Override
    boolean remove(Object keychain) {
        Objects.requireNonNull(keychain)

        if (!File.isInstance(keychain)) {
            throw new ClassCastException("expect object of type java.io.File")
        }

        File k = keychain as File

        SecurityUtil.removeKeychain(k)
        return !SecurityUtil.keychainIsAdded(k)
    }

    @Override
    boolean containsAll(Collection<?> c) {
        Objects.requireNonNull(c)

        for (Object o : c) {
            if (!File.isInstance(o)) {
                throw new ClassCastException("expect object of type java.io.File")
            }
        }

        return SecurityUtil.allKeychainsAdded(c as Collection<File>)
    }

    @Override
    boolean addAll(Collection<? extends File> c) {
        Objects.requireNonNull(c)

        return SecurityUtil.addKeychains(c)
    }

    @Override
    boolean addAll(int index, Collection<? extends File> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c)
        boolean modified = false
        Iterator<?> it = iterator()
        while (it.hasNext()) {
            if (c.contains(it.next())) {

                it.remove()
                modified = true
            }
        }

        return modified
    }

    @Override
    boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException()
    }

    @Override
    void clear() {
        File keychainConfigFile = new File(System.getProperty("user.home"), "Library/Preferences/com.apple.security.plist")
        keychainConfigFile.delete()
    }

    @Override
    File get(int index) {
        def l = innerLookUpList() ?: new ArrayList<File>()
        l.get(index)
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

        File keychain = o as File
        def l = innerLookUpList() ?: new ArrayList<File>()
        l.indexOf(keychain)
    }

    @Override
    int lastIndexOf(Object o) {
        Objects.requireNonNull(o)

        if (!File.isInstance(o)) {
            throw new ClassCastException("expect object of type java.io.File")
        }

        File keychain = o as File
        def l = innerLookUpList() ?: new ArrayList<File>()
        l.lastIndexOf(keychain)
    }

    @Override
    ListIterator<File> listIterator() {
        listIterator(0)
    }

    @Override
    ListIterator<File> listIterator(int index) {
        def l = innerLookUpList() ?: new ArrayList<File>()
        new KeychainLookupListIterator(l.listIterator(index))
    }

    //This implementation has not the desired effect for now.
    @Override
    List<File> subList(int fromIndex, int toIndex) {
        def l = innerLookUpList() ?: new ArrayList<File>()
        l.subList(fromIndex, toIndex)
    }

    private static List<File> innerLookUpList() {
        List<File> lookupList
        File keychainConfigFile = new File(System.getProperty("user.home"), "Library/Preferences/com.apple.security.plist")
        if (keychainConfigFile.exists()) {
            def config = Plist.load(keychainConfigFile)
            if (config[DLDBSearchList]) {
                lookupList = (config[DLDBSearchList] as List<Object>).collect { new File(it[DbName] as String) }
            }
        }
        lookupList
    }
}