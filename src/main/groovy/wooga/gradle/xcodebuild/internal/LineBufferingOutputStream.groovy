package wooga.gradle.xcodebuild.internal

import org.gradle.internal.SystemProperties
import org.gradle.internal.io.StreamByteBuffer

class LineBufferingOutputStream extends OutputStream {
    private static final int LINE_MAX_LENGTH = 1048576
    private boolean hasBeenClosed
    private final TextStream handler
    private StreamByteBuffer buffer
    private final OutputStream output
    private final byte lastLineSeparatorByte
    private final int lineMaxLength
    private int counter

    LineBufferingOutputStream(TextStream handler) {
        this(handler, 2048)
    }

    LineBufferingOutputStream(TextStream handler, int bufferLength) {
        this(handler, bufferLength, LINE_MAX_LENGTH)
    }

    LineBufferingOutputStream(TextStream handler, int bufferLength, int lineMaxLength) {
        this.handler = handler
        this.buffer = new StreamByteBuffer(bufferLength)
        this.lineMaxLength = lineMaxLength
        this.output = this.buffer.getOutputStream()
        byte[] lineSeparator = SystemProperties.getInstance().getLineSeparator().getBytes()
        this.lastLineSeparatorByte = lineSeparator[lineSeparator.length - 1]
    }

    void close() throws IOException {
        this.hasBeenClosed = true
        this.flushLine()
        this.handler.endOfStream((Throwable)null)
    }

    void write(int b) throws IOException {
        if (this.hasBeenClosed) {
            throw new IOException("The stream has been closed.")
        } else {
            this.output.write(b)
            ++this.counter
            if (this.endsWithLineSeparator(b) || this.counter >= this.lineMaxLength) {
                this.flushLine()
            }
        }
    }

    private boolean endsWithLineSeparator(int b) {
        byte currentByte = (byte)(b & 255)
        return currentByte == this.lastLineSeparatorByte || currentByte == 10
    }

    private void flushLine() {
        String text = this.buffer.readAsString()
        if (text.length() > 0) {
            this.handler.text(text)
        }

        this.counter = 0
    }

    void flush(){
    }
}
