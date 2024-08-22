package com.example.inventoryapp_coreysampson;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    // Channel ID and Name for notification
    private static final String CHANNEL_ID = "inventory_notifications";
    private static final String CHANNEL_NAME = "Inventory Notifications";

    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        // Initialize NotificationManager
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel if Android version is Oreo or higher (left in case API levels below 34 are used to run the app)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Send notification with given title, message, and id
     *
     * @param context           Context to use for the notification.
     * @param title             The title of the notification.
     * @param message           The message content of the notification.
     * @param notificationId    A unique id for the notification.
     */
    public void sendNotification(Context context, String title, String message, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher) // Icon for the notification
                .setAutoCancel(true)    // Automatically remove notification when tapped
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);  // Default priority

        // Build and send the notification
        notificationManager.notify(notificationId, builder.build());
    }
}