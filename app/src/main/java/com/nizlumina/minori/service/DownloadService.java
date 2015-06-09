package com.nizlumina.minori.service;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.nizlumina.minori.MinoriApplication;
import com.nizlumina.minori.internal.network.DownloadUnit;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DownloadService extends IntentService
{
    public static final String IKEY_WATCHDATA_ID = "com.nizlumina.minori.DownloadAction";
    public static final String IKEY_DOWNLOAD_UNIT_PARCEL = "com.nizlumina.minori.DownloadUnit";

    public DownloadService()
    {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            DownloadUnit downloadUnit = null;
            int id = intent.getIntExtra(IKEY_WATCHDATA_ID, -1);
            if (id == -1)
            {
                downloadUnit = intent.getParcelableExtra(IKEY_DOWNLOAD_UNIT_PARCEL);
            }
            else
            {
//                WatchlistController controller = new WatchlistController();
//                WatchData watchData = controller.getWatchData(id);
//                downloadUnit = DownloadUnitFactory.fromWatchData(watchData);
            }

            if (downloadUnit != null)
            {
                download(downloadUnit);
            }

            ServiceReceiver.completeWakefulIntent(intent);
        }
    }

    private void download(DownloadUnit downloadUnit)
    {
        Uri uri = null;
        try
        {
            uri = Uri.parse(downloadUnit.getUrl());
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        if (uri != null)
        {
            final CharSequence charSequence = null;
            final CharSequence charSequence1 = null;

            final DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setVisibleInDownloadsUi(false)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setTitle(charSequence)
                    .setDescription(charSequence1);

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            final String downloadLocation = preferences.getString(MinoriApplication.Preference.PREF_KEY_DOWNLOAD_LOC, null);

            if (downloadLocation == null)
                request.setDestinationUri(Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)));
            else
                request.setDestinationUri(Uri.parse(downloadLocation));
            int preferredNetwork = preferences.getInt(MinoriApplication.Preference.PREF_KEY_PREFFERED_CONN, -1);

            if (preferredNetwork > 0)
                request.setAllowedNetworkTypes(preferredNetwork);

            final DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            manager.enqueue(request);
        }
    }

    public static class ServiceReceiver extends WakefulBroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            intent.setClass(context, DownloadService.class);
            startWakefulService(context, intent);
        }
    }
}
