package com.insider.webviewstar;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * The worker that cleans the stars from the cache, or from the existing
 * attached WebView, whichever is available.
 *
 * If the WebView is currently available, the worker will clean
 * the stars inside the WebView JavaScript, allowing the Javascript
 * Interface at {@link StarsManager} to be called, which will
 * clean the shared preferences accordingly.
 */
public class StarsCleanerWorker extends Worker {

    private static final String TAG = StarsCleanerWorker.class.getSimpleName();

    public StarsCleanerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Creates a worker that will be executed at the first available time
     * (usually instantly.)
     *
     * @param context is any context, preferably application context.
     */
    public static void createWorker(Context context) {
        try {
            final WorkManager manager = WorkManager.getInstance(context);
            manager.enqueue(new OneTimeWorkRequest.Builder(StarsCleanerWorker.class)
                    .build());
        } catch (Exception e) {
            Log.e(TAG, "createWorker: Failed to create worker.", e);
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get the current SDK. If the WebView is not attached
        // to window at that moment, then this SDK will be null or
        // destroyed.
        final WebViewStarSDK sdk = WebViewStarSDK.getInstance();
        if (sdk != null && !sdk.isDestroyed()) {
            // Reset the current WebView state (also shared preferences)
            sdk.reset();
        }
        else {
            // Clear shared preferences data.
            StarsDataManager.getInstance(getApplicationContext()).clearData();
        }

        // Delete the notification to prevent multiple clicks.
        StarsNotificationPoster.cancelNotification(getApplicationContext());

        // Return success, always, regardless of failure.
        return Result.success();
    }
}