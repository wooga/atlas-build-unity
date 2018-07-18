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

package wooga.gradle.build.unity.ios.internal.utils

import org.w3c.dom.Document
import org.xml.sax.InputSource
import xmlwise.Plist

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import java.nio.file.Path

class SecurityUtil {

    static File keychainConfigFile = new File(System.getProperty("user.home"), "Library/Preferences/com.apple.security.plist")

    static String DLDBSearchList = 'DLDBSearchList'
    static String DbName = 'DbName'
    static String GUID = 'GUID'
    static String SubserviceType = 'SubserviceType'

    static boolean keychainIsAdded(File keychain) {
        allKeychainsAdded([keychain])
    }

    static boolean allKeychainsAdded(Iterable<File> keychains) {
        findAllKeychains(keychains).size() == keychains.size()
    }

    static void removeKeychain(File keychain) {
        removeKeychains([keychain])
    }

    static void removeKeychains(Iterable<File> keychains) {
        def keychainConfig = keychainConfigFile

        if (keychainConfig.exists()) {
            def config = Plist.load(keychainConfig)
            List<HashMap> dbSearchList = config[DLDBSearchList] as List<HashMap>
            dbSearchList.removeAll(findAllKeychains(keychains))

            if (dbSearchList.empty) {
                keychainConfig.delete()
            } else {
                keychainConfigFile.text = prettyXML(Plist.toPlist(config))
            }
        }

        assert !allKeychainsAdded(keychains)
    }

    static boolean addKeychain(File keychain) {
        addKeychains([keychain])
    }

    static boolean addKeychains(Iterable<File> keychains) {
        if (keychains.size() == 0 || allKeychainsAdded(keychains)) {
            return false
        }

        def keychainConfig = keychainConfigFile
        def config
        if (keychainConfig.exists()) {
            config = Plist.load(keychainConfig)
        } else {
            config = new HashMap<String, Object>()
            config[DLDBSearchList] = new ArrayList<HashMap>()
        }

        def hasChanges = false
        keychains.each { keychain ->
            if (!keychainIsAdded(keychain)) {
                def item = new HashMap(3)
                item[DbName] = keychain.path
                item[GUID] = "{${UUID.randomUUID().toString()}}".toString()
                item[SubserviceType] = 6

                (config[DLDBSearchList] as List<HashMap>).add(item)
                hasChanges = true
            }
        }

        if(hasChanges) {
            keychainConfigFile.text = prettyXML(Plist.toPlist(config))
        }
        hasChanges
    }

    private static List<Object> findAllKeychains(Iterable<File> keychains) {
        def keychainConfig = keychainConfigFile
        def result = new ArrayList<>()
        if (keychainConfig.exists()) {
            def config = Plist.load(keychainConfig)
            List<HashMap> dbSearchList = config[DLDBSearchList] as List<HashMap>
            result = dbSearchList.findAll { db ->
                File dbName = new File((String) db[DbName])
                dbName = expandPath(dbName)
                keychains.find { keychain ->
                    keychain = expandPath(keychain)

                    Path k = keychain.toPath()
                    Path p = dbName.toPath()

                    p = p.normalize().toAbsolutePath()
                    k = k.normalize().toAbsolutePath()
                    p.equals(k)
                } != null
            }
        }
        result
    }

    protected static String prettyXML(String xml) {

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))))

        XPath xPath = XPathFactory.newInstance().newXPath();
        org.w3c.dom.NodeList nodeList = (org.w3c.dom.NodeList) xPath.evaluate("//text()[normalize-space()='']",
                document,
                XPathConstants.NODESET)

        for (int i = 0; i < nodeList.getLength(); ++i) {
            org.w3c.dom.Node node = nodeList.item(i)
            node.getParentNode().removeChild(node)
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")

        StringWriter stringWriter = new StringWriter()
        StreamResult streamResult = new StreamResult(stringWriter)

        transformer.transform(new DOMSource(document), streamResult)

        stringWriter.toString()

    }

    protected static String expandPath(String path) {
        if (path.startsWith("~" + File.separator)) {
            path = System.getProperty("user.home") + path.substring(1)
        }
        path
    }

    protected static File expandPath(File path) {
        new File(expandPath(path.path))
    }
}
