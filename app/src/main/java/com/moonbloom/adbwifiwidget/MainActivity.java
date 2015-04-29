package com.moonbloom.adbwifiwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
public class MainActivity extends Activity implements View.OnClickListener {

    //region Variables
    //Debug TAG
    private transient final String TAG = ((Object)this).getClass().getSimpleName();

    private final String GET_PROP_COMMAND = "getprop service.adb.tcp.port";
    private final String SET_PROP_COMMAND_ON = "setprop service.adb.tcp.port 5555";
    private final String SET_PROP_COMMAND_OFF = "setprop service.adb.tcp.port -1";
    private final String STOP_ADBD_COMMAND = "stop adbd";
    private final String START_ADBD_COMMAND = "start adbd";

    private boolean mIsActive = false;
    //endregion

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!RootTools.isRootAvailable()) {
            new AlertDialog.Builder(this).setMessage(R.string.not_rooted).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).show();

            return;
        }

        refreshAdbState();
        updateState();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        refreshAdbState();
        updateState();
    }

    @Override
    public void onClick(View view) {
        switchState();
    }

    /**
     * Switch the state
     */
    private void switchState() {
        final String command = mIsActive ? SET_PROP_COMMAND_OFF : SET_PROP_COMMAND_ON;

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

        refreshAdbState();
        updateState();
    }

    /**
     * Update the state
     */
    private void updateState() {
        TextView hintTv = (TextView) findViewById(R.id.hint_tv);

        if (mIsActive) {
            String ip = getIpAddress();
            //String hint = ip != null ? String.format("%s:%s", ip, getAdbPort()) : "OK!";
            String hint = ip != null ? ip : "IP is null!";
            hintTv.setText(hint);
        } else {
            hintTv.setText(R.string.android_hint);
        }
    }

    /**
     * Refresh the ADB state
     */
    private void refreshAdbState() {
        String port = getAdbPort();
        mIsActive = (port != null && !port.equals("-1"));
    }

    /**
     * Get the ADB port
     *
     * @return Port or null
     */
    private String getAdbPort() {
        String output = execCommand(GET_PROP_COMMAND);
        if (output.isEmpty()) {
            return null;
        } else {
            return output;
        }
    }

    /**
     * Execute the command and get output
     *
     * @param command Command to execute
     * @return Command output
     */
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

    /**
     * Get IP address
     *
     * @return IP Address or null
     */
    private String getIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
}