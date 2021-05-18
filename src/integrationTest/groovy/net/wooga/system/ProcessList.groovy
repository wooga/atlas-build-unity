package net.wooga.system

import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType

class ProcessList {
    static List<Integer> findPID(@ClosureParams(value = SimpleType, options = ['java.lang.String']) Closure predicate) {
        def stdout = new StringBuffer()
        def stderr = new StringBuffer()

        "ps aux".execute().waitForProcessOutput(stdout, stderr)

        stdout.readLines().findAll(predicate).collect {Integer.parseInt(it.split(/\s+/)[1])}
    }

    enum Signal {
        HUP,
        INT,
        QUIT,
        ABRT,
        KILL,
        ALRM,
        TERM
    }

    static Boolean kill(Integer pid, signal = Signal.TERM) {
        def r = "kill -s ${signal} ${pid}".execute().waitFor()
        r == 0
    }

    static List<Integer> waitForProcess(@ClosureParams(value = SimpleType, options = ['java.lang.String']) Closure predicate) {
        List<Integer> pids = []
        while(pids.empty) {
            sleep(1 * 1000)
            pids = findPID(predicate)
        }
        pids
    }
}
