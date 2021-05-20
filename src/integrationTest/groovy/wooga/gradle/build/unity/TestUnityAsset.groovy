package wooga.gradle.build.unity

import org.yaml.snakeyaml.Yaml

class TestUnityAsset implements Map {
    public static final String UNITY_ASSET_HEADER = """
            %YAML 1.1
            %TAG !u! tag:unity3d.com,2011:
            --- !u!114 &11400000
            """.stripIndent().trim()

    @Delegate
    private final Map inner = [:]

    private TestUnityAsset(Map content = [:]) {
        inner.putAll(content)
    }

    static TestUnityAsset unityAsset(Map content = [:]) {
        new TestUnityAsset(content)
    }

    Boolean write(File output) {
        output.text = dump()
    }

    String dump() {
        def output = new StringBuilder()
        output.append(UNITY_ASSET_HEADER)
        output.append("\n")
        Yaml yaml = new Yaml()
        output.append(yaml.dump(inner))
        output.toString()
    }
}
