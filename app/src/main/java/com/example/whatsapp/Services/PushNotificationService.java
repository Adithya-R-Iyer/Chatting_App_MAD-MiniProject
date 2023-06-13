package com.example.whatsapp.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.whatsapp.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

public class PushNotificationService extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = Objects.requireNonNull(remoteMessage.getNotification()).getTitle();
        String text = remoteMessage.getNotification().getBody();
        String CHANNEL_ID = "MESSAGE";
        CharSequence name;
        NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Message Notification",
                    NotificationManager.IMPORTANCE_HIGH);
//        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Context context;
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                        .setContentText(text)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                        .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(1, notification.build());

        NotificationManagerCompat.from(this).createNotificationChannel(channel);
        NotificationManagerCompat.from(this).notify(1, notification.build());
//        getFirebaseMessage(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("TOKEN","token is " +  token);
        super.onNewToken(token);
    }

    public void getFirebaseMessage(String title, String msg)

    {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"myFirebaseChannel")
                .setSmallIcon(R.drawable.active_notification)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true);


        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(101,builder.build());

    }
}