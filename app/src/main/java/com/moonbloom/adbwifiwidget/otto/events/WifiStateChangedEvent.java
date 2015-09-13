package com.moonbloom.adbwifiwidget.otto.events;

import android.content.Context;
import android.net.NetworkInfo;

public class WifiStateChangedEvent {

    //region Variables
    private Context context;
    private NetworkInfo.DetailedState state;
    //endregion

    //region Constructor
    public WifiStateChangedEvent(Context context, NetworkInfo.DetailedState state) {
        this.context = context;
        this.state = state;
    }
    //endregion

    //region Get
    public Context getContext() {
        return context;
    }

    public NetworkInfo.DetailedState getState() {
        return state;
    }
    //endregion
}