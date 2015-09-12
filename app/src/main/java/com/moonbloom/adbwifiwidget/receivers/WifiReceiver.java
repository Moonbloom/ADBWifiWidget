package com.moonbloom.adbwifiwidget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.moonbloom.adbwifiwidget.otto.BusProvider;
import com.moonbloom.adbwifiwidget.otto.events.WifiStateChangedEvent;

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
                NetworkInfo.DetailedState state = networkInfo.getDetailedState();
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.DISCONNECTED) {
                    publishState(context, state);
                }
            }
        }
    }

    private void publishState(Context context, NetworkInfo.DetailedState state) {
        //Boast.makeText(context, "State: " + state);
        //Log.d(TAG, "State: " + state);

        //Publish the WifiStateChangedEvent
        BusProvider.getInstance().post(new WifiStateChangedEvent(context, state));
    }
}