package com.moonbloom.adbwifiwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "ADBON/MainActivity";

    private static final String GET_PROP_COMMAND = "getprop service.adb.tcp.port";
    private static final String SET_PROP_COMMAND_ON = "setprop service.adb.tcp.port 5555";
    private static final String SET_PROP_COMMAND_OFF = "setprop service.adb.tcp.port -1";
    private static final String STOP_ADBD_COMMAND = "stop adbd";
    private static final String START_ADBD_COMMAND = "start adbd";

    private boolean mIsActive = false;

    private ImageView mAndroidIv;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mAndroidIv = (ImageView) findViewById(R.id.android_iv);
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
        final TextView hintTv = (TextView) findViewById(R.id.hint_tv);

        if (mIsActive) {
            final String ip = getIpAddress();
            final String hint = ip != null ? String.format("%s:%s", ip, getAdbPort()) : "OK!";
            hintTv.setText(hint);
            mAndroidIv.setColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.MULTIPLY);
        } else {
            hintTv.setText(R.string.android_hint);
            mAndroidIv.setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.MULTIPLY);
        }
    }

    /**
     * Refresh the ADB state
     */
    private void refreshAdbState() {
        final String port = getAdbPort();
        mIsActive = (port != null && !port.equals("-1"));
    }

    /**
     * Get the ADB port
     *
     * @return Port or null
     */
    private static String getAdbPort() {
        final String output = execCommand(GET_PROP_COMMAND);
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
    private static String execCommand(String command) {
        try {
            final Process process = Runtime.getRuntime().exec(command);

            final InputStream stdout = process.getInputStream();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stdout));
            final StringBuilder stringBuilder = new StringBuilder();
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
    public String getIpAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final int ipAddress = wifiInfo.getIpAddress();

        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }
}