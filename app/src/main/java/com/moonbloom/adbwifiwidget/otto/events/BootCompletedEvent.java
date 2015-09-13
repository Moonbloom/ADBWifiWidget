package com.moonbloom.adbwifiwidget.otto.events;

import android.content.Context;

public class BootCompletedEvent {

    //region Variables
    private Context context;
    //endregion

    //region Constructor
    public BootCompletedEvent(Context context) {
        this.context = context;
    }
    //endregion

    //region Get
    public Context getContext() {
        return context;
    }
    //endregion
}