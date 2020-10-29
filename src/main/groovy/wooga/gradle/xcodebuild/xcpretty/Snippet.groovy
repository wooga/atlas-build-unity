package wooga.gradle.xcodebuild.xcpretty

class Snippet {

    final String contents
    final String filePath

    Snippet(String contents, String filePath) {
        this.contents = contents
        this.filePath = filePath
    }

    static fromFile(String filePath) {
        try {
            def pathLine = filePath.split(':')
            def text = readSnippet(new File(pathLine[0]), Integer.parseInt(pathLine[1]))
            return new Snippet(text, filePath)
        } catch (ignored) {
            return new Snippet("", filePath)
        }
    }

    static readSnippet(File file, int aroundLine) {
        def text = ""
        def startingPostion = aroundLine - 2
        file.withReader { reader ->
            startingPostion.times {
                reader.readLine()
            }
            def lines = []
            3.times {
                lines << reader.readLine()
            }
            text += lines.findAll { it != null }.join("\n")
        }
        text + "\n"
    }
}
