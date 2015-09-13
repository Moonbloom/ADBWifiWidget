package com.moonbloom.adbwifiwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.moonbloom.adbwifiwidget.R;
import com.moonbloom.adbwifiwidget.otto.BusProvider;
import com.moonbloom.adbwifiwidget.otto.events.BootCompletedEvent;
import com.moonbloom.adbwifiwidget.otto.events.WifiStateChangedEvent;
import com.moonbloom.adbwifiwidget.services.WifiReceiverService;
import com.moonbloom.adbwifiwidget.utilities.MLog;
import com.moonbloom.adbwifiwidget.utilities.SharedPrefs;
import com.moonbloom.boast.BConstants;
import com.moonbloom.boast.BStyle;
import com.moonbloom.boast.Boast;
import com.squareup.otto.Subscribe;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.TimeoutException;

//https://bitbucket.org/RankoR/adb-over-network/src/

@SuppressWarnings("FieldCanBeLocal")
public class ADBWifiWidget extends AppWidgetProvider {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    private static final String USER_CLICKED = "userClickedSwitchState";

    private final String invalidIp = "0.0.0.0";

    private final String GET_PROP_COMMAND = "getprop service.adb.tcp.port";
    private final String SET_PROP_COMMAND_ON = "setprop service.adb.tcp.port 5555";
    private final String SET_PROP_COMMAND_OFF = "setprop service.adb.tcp.port -1";
    private final String START_ADBD_COMMAND = "start adbd";
    private final String STOP_ADBD_COMMAND = "stop adbd";

    private boolean isActive = false;

    private final long mOffsetTime = 1000;
    //endregion

