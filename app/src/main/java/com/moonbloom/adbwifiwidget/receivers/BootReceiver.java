package com.moonbloom.adbwifiwidget.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.moonbloom.adbwifiwidget.otto.BusProvider;
import com.moonbloom.adbwifiwidget.otto.events.BootCompletedEvent;

public class BootReceiver extends BroadcastReceiver {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();
    //endregion

    @Override
    public void onReceive(Context context, Intent intent) {
        BusProvider.getInstance().post(new BootCompletedEvent(context));
    }
}