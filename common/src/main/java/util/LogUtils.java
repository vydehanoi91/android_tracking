package util;

import android.util.Log;

import tracking.VnEAnalytics;

public class LogUtils {
    private enum Type {
        INFORMATION,
        ERROR,
        DEBUG
    }

    public static void info(String info) {
        info("", info);
    }

    public static void error(String error) {
        error("", error);
    }

    public static void debug(String debug) {
        debug("", debug);
    }

    public static void info(String TAG, String info) {
        log(Type.INFORMATION, TAG, info);
    }

    public static void error(String TAG, String error) {
        log(Type.ERROR, TAG, error);
    }

    public static void debug(String TAG, String debug) {
        log(Type.DEBUG, TAG, debug);
    }

    public static void log(Type type, String TAG, String message) {
        try {
            if (!VnEAnalytics.isDebugMode() || message == null)
                return;
            if (TAG == null)
                TAG = "";
            int maxLogSize = 2000;
            for (int i = 0; i <= message.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > message.length() ? message.length() : end;
                switch (type) {
                    case ERROR:
                        Log.e(TAG, message.substring(start, end));
                        break;
                    case DEBUG:
                        Log.d(TAG, message.substring(start, end));
                        break;
                    default:
                        Log.i(TAG, message.substring(start, end));
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
