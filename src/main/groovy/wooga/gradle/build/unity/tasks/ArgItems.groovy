package wooga.gradle.build.unity.tasks

class ArgItems {

    final List<String> args

    static ArgItems createArgs(Object args) {
        if(args instanceof Map) {
            return new ArgItems(args)
        } else if(args instanceof List) {
            return new ArgItems(args)
        } else {
            return new ArgItems(args.toString())
        }
    }

    static String createArgsString(Object args) {
        return createArgs(args).createArgString()
    }

    private ArgItems(String arg) {
        this([arg])
    }

    private ArgItems(List<String> args) {
        this.args = args
    }

    private ArgItems(Map<String, ?> args) {
        this.args = args.collect {argPair ->
            return argPair.value != null? "${argPair.key}=${argPair.value}" : argPair.key
        }
    }

    String createArgString() {
        return args.join(" ")
    }

}
