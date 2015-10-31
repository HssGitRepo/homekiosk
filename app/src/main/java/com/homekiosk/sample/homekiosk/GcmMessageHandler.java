package com.homekiosk.sample.homekiosk;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;


public class GcmMessageHandler extends IntentService {
    static final String MSG_KEY = "m";
    String mes;
    public static final int notifyID = 9001;
    public GcmMessageHandler() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        Bundle extras = intent.getExtras();
        String messageType = gcm.getMessageType(intent);
        mes = intent.getExtras().getString("message");

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                    .equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                    .equals(messageType)) {
                sendNotification("Deleted messages on server: "
                        + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                    .equals(messageType)) {
                sendNotification("Message Received from Google GCM Server:\n\n"
                        + extras.get(MSG_KEY));
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
//        Intent resultIntent = new Intent(this, MainActivity.class);
//        resultIntent.putExtra("msg", mes);
//        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
//                resultIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder mNotifyBuilder;
//        NotificationManager mNotificationManager;
//
//        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        mNotifyBuilder = new NotificationCompat.Builder(this)
//                .setContentTitle("Dr.Owl")
//                .setContentText("New Order")
//                .setSound(alarmSound)
//                .setSmallIcon(R.drawable.add);
//
//        mNotifyBuilder.setContentIntent(resultPendingIntent);
//        mNotifyBuilder.setContentText("New Order available in your place!");
//        mNotifyBuilder.setAutoCancel(true);
//        mNotificationManager.notify(notifyID, mNotifyBuilder.build());
        Log.e("GCM MESSAGE", "" + mes);
        try {
            JSONObject root=new JSONObject(mes);
            String status=root.optString("status");
            if (status.equals("1"))
            {
                String keys=root.optString("keyvalue");
                String expiry=root.optString("expirytime");
                DbHelper helper=new DbHelper(getBaseContext());
                helper.insertContact(keys,expiry);
                Toast.makeText(getApplicationContext(),"Key database updated",Toast.LENGTH_SHORT).show();

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}