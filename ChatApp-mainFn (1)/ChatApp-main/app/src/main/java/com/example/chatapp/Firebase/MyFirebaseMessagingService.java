package com.example.chatapp.Firebase;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.example.chatapp.R;
import com.example.chatapp.View.VideoCallComingActivity;
import com.example.chatapp.View.VoiceCallComingActivity;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String CHANNEL_ID = "MY_CHANNEL_ID";
    NotificationManager mNotificationManager;
    CharSequence name = "ChatApp";


    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);

        String type = message.getNotification().getTitle();
        String body = message.getNotification().getBody();

        if (type.equals("VideoCall")) {
            startVideoCall(body);

        } else if (type.equals("VoiceCall")) {
            startVoiceCall(body);
        }
        else if (type.equals("Chat App")) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            Intent resultIntent = new Intent(this, VideoCallComingActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_IMMUTABLE);

            builder.setContentTitle(message.getNotification().getTitle());
            builder.setContentText(message.getNotification().getBody());
            builder.setContentIntent(pendingIntent);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.getNotification().getBody()));
            builder.setAutoCancel(true);
            builder.setPriority(Notification.PRIORITY_MAX);

            mNotificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        name,
                        NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager.createNotificationChannel(channel);
                builder.setChannelId(CHANNEL_ID);
            }

            mNotificationManager.notify(100, builder.build());
        }
    }

    private void startVideoCall(String body) {

        Intent intent = new Intent(this, VideoCallComingActivity.class);
        intent.putExtra("senderID", body);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void startVoiceCall(String body) {
        Intent intent = new Intent(this, VoiceCallComingActivity.class);
        intent.putExtra("senderID", body);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

}
