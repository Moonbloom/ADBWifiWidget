package com.moonbloom.adbwifiwidget.otto;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public final class BusProvider {

    //region Variables
    private static final Bus mBus = new Bus(ThreadEnforcer.ANY);
    //endregion

    //region Constructor
    private BusProvider() {

    }
    //endregion

    //region Get
    public static Bus getInstance() {
        return mBus;
    }
    //endregion
}