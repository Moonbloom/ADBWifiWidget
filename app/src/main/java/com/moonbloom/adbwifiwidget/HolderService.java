package com.moonbloom.adbwifiwidget;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

public class HolderService extends Service {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    private WifiReceiver wifiReceiver = null;
    //endregion

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
/*        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Boast.makeText(MyService.this, "onStartCommand");
            }
        });*/

        Log.d(TAG, "onStartCommand");

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
            unregisterReceiver(wifiReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}