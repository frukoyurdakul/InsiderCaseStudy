package com.insider.webviewstar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.insider.webviewstar.manifest.StarsCleanerBroadcastReceiver;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The responsible class for sending or canceling existing star count
 * notifications after the app goes to background, or goes to foreground.
 * Triggered via {@link WebViewStarSDK#onBackground()} or
 * {@link WebViewStarSDK#onForeground()}.
 *
 * Methods are defined as static, as the context will be provided,
 * however the initialize method can also be called via an {@link android.app.Application}
 * object, which can be held in a static instance to prevent leaks, and
 * the other methods can be called using the provided application.
 *
 * The notification will be removed using the {@link #cancelNotification(Context)} method
 * and the notification (which will also contain an action to delete existing stars
 * from the disk and RAM) can be posted using {@link #postNotification(Context, int)}.
 *
 * The post notification method gets triggered after 5 seconds when the app
 * goes to background, using the {@link StarsNotificationWorker}.
 */
class StarsNotificationPoster {

    private static final String TAG = StarsNotificationPoster.class.getSimpleName();
    private static final String CHANNEL_ID = BuildConfig.LIBRARY_PACKAGE_NAME + ".StarsNotification";
    private static final int NOTIFICATION_ID = 123123;
    private static final int PENDING_INTENT_CODE = 123124;

    private static final AtomicBoolean isInitialized = new AtomicBoolean(false);

    /**
     * Initializes the notification poster. Uses an atomic boolean to
     * keep its state, so calling this function multiple times is a no-op.
     *
     * @param context is any context.
     */
    static void initialize(Context context) {
        if (isInitialized.compareAndSet(false, true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                final NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "StarsNotificationChannel", NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription("Shows how many stars exist inside the notification after 5 seconds subsequent to app exit.");
                final NotificationManager manager = ContextCompat.getSystemService(context, NotificationManager.class);
                if (manager != null)
                    manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Cancels the existing stars notification, if there is one.
     *
     * @param context is any context.
     */
    static void cancelNotification(Context context) {
        final NotificationManager manager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (manager != null) {
            manager.cancel(NOTIFICATION_ID);
        }
    }

    /**
     * Posts a notification in regards to how many stars are added.
     * If no stars are added, then there will be no action button in the notification.
     *
     * @param context is any context
     * @param starsCount is the current star count added to disk or WebView itself.
     */
    static void postNotification(Context context, int starsCount) {
        final NotificationManager manager = ContextCompat.getSystemService(context,
                NotificationManager.class);
        if (manager != null) {
            final int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    ? (PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT)
                    : PendingIntent.FLAG_CANCEL_CURRENT;
            final Intent deleteStarsIntent = new Intent()
                    .setComponent(new ComponentName(context, StarsCleanerBroadcastReceiver.class));
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    PENDING_INTENT_CODE, deleteStarsIntent, flags);
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.com_insider_webviewstar_notif_icon)
                    .setContentTitle("Stars Notification")
                    .setContentIntent(null)
                    .setOngoing(false)
                    .setOnlyAlertOnce(true);
            if (starsCount > 0) {
                final String contentText = String.format(Locale.ROOT,
                        "There are %d stars at the moment.", starsCount);
                builder.addAction(new NotificationCompat.Action.Builder(null,
                                "Delete Stars", pendingIntent).build())
                        .setContentText(contentText);
            } else {
                builder.setContentText("No stars are added at the moment.");
            }
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}