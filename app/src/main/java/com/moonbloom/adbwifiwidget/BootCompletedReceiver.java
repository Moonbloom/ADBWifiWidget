package com.moonbloom.adbwifiwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Start service to monitor WiFi connectivity
        Intent intentService = new Intent(context, WifiReceiverService.class);
        context.getApplicationContext().startService(intentService);

        //Broadcast intent to register click event on widget
        Intent broadcastIntent = new Intent(ADBWifiWidget.localRegisterClickEventMsg);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
    }
}