package com.moonbloom.adbwifiwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

//https://bitbucket.org/RankoR/adb-over-network/src/

@SuppressWarnings("FieldCanBeLocal")
public class ADBWifiWidget extends AppWidgetProvider {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    public static final String localBroadcastUpdateWifiMsg = "local_broadcast_update_wifi_msg";
    public static final String wifiEnabledBroadcastExtra = "wifi_enabled_broadcast_extra";

    private static final String USER_CLICKED = "userClickedSwitchState";

    private final String invalidIp = "0.0.0.0";

    private final String GET_PROP_COMMAND = "getprop service.adb.tcp.port";
    private final String SET_PROP_COMMAND_ON = "setprop service.adb.tcp.port 5555";
    private final String SET_PROP_COMMAND_OFF = "setprop service.adb.tcp.port -1";
    private final String START_ADBD_COMMAND = "start adbd";
    private final String STOP_ADBD_COMMAND = "stop adbd";

    private static WifiReceiver wifiReceiver;
    private static BroadcastReceiver localBroadcastReceiver;

    private boolean isActive = false;
    //endregion

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        //Register both receivers when the widget is created
        registerWifiBroadcastReceiver(context);
        registerLocalBroadcastReceiver(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        //Unregister both receivers when the widget is removed
        if(wifiReceiver != null) {
            context.getApplicationContext().unregisterReceiver(wifiReceiver);
        }

        if(localBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(context.getApplicationContext()).unregisterReceiver(localBroadcastReceiver);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        //Construct the RemoteViews & ComponentName objects
        RemoteViews remoteViews = createRemoveViews(context);
        ComponentName componentName = createComponentName(context);

        //Check for root, cant do anything without root
        if (!RootTools.isRootAvailable()) {
            //Set views
            remoteViews.setTextViewText(R.id.widget_text, context.getString(R.string.not_rooted));
            remoteViews.setImageViewResource(R.id.widget_image, R.drawable.adb_icon_off);

            //Update widget
            appWidgetManager.updateAppWidget(componentName, remoteViews);
            return;
        }

        //Set click event to call onReceive()
        remoteViews.setOnClickPendingIntent(R.id.widget_parent_relative_layout, getPendingSelfIntent(context));

        //Update internally
        updateState(context, remoteViews);

        //Update widget
        appWidgetManager.updateAppWidget(componentName, remoteViews);

        //Close shells to remove root icon
        closeAllShells();
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        //Construct the RemoteViews & ComponentName objects
        RemoteViews remoteViews = createRemoveViews(context);
        ComponentName componentName = createComponentName(context);

        //Update internally
        updateState(context, remoteViews);

        //If it's a user click, switch the state
        if(intent.getAction().equals(USER_CLICKED)) {
            switchState(context, remoteViews);
        }

        //Update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(componentName, remoteViews);

        //Close shells to remove root icon
        closeAllShells();
    }

    private void registerWifiBroadcastReceiver(Context context) {
        wifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.getApplicationContext().registerReceiver(wifiReceiver, intentFilter);
    }

    private void registerLocalBroadcastReceiver(Context context) {
        localBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Not using it at the moment, but might need it later
                boolean wifiEnabled = intent.getBooleanExtra(wifiEnabledBroadcastExtra, false);

                //Construct the RemoteViews & ComponentName objects
                RemoteViews remoteViews = createRemoveViews(context);
                ComponentName componentName = createComponentName(context);

                //Update internally
                updateState(context, remoteViews);

                //Update widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(componentName, remoteViews);
            }
        };

        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(localBroadcastReceiver, new IntentFilter(localBroadcastUpdateWifiMsg));
    }

    private RemoteViews createRemoveViews(Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.adbwifi_widget);
    }

    private ComponentName createComponentName(Context context) {
        return new ComponentName(context, ADBWifiWidget.class);
    }

    private PendingIntent getPendingSelfIntent(Context context) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(USER_CLICKED);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void switchState(Context context, RemoteViews remoteViews) {
        String command = isActive ? SET_PROP_COMMAND_OFF : SET_PROP_COMMAND_ON;

        try {
            RootTools.getShell(true).add(new CommandCapture(0, command));
            RootTools.getShell(true).add(new CommandCapture(0, STOP_ADBD_COMMAND));
            RootTools.getShell(true).add(new CommandCapture(0, START_ADBD_COMMAND));
        } catch (IOException e) {
            Log.e(TAG, "I/O Exception: " + e.toString());
            return;
        } catch (TimeoutException e) {
            Log.e(TAG, "Command timeout: " + e.toString());
            return;
        } catch (RootDeniedException e) {
            Log.e(TAG, "Root denied: " + e.toString());
            return;
        }

        updateState(context, remoteViews);
    }

    private void updateState(Context context, RemoteViews remoteViews) {
        refreshAdbState();

        String ip = getIpAddress(context);
        String text;
        int drawable;

        if (isActive) {
            if(ip != null && !ip.equals(invalidIp)) {
                text = ip;
                drawable = R.drawable.adb_icon_on;
            } else {
                text = context.getString(R.string.turn_on_wifi);
                drawable = R.drawable.adb_icon_wifi;
            }
        } else {
            text = context.getString(R.string.disabled);
            drawable = R.drawable.adb_icon_off;
        }

        remoteViews.setImageViewResource(R.id.widget_image, drawable);
        remoteViews.setTextViewText(R.id.widget_text, text);
    }

    private void refreshAdbState() {
        String port = getAdbPort();
        isActive = !(port == null || port.equals("-1"));
    }

    private String getAdbPort() {
        String output = execCommand(GET_PROP_COMMAND);
        if (output.isEmpty()) {
            return null;
        } else {
            return output;
        }
    }

    private String execCommand(String command) {
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
            Log.e(TAG, "Failed to exec command " + command + ": " + e.toString());
            return "";
        }
    }

    private String getIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    private void closeAllShells() {
        if(RootTools.isAccessGiven()) {
            try {
                RootTools.closeAllShells();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}