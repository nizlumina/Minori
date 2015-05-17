package com.nizlumina.minori.utility;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.minori.internal.network.CoreQuery;


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
        final NotificationManager ntManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        //if(largeIcon == null) largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_stat_noti);

        Log.v("NotificationFactory", "Notification POSTED for [" + nyaaEntry.getId() + "]");

        String notificationText = String.format("[%s] %s - %s", nyaaEntry.getFansub(), nyaaEntry.getTitle(), nyaaEntry.getEpisodeString());
        String notificationTitle = String.format("Episode %s released", String.valueOf(nyaaEntry.getCurrentEpisode()));
        builder.setContentTitle(notificationTitle)
                //.setLargeIcon(largeIcon)
                .setContentText(notificationText)
                        //.setSmallIcon(R.drawable.ic_stat_noti)
                .setAutoCancel(true)
                .setTicker(notificationText + " is released")
                .setContentIntent(PendingIntent.getActivity(context, nyaaEntry.getId(), new Intent(Intent.ACTION_VIEW).setData(CoreQuery.Nyaa.viewID(nyaaEntry.getId())), PendingIntent.FLAG_UPDATE_CURRENT));

        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        //if (Global.NOTI_SOUND_URI != null) builder.setSound(Global.NOTI_SOUND_URI);
        ntManager.notify(nyaaEntry.getId(), builder.build());
    }
}
