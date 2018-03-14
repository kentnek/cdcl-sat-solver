package com.kentnek.cdcl;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Simple logger.
 * <p>
 *
 * @author kentnek
 */

public class Logger {
    private static boolean showDebug = false;

    public static void setShowDebug(boolean showDebug) {
        Logger.showDebug = showDebug;
    }

    public static void log(Object... objs) {
        List<String> ret = Arrays.stream(objs).map(Object::toString).collect(toList());
        System.out.println(String.join(" ", ret));
    }

    public static void debug(Object... objs) {
        if (showDebug) log(objs);
    }
}
