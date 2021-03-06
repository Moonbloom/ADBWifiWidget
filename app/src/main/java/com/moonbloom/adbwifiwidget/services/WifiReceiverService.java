package com.moonbloom.adbwifiwidget.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import com.moonbloom.adbwifiwidget.receivers.WifiReceiver;
import com.moonbloom.adbwifiwidget.utilities.MLog;

public class WifiReceiverService extends Service {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    private WifiReceiver wifiReceiver = null;
    //endregion

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /*Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Boast.makeText(MyService.this, "onStartCommand");
            }
        });*/

        //MLog.makeLog(TAG, "onStartCommand");

        if(wifiReceiver == null) {
            wifiReceiver = new WifiReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            getApplicationContext().registerReceiver(wifiReceiver, intentFilter);
        }

        return Service.START_STICKY; //START_REDELIVER_INTENT
    }

    @Override
    public void onDestroy() {
        if(wifiReceiver != null) {
            try {
                unregisterReceiver(wifiReceiver);
            } catch(IllegalArgumentException ex) {
                MLog.makeLog(TAG, "Unregister WifiReceiver exception: " + ex.toString());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}