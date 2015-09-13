package com.moonbloom.adbwifiwidget.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.moonbloom.adbwifiwidget.R;
import com.moonbloom.adbwifiwidget.WidgetApp;

public abstract class SharedPrefs {

    //region Variables
    //Filename for preference file
    private static String mPrefFile = "adb_wifi_widget_shared_preferences";
    //endregion

    //region Constructor
    private SharedPrefs() {

    }
    //endregion

    private static SharedPreferences.Editor getEditor() {
        SharedPreferences settings = WidgetApp.context.getSharedPreferences(mPrefFile, Context.MODE_PRIVATE);
        return settings.edit();
    }

    private static SharedPreferences getSharedPrefFile() {
        return WidgetApp.context.getSharedPreferences(mPrefFile, Context.MODE_PRIVATE);
    }

    //Clear all Shared Preferences
    public static void clearAllSharedPrefs() {
        getEditor().clear().commit();
    }

    //region Wrapper functions for getting preferences
    public static String getString(Pref pref) {
        return getSharedPrefFile().getString(pref.getPrefName(), WidgetApp.context.getResources().getString(pref.getDefaultPrefValue()));
    }

    public static Boolean getBool(Pref pref) {
        return getSharedPrefFile().getBoolean(pref.getPrefName(), WidgetApp.context.getResources().getBoolean(pref.getDefaultPrefValue()));
    }

    public static int getInt(Pref pref) {
        return getSharedPrefFile().getInt(pref.getPrefName(), WidgetApp.context.getResources().getInteger(pref.getDefaultPrefValue()));
    }

    public static long getLong(Pref pref) {
        return getSharedPrefFile().getLong(pref.getPrefName(), WidgetApp.context.getResources().getInteger(pref.getDefaultPrefValue()));
    }
    //endregion

    //region Wrapper functions for setting preferences
    public static void setString(Pref pref, String variable) {
        getEditor().putString(pref.getPrefName(), variable).commit();
    }

    public static void setBool(Pref pref, Boolean variable) {
        getEditor().putBoolean(pref.getPrefName(), variable).commit();
    }

    public static void setInt(Pref pref, int variable) {
        getEditor().putInt(pref.getPrefName(), variable).commit();
    }

    public static void setLong(Pref pref, long variable) {
        getEditor().putLong(pref.getPrefName(), variable).commit();
    }
    //endregion

    //List of all SharedPreferences, to easily keep track of them and their default values
    public enum Pref {
        lastInternetConnect("lastInternetConnect", R.integer.default_last_internet_connect),
        lastWidgetUpdate("lastWidgetUpdate", R.integer.default_last_widget_update);

        private String prefName;
        private int defaultPrefValue;

        public String getPrefName() {
            return this.prefName;
        }

        public int getDefaultPrefValue() {
            return this.defaultPrefValue;
        }

        Pref(String prefName, int defaultPrefValue) {
            this.prefName = prefName;
            this.defaultPrefValue = defaultPrefValue;
        }
    }
}