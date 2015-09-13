package com.moonbloom.adbwifiwidget.utilities;

import android.util.Log;

import com.moonbloom.adbwifiwidget.BuildConfig;

@SuppressWarnings("FieldCanBeLocal")
public abstract class MLog {

    //region Variables
    private static int mLogLevel = 1; //Set to 0 to ignore calls to MLog without removing them from the code
    //endregion

    private static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    public static void makeLog(String tag, String text)	{
        if(!isDebuggable()) {
            return;
        }

        if(mLogLevel == 1) { //Standard log way
            Log.d(tag, text);
        } else if(mLogLevel == 2) { //Advanced way with stack trace
            int stackTraceAmount = 1;
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            String fileName = stackTraceElements[stackTraceAmount].getFileName();
            String methodName = stackTraceElements[stackTraceAmount].getMethodName();
            if(!text.equals("")) {
                Log.d(fileName, methodName + " - " + text);
            } else {
                Log.d(fileName, methodName);
            }
        }
    }
}