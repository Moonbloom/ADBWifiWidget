package com.moonbloom.adbwifiwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.moonbloom.boast.BConstants;
import com.moonbloom.boast.BStyle;
import com.moonbloom.boast.Boast;
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

    //public static final String localBroadcastUpdateWifiMsg = "local_broadcast_update_wifi_msg";
    //public static final String wifiEnabledBroadcastExtra = "wifi_enabled_broadcast_extra";

    private static final String USER_CLICKED = "userClickedSwitchState";

    private final String invalidIp = "0.0.0.0";

    private final String GET_PROP_COMMAND = "getprop service.adb.tcp.port";
    private final String SET_PROP_COMMAND_ON = "setprop service.adb.tcp.port 5555";
    private final String SET_PROP_COMMAND_OFF = "setprop service.adb.tcp.port -1";
    private final String START_ADBD_COMMAND = "start adbd";
    private final String STOP_ADBD_COMMAND = "stop adbd";

    public static boolean debug = false;

    private WifiReceiver wifiReceiver;

    private boolean isActive = false;

    static {
        BStyle myStyle = new BStyle.Builder(BStyle.INFO).setAutoCancel(false).setDuration(BConstants.BDuration.Long).build();

        Boast.setDefaultBStyle(myStyle);
    }
    //endregion

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        wifiReceiver = new WifiReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        context.getApplicationContext().registerReceiver(wifiReceiver, intentFilter);

        /*
        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Not using it atm, but might later
                boolean wifiEnabled = intent.getBooleanExtra(wifiEnabledBroadcastExtra, false);

                createBoastAndLog(context, "WiFi state: " + wifiEnabled);

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.adbwifi_widget);
                updateState(context, remoteViews);
            }
        };

        LocalBroadcastManager.getInstance(context.getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter(localBroadcastUpdateWifiMsg));
        */
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        if(wifiReceiver != null) {
            context.getApplicationContext().unregisterReceiver(wifiReceiver);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        createBoastAndLog(context, "onUpdate");

        //Construct the RemoteViews & ComponentName objects
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.adbwifi_widget);
        ComponentName componentName = new ComponentName(context, ADBWifiWidget.class);

        //Check for root, cant do anything without root
        if (!RootTools.isRootAvailable()) {
            String text = context.getString(R.string.not_rooted);

            createBoastAndLog(context, text);

            remoteViews.setTextViewText(R.id.widget_text, text);
            remoteViews.setImageViewResource(R.id.widget_image, R.drawable.android_adb_off);
            return;
        }

        remoteViews.setOnClickPendingIntent(R.id.widget_parent_relative_layout, getPendingSelfIntent(context, USER_CLICKED));

        updateState(context, remoteViews);

        appWidgetManager.updateAppWidget(componentName, remoteViews);

        closeAllShells();
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);

        createBoastAndLog(context, "onReceive");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        //Construct the RemoteViews & ComponentName objects
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.adbwifi_widget);
        ComponentName componentName = new ComponentName(context, ADBWifiWidget.class);

        if(USER_CLICKED.equals(intent.getAction())) {
            refreshAdbState(context);
            switchState(context, remoteViews);
        }

        appWidgetManager.updateAppWidget(componentName, remoteViews);

        closeAllShells();
    }

    private PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void switchState(Context context, RemoteViews remoteViews) {
        String command = isActive ? SET_PROP_COMMAND_OFF : SET_PROP_COMMAND_ON;

        createBoastAndLog(context, "isActive: " + isActive);

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
        refreshAdbState(context);

        if (isActive) {
            String ip = getIpAddress(context);
            //String hint = ip != null ? String.format("%s:%s", ip, getAdbPort()) : "OK!";
            String hint;
            if(ip != null && !ip.equals(invalidIp)) {
                hint = "LOL"; //ip;
                remoteViews.setImageViewResource(R.id.widget_image, R.drawable.android_adb_on);
            } else {
                hint = context.getString(R.string.turn_on_wifi);
                remoteViews.setImageViewResource(R.id.widget_image, R.drawable.android_adb_wifi);
            }

            createBoastAndLog(context, "IP: " + ip + " - Hint: " + hint);

            remoteViews.setTextViewText(R.id.widget_text, hint);
        } else {
            remoteViews.setTextViewText(R.id.widget_text, context.getString(R.string.disabled));
            remoteViews.setImageViewResource(R.id.widget_image, R.drawable.android_adb_off);
        }
    }

    private void refreshAdbState(Context context) {
        String port = getAdbPort();
        isActive = !(port == null || port.equals("-1"));

        createBoastAndLog(context, "Port: " + port + " - Refresh-isActive: " + isActive);
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

    private void createBoastAndLog(Context context, String text) {
        if(debug) {
            Log.d(TAG, text);
            Boast.makeText(context, text);
        }
    }
}