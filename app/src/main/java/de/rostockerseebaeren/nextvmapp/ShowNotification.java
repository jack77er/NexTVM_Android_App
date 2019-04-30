package de.rostockerseebaeren.nextvmapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;

import java.text.SimpleDateFormat;

public class ShowNotification extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        // Prepare intent which is triggered if the
        // notification is selected
        Bundle extras = intent.getExtras();
        TvmEvent e = (TvmEvent) intent.getParcelableExtra("event");
        if(e == null) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 1000 milliseconds
            v.vibrate(1000);
            return;
        }

        Intent i = new Intent(context, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i, 0);

        SimpleDateFormat sfd = new SimpleDateFormat("HH:mm");

        String text = String.format(context.getString(R.string.notification_event_details), e.mTitle, sfd.format(e.mDate));
        // Build notification
        // Actions are just fake
        //NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,0)
        Notification noti = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.notification_event_title))
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent).build();
        noti.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // hide the notification after its selected

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        v.vibrate(1000);

        notificationManager.notify(0, noti);
    }
}
