package com.moonbloom.adbwifiwidget;

import android.app.Application;
import android.content.Context;

import com.moonbloom.boast.BConstants;
import com.moonbloom.boast.BStyle;
import com.moonbloom.boast.Boast;

public class WidgetApp extends Application {

    //region Variables
    //Only used for POJO classes that needs any kind of context to access system variables (resources, shared preferences, etc)
    public static Context context;
    //endregion

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        BStyle myStyle = new BStyle.Builder(BStyle.MESSAGE).setAutoCancel(true).setDuration(BConstants.BDuration.Long).build();
        Boast.setDefaultBStyle(myStyle);
    }
}