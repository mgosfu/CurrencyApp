package com.mgodevelopment.currencyapp.utils;

/**
 * Created by Martin on 9/13/2016.
 */
public class LogUtils {

    private static StringBuffer sStringBuffer = new StringBuffer();

    public interface LogListener {
        void onLogged(StringBuffer log);
    }

    private static LogListener sLogListener;

    public static void log(String tag, String message) {

    }

}
