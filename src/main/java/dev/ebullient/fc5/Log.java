package dev.ebullient.fc5;

import java.io.PrintWriter;

import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.CommandSpec;

public final class Log {
    private Log() {
    }

    static final boolean picocliDebugEnabled = "DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"));

    private static PrintWriter out = new PrintWriter(System.out);
    private static PrintWriter err = new PrintWriter(System.err);
    private static boolean verbose;
    private static ColorScheme colors;

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

    public static void errorf(String format, Object... args) {
        error(null, String.format(format, args));
    }

    public static void errorf(Throwable th, String format, Object... args) {
        error(th, String.format(format, args));
    }

    public static void error(String errorMsg) {
        error(null, errorMsg);
    }

    public static void error(Throwable ex, String errorMsg) {
        if (colors == null) {
            Log.err.println(errorMsg);
        } else {
            Log.err.println(colors.ansi().text("⛔️@|fg(red) " + errorMsg + "|@"));
        }
        Log.err.flush();
        if (ex != null && isVerbose()) {
            ex.printStackTrace(err);
        }
    }

    public static void outPrintf(String format, Object... args) {
        String output = String.format(format, args);
        if (colors == null) {
            Log.out.print(output);
        } else {
            Log.out.print(colors.ansi().text(output));
        }
        Log.out.flush();
    }

    public static void outPrintln(String output) {
        if (colors == null) {
            Log.out.println(output);
        } else {
            Log.out.println(colors.ansi().new Text(output, colors));
        }
        Log.out.flush();
    }

    public static PrintWriter err() {
        return err;
    }
}
