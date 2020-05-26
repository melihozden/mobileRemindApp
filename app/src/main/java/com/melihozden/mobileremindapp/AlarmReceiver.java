package com.melihozden.mobileremindapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.melihozden.mobileremindapp.ui.home.Alert;

public class AlarmReceiver extends BroadcastReceiver {


    private String CHANNEL_ID = "channel_id" ;
    private int NOTIFICATION_ID = 1 ;
    private String CHANNEL_NAME = "Notification name";
    private int NOTIFICATION_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT ;



    @Override
    public void onReceive(Context context, Intent intent) {



        //Toast.makeText(context,"Alarm Çalıyor...",Toast.LENGTH_LONG).show();

/*
        if(alarmUri == null){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ;
        }

        Ringtone ringtone = RingtoneManager.getRingtone(context,alarmUri);
        ringtone.play();

        */
        showNotification(context);

    }

    public void showNotification(Context context){
        long vibrate ;

        SharedPreferences sharedPreferences = context.getSharedPreferences("App Pref", Context.MODE_PRIVATE);
        String isVibrationOn = sharedPreferences.getString("vibrationMode","0");

        Uri alarmUri;

        String ringTone = sharedPreferences.getString("ringType","TYPE_NOTIFICATION");

        if(ringTone.equals("TYPE_NOTIFICATION")){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        else if(ringTone.equals("TYPE_ALARM")){
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        }
        else{
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NOTIFICATION_IMPORTANCE);
            notificationChannel.setDescription("This is notification channel");

            // register it
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(NotificationManager.class);
            assert  notificationManager != null ;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent myIntent = new Intent(context,MainActivity.class);
        myIntent.putExtra("action","turnoff");
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        @SuppressLint("WrongConstant") PendingIntent pendingIntent =
                PendingIntent.getActivity(context,0,myIntent,Intent.FLAG_ACTIVITY_NEW_TASK);

        if(isVibrationOn.equals("true")){
            vibrate = 500 ; // 500 milisecond vibrate
        }
        else{
            vibrate = 0;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.blacktrans2x)
                .setContentTitle("Bir Hatırlatma Var")
                .setPriority(NotificationCompat.FLAG_ONGOING_EVENT)
                .setSound(alarmUri)
                .setAutoCancel(true)
                .setVibrate(new long[] {vibrate})
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());

    }
}
