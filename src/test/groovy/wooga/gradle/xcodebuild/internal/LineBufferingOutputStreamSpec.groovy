package wooga.gradle.xcodebuild.internal

import spock.lang.Specification

class LineBufferingOutputStreamSpec extends Specification {


    def handler = Mock(TextStream)
    def stream = new LineBufferingOutputStream(handler)
    def w = stream.newWriter()

    def "writes whole lines to handler"() {
        when:
        w.write("hello")
        w.flush()
        w.write(" world")
        w.flush()
        w.write("!\n")
        w.flush()

        w.write("hi")
        w.flush()
        w.write(" you")
        w.flush()
        w.write(" too!\n")
        w.flush()

        then:
        1 * handler.text("hello world!\n")
        1 * handler.text("hi you too!\n")
    }

    def "writes multiple lines from single String"() {
        when:
        w.write("""
        First line
        Second line
        Third line
        """.stripIndent().trim())
        w.flush()
        w.close()

        then:
        1 * handler.text("First line\n")
        1 * handler.text("Second line\n")
        1 * handler.text("Third line")
    }

    def "writes line to handler when line length exceeds max line lenght"() {
        given: "a stream with a short linelength"
        stream = new LineBufferingOutputStream(handler, 2048, 5)
        w = stream.newWriter()

        when: "write a few lines which are too long"
        w.write("hello world!\n")
        w.write("hi you too!\n")
        w.flush()

        then:
        1 * handler.text("hello")
        1 * handler.text(" worl")
        1 * handler.text("d!\n")
        1 * handler.text("hi yo")
        1 * handler.text("u too")
        1 * handler.text("!\n")
    }

    def "flushes last written bytes when stream closes"() {
        given: "a written line without line ending"
        w.write("a line without ending")

        when:
        w.flush()

        then:
        0 * handler.text("a line without ending")


        when:
        stream.close()

        then:
        1 * handler.text("a line without ending")
    }

    def "calls endOfStream on handler when stream closes"() {
        when:
        stream.close()

        then:
        1 * handler.endOfStream(null)
    }

    def "throws IOException when attempt to write on closed stream"() {
        given: "a closed stream"
        stream.close()

        when:
        w.write("one last string please")
        w.flush()

        then:
        def e = thrown(IOException)
        e.message == "The stream has been closed."
    }

    def "flush is a no-op"() {
        given: "an unfinished written line"
        w.write("a line without ending")
        w.flush()

        when:
        stream.flush()

        then:
        0 * handler.text("a line without ending")
    }
}
