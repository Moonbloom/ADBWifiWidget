package com.moonbloom.adbwifiwidget.otto.events;

import android.content.Context;

public class BootCompletedEvent {

    //region Variables
    public Context context;
    //endregion

    //region Constructor
    public BootCompletedEvent(Context context) {
        this.context = context;
    }
    //endregion
}