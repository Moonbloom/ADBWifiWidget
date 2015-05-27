package com.moonbloom.adbwifiwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

public class WifiReceiver extends BroadcastReceiver {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();
    //endregion

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    createLocalBroadcast(context, true);
                } else if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                    createLocalBroadcast(context, false);
                }
            }
        }
    }

    private void createLocalBroadcast(Context context, boolean isConnected) {
        //Boast.makeText(context, "Connected: " + isConnected);
        //Log.d(TAG, "Connected: " + isConnected);

        //Broadcast the update intent with the connection status as an extra
        Intent broadcastIntent = new Intent(ADBWifiWidget.localBroadcastUpdateWifiMsg);
        broadcastIntent.putExtra(ADBWifiWidget.wifiEnabledBroadcastExtra, isConnected);
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
    }
}