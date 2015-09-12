package com.moonbloom.adbwifiwidget.viewmodels;

import android.app.Activity;
import android.os.Bundle;

import com.moonbloom.adbwifiwidget.R;

public class MainActivity extends Activity {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    //private WifiReceiver wifiReceiver;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        /*
        wifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(wifiReceiver, intentFilter);
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //unregisterReceiver(wifiReceiver);
    }
}