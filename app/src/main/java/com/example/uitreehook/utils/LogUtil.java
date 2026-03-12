package com.example.uitreehook.utils;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

/**
 * 日志工具类
 */

public class LogUtil {

    private static String logTag = "UiTreeHook" + "_LSP_TAG";
    private static boolean existsXposedBridge = true;

    static {
        try {
            Class.forName("de.robv.android.xposed.XposedBridge");
        } catch (ClassNotFoundException e) {
            existsXposedBridge = false;
        } catch (Exception ignored) {
        }
    }

    public LogUtil(String prefix) {
        logTag = prefix + "_LSP_TAG";
    }

    public static void debug(String tag, String msg) {
        Log.d(tag, msg);
        if (existsXposedBridge) {
            XposedBridge.log(String.format("[%s] [DEBUG] %s", tag, msg));
        }
    }

    public static void debug(String msg) {
        debug(logTag, msg);
    }

    public static void info(String tag, String msg) {
        Log.i(tag, msg);
        if (existsXposedBridge) {
            XposedBridge.log(String.format("[%s] [INFO] %s", tag, msg));
        }
    }

    public static void info(String msg) {
        info(logTag, msg);
    }

    public static void error(String tag, String msg) {
        Log.e(tag, msg);
        if (existsXposedBridge) {
            XposedBridge.log(new RuntimeException(
                    String.format("[%s] [ERROR] %s", tag, msg)
            ));
        }
    }

    public static void error(String msg) {
        error(logTag, msg);
    }

    public static void error(String tag, String msg, Throwable e) {
        Log.e(tag, msg, e);
        if (existsXposedBridge) {
            XposedBridge.log(new RuntimeException(
                    String.format("[%s] [ERROR] %s", tag, msg),
                    e
            ));
        }
    }

    public static void error(String msg, Throwable e) {
        error(logTag, msg, e);
    }

    public static void error(Throwable e) {
        error(logTag, "", e);
    }
}
