package dev.ebullient.fc5;

import java.io.PrintWriter;

import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.CommandSpec;

public class Log {
    private Log() {
    }

    static final boolean picocliDebugEnabled = "DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"));

    private static PrintWriter out = new PrintWriter(System.out);
    private static PrintWriter err = new PrintWriter(System.err);
    private static boolean verbose = false;
    private static ColorScheme colors = null;

    public static void prepareStreams(CommandSpec spec) {
        if (spec != null) {
            Log.out = spec.commandLine().getOut();
            Log.err = spec.commandLine().getErr();
            Log.colors = spec.commandLine().getHelp().colorScheme();
        }
    }

    public static void setVerbose(boolean verbose) {
        Log.verbose = verbose;
    }

    public static boolean isVerbose() {
        return Log.verbose || picocliDebugEnabled;
    }

    public static void debugf(String format, Object... params) {
        if (isVerbose()) {
            debug(String.format(format, params));
        }
    }

    public static void debug(String output) {
        if (isVerbose()) {
            if (colors == null) {
                Log.out.println(output);
            } else {
                Log.out.println(colors.ansi().new Text("@|faint " + output + "|@", colors));
            }
        }
    }

    public static void outPrintf(String format, Object... args) {
        String output = String.format(format, args);
        if (colors == null) {
            Log.out.print(output);
        } else {
            Log.out.print(colors.ansi().new Text(output, colors));
        }
    }

    public static void outPrintln(String output) {
        if (colors == null) {
            Log.out.println(output);
        } else {
            Log.out.println(colors.ansi().new Text(output, colors));
        }
    }

    public static void errPrintln(String errorMsg) {
        if (colors == null) {
            Log.err.println(errorMsg);
        } else {
            Log.err.println(colors.ansi().new Text(errorMsg, colors));
        }
    }

    public static PrintWriter err() {
        return err;
    }
}
