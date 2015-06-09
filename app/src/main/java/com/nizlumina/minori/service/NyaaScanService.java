package com.nizlumina.minori.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.minori.MinoriApplication;
import com.nizlumina.minori.R;
import com.nizlumina.minori.internal.factory.CoreNetworkFactory;
import com.nizlumina.minori.internal.factory.IntentFactory;
import com.nizlumina.minori.internal.network.DownloadUnit;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NyaaScanService extends IntentService
{

    public static final String IKEY_SCAN_INT = "com.nizlumina.minori.ScanAction";
    public static final String IKEY_OPT_MANUAL_SCAN_SIZE = "com.nizlumina.minori.ScanSize";
    private ScanNotification notification;

    public NyaaScanService()
    {
        super("ScanService");
    }

//    public static Intent makeServiceIntent(final NyaaEntry... entryForScan)
//    {
//        Intent intent = new Intent(MinoriApplication.getAppContext(), NyaaScanService.class);
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
        {
            int scanSize = intent.getIntExtra(IKEY_OPT_MANUAL_SCAN_SIZE, -1);
            if (scanSize != -1)
            {
                notification = new ScanNotification(this, scanSize, 0);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {

        if (intent != null)
        {
            int id = intent.getIntExtra(IKEY_SCAN_INT, -1);

            if (id != -1)
            {
                //LocalBroadcastManager.getInstance(this).sendBroadcast();

                //WatchlistController controller = new WatchlistController();
                //controller.forceLoadData(getApplicationContext());

                //WatchData watchData = controller.getWatchData(id);
                //TODO: RECHECK
                NyaaEntry original = new NyaaEntry(); //watchData.getNyaaEntry();

                ArrayList<NyaaEntry> scanResults = new ArrayList<>();
                CoreNetworkFactory.getNyaaEntries(original.buildQuery(), scanResults);

                NyaaEntry latestEntry = null;
                for (NyaaEntry scanResult : scanResults)
                {
                    if (scanResult.matchOriginalSignature(original))
                    {
                        if (scanResult.getCurrentEpisode() > original.getCurrentEpisode())
                        {
                            String outputFileName = String.format("[%s] %s - %d", scanResult.getFansub(), scanResult.getTitle(), scanResult.getCurrentEpisode());
                            String notificationTitle = String.format("[%s] %s - %d", scanResult.getFansub(), scanResult.getTitle(), scanResult.getCurrentEpisode());
                            DownloadUnit downloadUnit = new DownloadUnit(scanResult.getId(), scanResult.getTorrentLink(), outputFileName, notificationTitle);


                            sendBroadcast(IntentFactory.getDownloadIntent(getApplicationContext(), downloadUnit));

                            if (latestEntry == null)
                            {
                                latestEntry = scanResult;
                                continue;
                            }
                            if (scanResult.getCurrentEpisode() > latestEntry.getCurrentEpisode())
                                latestEntry = scanResult;
                        }
                    }
                }
                if (latestEntry != null)
                {
                    //watchData.updateNyaaEntry(getApplicationContext(), latestEntry);
                }
                else
                {
//                    boolean manualScan = intent.getBooleanExtra(IKEY_MANUAL_SCAN_BOOL, false);
//                    if (manualScan)
//                    {
//                        //this is forcefully called on manual scan due to some weird bug where alarm sometimes stuck in the past even after getNextDate is called.
//
//                        //watchData.getAlarm().raiseProtectAlarmCountFlag();
//                    }

                    //watchData.updateAlarm(getApplicationContext());
                }

                if (!MinoriApplication.listenersAvailable())
                {
                    //controller.saveData(getApplicationContext());
                }
            }

            if (notification != null)
            {
                notification.incrementScanProgress();
            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            ServiceReceiver.completeWakefulIntent(intent);
        }
    }

    private static class ScanNotification
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
                    final long delay = 1000;
                    int counter = 3;

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

    public static class ServiceReceiver extends WakefulBroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            intent.setClass(context, NyaaScanService.class);
            startWakefulService(context, intent);
        }
    }
}
