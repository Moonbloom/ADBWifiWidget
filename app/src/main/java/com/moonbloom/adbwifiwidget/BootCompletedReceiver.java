package com.moonbloom.adbwifiwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentService = new Intent(context, HolderService.class);
        context.getApplicationContext().startService(intentService);

        //Broadcast the update intent with the connection status as an extra
        Intent broadcastIntent = new Intent(ADBWifiWidget.localRegisterClickEventMsg);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
    }
}