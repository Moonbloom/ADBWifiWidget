package com.moonbloom.adbwifiwidget.otto.events;

import android.content.Context;
import android.net.NetworkInfo;

public class WifiStateChangedEvent {

    //region Variables
    public Context context;
    public NetworkInfo.DetailedState state;
    //endregion

    //region Constructor
    public WifiStateChangedEvent(Context context, NetworkInfo.DetailedState state) {
        this.context = context;
        this.state = state;
    }
    //endregion
}