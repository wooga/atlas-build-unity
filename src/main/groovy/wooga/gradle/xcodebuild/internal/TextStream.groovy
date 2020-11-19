package wooga.gradle.xcodebuild.internal

interface TextStream {
    void text(String text);

    void endOfStream(Throwable stream);
}
