package com.moonbloom.adbwifiwidget;

import android.app.Application;

import com.moonbloom.boast.BConstants;
import com.moonbloom.boast.BStyle;
import com.moonbloom.boast.Boast;

public class WidgetApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        BStyle myStyle = new BStyle.Builder(BStyle.MESSAGE).setAutoCancel(false).setDuration(BConstants.BDuration.Long).build();
        Boast.setDefaultBStyle(myStyle);
    }
}