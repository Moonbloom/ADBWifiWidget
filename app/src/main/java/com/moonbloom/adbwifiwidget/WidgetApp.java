package com.moonbloom.adbwifiwidget;

import android.app.Application;

public class WidgetApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //BStyle myStyle = new BStyle.Builder(BStyle.MESSAGE).setAutoCancel(false).setDuration(BConstants.BDuration.Long).build();
        //Boast.setDefaultBStyle(myStyle);
    }
}