package com.nizlumina.minori.internal.factory;

import android.content.Context;
import android.content.Intent;

import com.nizlumina.minori.internal.network.DownloadUnit;
import com.nizlumina.minori.model.WatchData;
import com.nizlumina.minori.receiver.DownloadReceiver;
import com.nizlumina.minori.receiver.ScanReceiver;
import com.nizlumina.minori.service.DownloadService;
import com.nizlumina.minori.service.ScanService;

/**
 * Basic IntentFactory for building intents with regards to IPC
 */
public class IntentFactory
{
    public static Intent getScanIntent(Context context, WatchData watchData, boolean manualScan)
    {
        return new Intent(context, ScanReceiver.class).putExtra(ScanService.IKEY_SCAN_INT, watchData.getId()).putExtra(ScanService.IKEY_MANUAL_SCAN_BOOL, manualScan);
    }

    public static Intent getScanIntent(Context context, WatchData watchData, boolean manualScan, int scanSize)
    {
        return new Intent(context, ScanReceiver.class).putExtra(ScanService.IKEY_SCAN_INT, watchData.getId()).putExtra(ScanService.IKEY_MANUAL_SCAN_BOOL, manualScan).putExtra(ScanService.IKEY_OPT_MANUAL_SCAN_SIZE, scanSize);
    }

    public static Intent getDownloadIntent(Context context, DownloadUnit downloadUnit)
    {
        return new Intent(context, DownloadReceiver.class).putExtra(DownloadService.IKEY_DOWNLOAD_UNIT_PARCEL, downloadUnit);
    }

    public static Intent getDownloadIntent(Context context, WatchData watchData)
    {
        return new Intent(context, DownloadReceiver.class).putExtra(DownloadService.IKEY_WATCHDATA_ID, watchData.getId());
    }
}
