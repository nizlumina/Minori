package com.nizlumina.minori.android.utility;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nizlumina.minori.R;

/**
 *
 */
public class ScanNotification
{
    //private static final String NOTI_GROUP = "com.nizlumina.robomaru.scannoti";
    private static final int NOTI_CODE = 351497;
    private final int mSize;
    private final NotificationManager ntManager;
    private final NotificationCompat.Builder builder;
    private int internalScanProgress;

    public ScanNotification(Context context, int scanSize, int initialProgress)
    {
        internalScanProgress = initialProgress;
        ntManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(context);
        builder.setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Scanning in progress");
        mSize = scanSize;
    }

    public void updateScan(final int progress)
    {
        internalScanProgress = progress;
        if (progress != mSize)
        {
            builder.setContentText(String.format("Completed %d of %d entries", progress, mSize));
        }
        else
        {
            builder.setContentTitle("Scanning completed");
            builder.setContentText("");
        }
        builder.setProgress(mSize, progress, false);
        ntManager.notify(NOTI_CODE, builder.build());

        if (progress == mSize)
        {

            final Runnable runnable = new Runnable()
            {
                int counter = 3;
                final long delay = 1000;

                @Override
                public void run()
                {
                    while (counter > 0)
                    {
                        try
                        {
                            Thread.sleep(delay);
                        }
                        catch (Exception e)
                        {

                        }
                        builder.setContentText(String.format("Auto-clear in %d seconds", counter));
                        ntManager.notify(NOTI_CODE, builder.build());
                        counter--;

                    }

                    cancel();
                    Log.v("ScanNotification", "AutoCleared!");
                }
            };
            new Thread(runnable).start();
        }
    }

    public void incrementScanProgress()
    {
        updateScan(++internalScanProgress);
    }

    public int getSize()
    {
        return mSize;
    }

    public void cancel()
    {
        ntManager.cancel(NOTI_CODE);
    }

}