    //region Overrides
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        //Register receiver and start service when the widget is added
        registerBusAndStartService(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        //Unregister receiver and stop service when the widget is removed
        unregisterBusAndStopService(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        runRunnable(context, null);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        //showBoast(context, "Receive");
        runRunnable(context, intent);
    }
    //endregion

    //region Subscribe
    @Subscribe
    public void onWifiStateChanged(WifiStateChangedEvent event) {
        runRunnable(event.getContext(), null);
    }

    @Subscribe
    public void onBootCompleted(BootCompletedEvent event) {
        setOnClickEvent(event.getContext());

        //runRunnable(event.getContext(), null);
    }
    //endregion

    //region Start/Stop Bus & Service
    private void registerBusAndStartService(Context context) {
        BusProvider.getInstance().register(this);

        Intent intentService = new Intent(context, WifiReceiverService.class);
        context.getApplicationContext().startService(intentService);
    }

    private void unregisterBusAndStopService(Context context) {
        try {
            BusProvider.getInstance().unregister(this);
        } catch (IllegalArgumentException ex) {
            MLog.makeLog(TAG, "Unregister Bus/Stop Service exception: " + ex.toString());
        }

        Intent intentService = new Intent(context, WifiReceiverService.class);
        context.getApplicationContext().stopService(intentService);
    }
    //endregion

    private void setOnClickEvent(Context context) {
        //Construct the RemoteViews & ComponentName objects
        RemoteViews remoteViews = createRemoteViews(context);
        ComponentName componentName = createComponentName(context);

        //Set click event to call onReceive()
        remoteViews.setOnClickPendingIntent(R.id.widget_parent_relative_layout, createPendingSelfIntent(context));

        //Update widget to enable the new onClickPendingIntent
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }

    private void runRunnable(final Context context, final Intent intent) {
        if((System.currentTimeMillis() - SharedPrefs.getLong(SharedPrefs.Pref.lastWidgetUpdate)) >= mOffsetTime) {
            SharedPrefs.setLong(SharedPrefs.Pref.lastWidgetUpdate, System.currentTimeMillis());

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    setOnClickEvent(context);

                    //Check for root, cant do anything without root
                    if(!RootTools.isRootAvailable()) {
                        updateView(context, context.getString(R.string.not_rooted), R.drawable.adb_icon_off);
                        return;
                    }

                    //Not 100% sure if this is needed, as i don't trust it to always be registered/started
                    //registerBusAndStartService(context);

                    //Update internally
                    updateState(context, false);

                    //If it's a user click, switch the state
                    if(intent != null && intent.getAction() != null && intent.getAction().equals(USER_CLICKED)) {
                        switchState(context, true);
                    }

                    //Close shells to remove root icon
                    closeAllShells();
                }
            };

            Thread thread = new Thread(runnable);
            thread.start();
        }
    }

    //region Create elements
    private RemoteViews createRemoteViews(Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.adbwifi_widget);
    }

    private ComponentName createComponentName(Context context) {
        return new ComponentName(context, ADBWifiWidget.class);
    }

    private PendingIntent createPendingSelfIntent(Context context) {
        Intent intent = new Intent(context.getApplicationContext(), getClass());
        intent.setAction(USER_CLICKED);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    //endregion

    private void switchState(Context context, boolean userClicked) {
        String command = isActive ? SET_PROP_COMMAND_OFF : SET_PROP_COMMAND_ON;

        try {
            RootTools.getShell(true).add(new CommandCapture(0, command));
            RootTools.getShell(true).add(new CommandCapture(0, STOP_ADBD_COMMAND));
            RootTools.getShell(true).add(new CommandCapture(0, START_ADBD_COMMAND));
        } catch (IOException e) {
            MLog.makeLog(TAG, "I/O Exception: " + e.toString());
            return;
        } catch (TimeoutException e) {
            MLog.makeLog(TAG, "Command timeout: " + e.toString());
            return;
        } catch (RootDeniedException e) {
            MLog.makeLog(TAG, "Root denied: " + e.toString());
            return;
        }

        updateState(context, userClicked);
    }

    private void updateState(Context context, boolean userClicked) {
        refreshAdbState(context, userClicked);

        String ip = getIpAddress(context);
        String text;
        int imageResId;

        if (isActive) {
            if(ip != null && !ip.equals(invalidIp)) {
                text = ip;
                imageResId = R.drawable.adb_icon_on;
            } else {
                text = context.getString(R.string.turn_on_wifi);
                imageResId = R.drawable.adb_icon_wifi;
            }
        } else {
            text = context.getString(R.string.disabled);
            imageResId = R.drawable.adb_icon_off;
        }

        updateView(context, text, imageResId);
    }

    private void updateView(Context context, String text, int drawableResId) {
        RemoteViews remoteViews = createRemoteViews(context);
        ComponentName componentName = createComponentName(context);

        remoteViews.setImageViewResource(R.id.widget_image, drawableResId);
        remoteViews.setTextViewText(R.id.widget_text, text);

        //Update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, remoteViews);
    }

    private void refreshAdbState(final Context context, boolean userClicked) {
        String port = getAdbPort();
        //showBoast(context, userClicked + "\n" + "Port: " + port);
        isActive = !(port == null || port.equals("-1"));
        if(userClicked) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    BStyle.Builder style = new BStyle.Builder().setAutoCancel(true).setDuration(BConstants.BDuration.Long);
                    if(isActive) {
                        Boast.makeText(context, "Enabled", style.setBackgroundColor(BConstants.BColor.GreenOk).build());
                    } else {
                        Boast.makeText(context, "Disabled", style.setBackgroundColor(BConstants.BColor.RedAlert).build());
                    }
                }
            });
        }
    }

    private String executeCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);

            InputStream stdout = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdout));
            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            bufferedReader.close();
            stdout.close();

            return stringBuilder.toString();
        } catch (IOException e) {
            MLog.makeLog(TAG, "Failed to execute command " + command + ": " + e.toString());
            return "";
        }
    }

    //region Get IP / ADB
    private String getIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    private String getAdbPort() {
        String output = executeCommand(GET_PROP_COMMAND);
        if (output.isEmpty()) {
            return null;
        } else {
            return output;
        }
    }
    //endregion

    private void closeAllShells() {
        if(RootTools.isAccessGiven()) {
            try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //region Debugging
    private void showBoast(final Context context, final String text) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                Boast.makeText(context, text + "\n\n" + random.nextInt(1000));
            }
        });
    }
    //endregion
}