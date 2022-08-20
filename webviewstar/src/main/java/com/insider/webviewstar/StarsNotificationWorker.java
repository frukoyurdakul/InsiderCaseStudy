package com.insider.webviewstar;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

/**
 * The worker class that will post a notification after
 * 5 seconds when the app goes to background.
 *
 * Simply call {@link #createWorker(Context)} to start the trigger,
 * and as long as {@link #cancelWorker(Context)} was not called between 5 seconds,
 * the worker will work and post the notification using
 * {@link StarsNotificationPoster#postNotification(Context, int)}.
 */
public class StarsNotificationWorker extends Worker {

    private static final String TAG = StarsNotificationWorker.class.getSimpleName();
    private static final String WORK_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".NotificationWorker";

    public StarsNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Creates the worker which will have a 5 second initial delay to be
     * executed. If there was an existing worker, it will be replaced.
     *
     * @param context is any context.
     */
    public static void createWorker(Context context) {
        try {
            final WorkManager manager = WorkManager.getInstance(context);
            final OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(StarsNotificationWorker.class)
                    .setInitialDelay(5, TimeUnit.SECONDS)
                    .build();
            manager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request);
        } catch (Exception e) {
            Log.e(TAG, "createWorker: Failed to create worker.", e);
        }
    }

    /**
     * Cancels the existing worker, if there is any, preventing
     * posting a notification about how many stars there are.
     *
     * @param context is any context.
     */
    public static void cancelWorker(Context context) {
        try {
            final WorkManager manager = WorkManager.getInstance(context);
            manager.cancelUniqueWork(WORK_NAME);
        } catch (Exception e) {
            Log.e(TAG, "cancelWorker: Failed to cancel work.", e);
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        // Read the star count using the data manager singleton.
        final int starCount = StarsDataManager.getInstance(getApplicationContext()).getStarCount();

        // Post the notification.
        StarsNotificationPoster.postNotification(getApplicationContext(), starCount);

        // Return success.
        return Result.success();
    }
}
