package com.alextns.chatapp_master;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * @author alexandru.tanasescu on 09/05/2018.
 */
public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationTitle = remoteMessage.getNotification().getTitle();
        String notificationBody = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String user_id = remoteMessage.getData().get("user_id");


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default_channelID")
                .setSmallIcon(R.drawable.thotchaticon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true);


        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",user_id);
        resultIntent.putExtra("recivedRequestState",2);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationID = (int) System.currentTimeMillis();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationID,mBuilder.build());
    }

}
