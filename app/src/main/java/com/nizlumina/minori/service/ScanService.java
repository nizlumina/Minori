package com.nizlumina.minori.service;

import android.app.IntentService;
import android.content.Intent;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.minori.MinoriApplication;
import com.nizlumina.minori.controller.WatchlistController;
import com.nizlumina.minori.internal.factory.CoreNetworkFactory;
import com.nizlumina.minori.internal.factory.IntentFactory;
import com.nizlumina.minori.internal.network.DownloadUnit;
import com.nizlumina.minori.model.WatchData;
import com.nizlumina.minori.receiver.ScanReceiver;
import com.nizlumina.minori.utility.ScanNotification;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ScanService extends IntentService
{

    public static final String IKEY_SCAN_INT = "com.nizlumina.minori.ScanAction";
    public static final String IKEY_MANUAL_SCAN_BOOL = "com.nizlumina.minori.ScanProtection";
    public static final String IKEY_OPT_MANUAL_SCAN_SIZE = "com.nizlumina.minori.ScanSize";
    private ScanNotification notification;

    public ScanService()
    {
        super("ScanService");
    }

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
                WatchlistController controller = new WatchlistController();
                controller.forceLoadData(getApplicationContext());

                WatchData watchData = controller.getWatchData(id);
                NyaaEntry original = watchData.getNyaaEntry();

                ArrayList<NyaaEntry> scanResults = new ArrayList<>();
                CoreNetworkFactory.getNyaaEntries(watchData.getNyaaEntry().buildQuery(), scanResults);

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
                    watchData.updateNyaaEntry(getApplicationContext(), latestEntry);
                }
                else
                {
                    boolean manualScan = intent.getBooleanExtra(IKEY_MANUAL_SCAN_BOOL, false);
                    if (manualScan)
                    {
                        //this is forcefully called on manual scan due to some weird bug where alarm sometimes stuck in the past even after getNextDate is called.
                        watchData.getAlarm().raiseProtectAlarmCountFlag();
                    }

                    watchData.updateAlarm(getApplicationContext());
                }

                if (!MinoriApplication.listenersAvailable())
                {
                    controller.saveData(getApplicationContext());
                }
            }

            if (notification != null)
            {
                notification.incrementScanProgress();
            }
        }
        ScanReceiver.completeWakefulIntent(intent);
    }
}
