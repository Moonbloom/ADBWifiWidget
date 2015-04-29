package com.moonbloom.adbwifiwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.moonbloom.boast.Boast;

public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            boolean isConnected = info.isConnected();

            if(ADBWifiWidget.debug) {
                if (isConnected) {
                    Boast.makeText(context, "WiFi: Enabled");
                } else {
                    Boast.makeText(context, "WiFi: Disabled");
                }
            }

            /*
            //Broadcast the update intent with the percentage as an extra
            Intent broadcastIntent = new Intent(ADBWifiWidget.localBroadcastUpdateWifiMsg);
            broadcastIntent.putExtra(ADBWifiWidget.wifiEnabledBroadcastExtra, isConnected);
            LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
            */
        }
    }
}