package com.nizlumina.minori.android.utility;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.nizlumina.minori.android.network.CoreQuery;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;


/**
 * Simple factory for notification. Do not use this for Downloads yet.
 */
public final class NotificationFactory
{
    private static NotificationFactory mInstance;

    private NotificationFactory() {}
    //private static Bitmap largeIcon = null;

    public static NotificationFactory getInstance()
    {
        if (mInstance == null)
        {
            mInstance = new NotificationFactory();
        }
        return mInstance;
    }

    public void postNotification(final Context context, final NyaaEntry nyaaEntry)
    {
        final NotificationManagerCompat ntManager = (NotificationManagerCompat) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        //if(largeIcon == null) largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_noti);

        Log.v("NotificationFactory", "Notification POSTED for [" + nyaaEntry.id + "]");

        String notificationText = String.format("[%s] %s - %s", nyaaEntry.fansub, nyaaEntry.title, nyaaEntry.episodeString);
        String notificationTitle = String.format("Episode %s released", String.valueOf(nyaaEntry.currentEpisode));
        builder.setContentTitle(notificationTitle)
                //.setLargeIcon(largeIcon)
                .setContentText(notificationText)
                        //.setSmallIcon(R.drawable.ic_stat_noti)
                .setAutoCancel(true)
                .setTicker(notificationText + " is released")
                .setContentIntent(PendingIntent.getActivity(context, nyaaEntry.id, new Intent(Intent.ACTION_VIEW).setData(CoreQuery.Nyaa.viewID(nyaaEntry.id)), PendingIntent.FLAG_UPDATE_CURRENT));

        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        //if (Global.NOTI_SOUND_URI != null) builder.setSound(Global.NOTI_SOUND_URI);
        ntManager.notify(nyaaEntry.id, builder.build());
    }
}
