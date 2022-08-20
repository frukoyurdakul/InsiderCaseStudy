package com.insider.webviewstar.manifest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.insider.webviewstar.StarsCleanerWorker;

/**
 * The broadcast receiver used to call the pending intent with.
 * The notification will be posted via
 * {@link StarsCleanerBroadcastReceiver#postNotification(Context, int)}
 * after the app goes to background and does not get into foreground state for 5 seconds.
 */
public class StarsCleanerBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        StarsCleanerWorker.createWorker(context);
    }
}