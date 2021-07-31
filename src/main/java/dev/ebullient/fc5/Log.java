package dev.ebullient.fc5;

import java.io.PrintWriter;

import org.jboss.logging.Logger;

import picocli.CommandLine.Model.CommandSpec;

public class Log {
    final static Logger log = Logger.getLogger(Log.class);

    private Log() {
    }

    private static PrintWriter out;
    private static PrintWriter err;
    static boolean useLogger = true;

    public static void prepareStreams(CommandSpec spec) {
        if (spec != null) {
            Log.out = spec.commandLine().getOut();
            Log.err = spec.commandLine().getErr();
        } else {
            Log.out = new PrintWriter(System.out);
            Log.err = new PrintWriter(System.err);
        }
    }

    public static void debugf(String format, Object... params) {
        log.debugf(format, params);
    }

    public static PrintWriter out() {
        return out;
    }

    public static PrintWriter err() {
        return err;
    }
}
