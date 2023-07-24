package com.gitthub.wujun728.engine.bytecodes.util;

public class OSDetect {
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static String PATH_DELEMETER = isWindows()?";":":";

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }
}
