package com.moonbloom.adbwifiwidget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.moonbloom.adbwifiwidget.services.WifiReceiverService;
import com.moonbloom.adbwifiwidget.widget.ADBWifiWidget;

public class BootReceiver extends BroadcastReceiver {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();
    //endregion

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