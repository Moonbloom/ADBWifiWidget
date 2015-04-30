package com.moonbloom.adbwifiwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.moonbloom.boast.BStyle;
import com.moonbloom.boast.Boast;

public class WifiReceiver extends BroadcastReceiver {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    private static int count = 0;

    static {
        BStyle style = new BStyle.Builder(BStyle.MESSAGE).setAutoCancel(true).build();
        Boast.setDefaultBStyle(style);
    }
    //endregion

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        if(networkInfo == null) {
            Boast.makeText(context, "Network info is NULL");
            return;
        }

        Log.d(TAG, "Type: " + networkInfo.getType() + " - State: " + networkInfo.getState());
        count++;
        int localCount = count;
        Boast.makeText(context, "count: " + localCount);
        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            if(networkInfo.getState() == NetworkInfo.State.CONNECTING) {
                Boast.makeText(context, localCount + " Wifi connecting");
            } else if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                Boast.makeText(context, localCount + " Wifi connected");
            } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTING) {
                Boast.makeText(context, localCount + " Wifi disconnecting");
            } else if (networkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                Boast.makeText(context, localCount + " Wifi disconnected");
            }
        } else {
            Boast.makeText(context, localCount + " Not wifi.. it's something else..");
        }

        /*
        Log.d(TAG, "Checking wifi state ...");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        SupplicantState supState = wifiInfo.getSupplicantState();
        Log.d(TAG, "Supplicant state: " + supState);

        if (supState.equals(SupplicantState.COMPLETED)) {
            Boast.makeText(context, "Wifi enabled & connected");
        } else {
            if (supState.equals(SupplicantState.SCANNING)) {
                Boast.makeText(context, "Wifi scanning");
            } else if (supState.equals(SupplicantState.DISCONNECTED)) {
                Boast.makeText(context, "Wifi disconnected");
            } else {
                Boast.makeText(context, "Wifi enabling");
            }
        }*/

        /*int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE ,WifiManager.WIFI_STATE_UNKNOWN);

        switch(extraWifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
                Boast.makeText(context, "Disabled");
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                Boast.makeText(context, "Disabling");
                break;

            case WifiManager.WIFI_STATE_ENABLED:
                Boast.makeText(context, "Enabled");
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                Boast.makeText(context, "Enabling");
                break;

            case WifiManager.WIFI_STATE_UNKNOWN:
                Boast.makeText(context, "Unknown !!!");
                break;

            default:
                Boast.makeText(context, "Default");
                break;
        }*/

        //Broadcast the update intent with the percentage as an extra
        //Intent broadcastIntent = new Intent(ADBWifiWidget.localBroadcastUpdateWifiMsg);
        //broadcastIntent.putExtra(ADBWifiWidget.wifiEnabledBroadcastExtra, isConnected);
        //LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(broadcastIntent);
    }
}