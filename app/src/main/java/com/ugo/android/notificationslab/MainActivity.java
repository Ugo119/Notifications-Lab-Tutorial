package com.ugo.android.notificationslab;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button notifyButton;
    private Button cancelButton;
    private Button updateButton;
    private NotificationReceiver notificationReceiver = new NotificationReceiver();

    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager notificationManager;

    // Notification ID.
    private static final int NOTIFICATION_ID = 0;

    // Constants for the notification actions buttons.
    private static final String ACTION_UPDATE_NOTIFICATION =
            "com.ugo.android.notificationslab.ACTION_UPDATE_NOTIFICATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the notification channel.
        createNotificationChannel();

        // Register the broadcast receiver to receive the update action from
        // the notification.
        registerReceiver(notificationReceiver,new IntentFilter(ACTION_UPDATE_NOTIFICATION));
        notifyButton = findViewById(R.id.notify);
        updateButton = findViewById(R.id.update);
        cancelButton = findViewById(R.id.cancel);
        notifyButton.setOnClickListener(v -> sendNotification());
        updateButton.setOnClickListener(v -> updateNotification());
        cancelButton.setOnClickListener(v -> cancelNotification());

        // Reset the button states. Enable only Notify button and disable
        // update and cancel buttons.
        setNotificationButtonState(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(notificationReceiver);
        super.onDestroy();
    }

    /**
     * Define a custom method called createNotificationChannel. In the method, instantiate
     * NotificationManager object. Then confirm that the device is API level 26 or higher (since
     * NotificationChannel is available in API level 26 and higher). If it is, declare and
     * initialize a notification channel while assigning the channel's attributes.
     * Then invoke createNotificationChannel() method on the NotificationManager object, passing in
     * the NotificationChannel object in order to create the NotificationChannel.
     */
    public void createNotificationChannel() {
        //Create a NotificationManager which the Android system uses to deliver notifications to user
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    getString(R.string.notification_settings_name), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(Boolean.TRUE);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(Boolean.TRUE);
            notificationChannel.setDescription(getString(R.string.notification_settings_description));
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * OnClick method for the "Notify Me!" button.
     * Creates and delivers a simple notification.
     */
    public void sendNotification() {
        // Sets up the pending intent to update the notification.
        // Corresponds to a press of the Update Me! button.
        Intent updateIntent = new Intent(ACTION_UPDATE_NOTIFICATION);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(this,
                NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT);

        // Build the notification with all of the parameters using helper
        // method.
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        // Add the action button using the pending intent.
        notifyBuilder.addAction(R.drawable.ic_update, "Update Notification", updatePendingIntent);

        // Deliver the notification.
        notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());

        // Enable the update and cancel buttons but disables the "Notify
        // Me!" button.
        setNotificationButtonState(Boolean.FALSE, Boolean.TRUE, Boolean.TRUE);
    }

    /**
     * Define a custom method called getNotificationBuilder which is used to create and return a
     * NotificationCompat.Builder. The object is used to create the notification that displays in the
     * notification area. While creating the builder, you set the smallIcon which is mandatory as well
     * as contentText and contentTitle.
     * Inside the method, we also create an Intent object which will be used to explicitly launch
     * MainActivity when the notification is tapped. This Intent is wrapped in a PendingIntent
     * @return
     */
    private NotificationCompat.Builder getNotificationBuilder() {
        // Set up the pending intent that is delivered when the notification
        // is clicked.
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the notification with all of the parameters.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_display_title))
                .setContentText(getString(R.string.notification_display_text))
                .setSmallIcon(R.drawable.ic_android_foreground)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(Boolean.TRUE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        return notifyBuilder;
    }

    public void updateNotification() {
        // Load the drawable resource into the a bitmap image.
        Bitmap androidImage = BitmapFactory.decodeResource(getResources(), R.drawable.mascot_1);

        // Build the notification with all of the parameters using helper
        // method.
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();

        // Update the notification style to BigPictureStyle.
        notifyBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                       .bigPicture(androidImage)
                       .setBigContentTitle("Notification Updated!"));

        // Deliver the notification.
        notificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());

        // Disable the update button, leaving only the cancel button enabled.
        setNotificationButtonState(Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
    }

    public void cancelNotification() {
        // Cancel the notification.
        notificationManager.cancel(NOTIFICATION_ID);

        // Reset the buttons.
        setNotificationButtonState(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
    }

    public void setNotificationButtonState(Boolean isNotifyEnabled, Boolean isUpdateEnabled,
                                           Boolean isCancelEnabled) {
        notifyButton.setEnabled(isNotifyEnabled);
        updateButton.setEnabled(isUpdateEnabled);
        cancelButton.setEnabled(isCancelEnabled);
    }

    /**
     * The broadcast receiver class for notifications.
     * Responds to the update notification pending intent action.
     */
    public class NotificationReceiver extends BroadcastReceiver {
        public NotificationReceiver() {

        }

        /**
         * Receives the incoming broadcasts and responds accordingly.
         *
         * @param context Context of the app when the broadcast is received.
         * @param intent The broadcast intent containing the action.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the notification.
            updateNotification();

        }
    }
}