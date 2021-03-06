/*
package com.moonbloom.adbwifiwidget.testversions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.moonbloom.adbwifiwidget.otto.BusProvider;
import com.moonbloom.adbwifiwidget.otto.events.WifiStateChangedEvent;
import com.moonbloom.adbwifiwidget.utilities.SharedPrefs;

@SuppressWarnings("FieldCanBeLocal")
public class WifiReceiver extends BroadcastReceiver {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    private final long mConnectionOffsetTime = 2000;
    //endregion

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                NetworkInfo.DetailedState state = networkInfo.getDetailedState();
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.DISCONNECTED) {
                    if((System.currentTimeMillis() - SharedPrefs.getLong(SharedPrefs.Pref.lastInternetConnect)) >= mConnectionOffsetTime) {
                        SharedPrefs.setLong(SharedPrefs.Pref.lastInternetConnect, System.currentTimeMillis());
                        publishState(context, state);
                    }
                }
            }
        }
    }

    private void publishState(Context context, NetworkInfo.DetailedState state) {
        //Boast.makeText(context, "State: " + state);
        //MLog.makeLog(TAG, "State: " + state);

        //Publish the WifiStateChangedEvent
        BusProvider.getInstance().post(new WifiStateChangedEvent(context, state));
    }
}*/
