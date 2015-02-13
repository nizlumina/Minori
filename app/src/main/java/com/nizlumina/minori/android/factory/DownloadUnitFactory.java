package com.nizlumina.minori.android.factory;

import com.nizlumina.minori.android.model.WatchData;
import com.nizlumina.minori.android.network.DownloadUnit;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;

/**
 * Simple factory for DownloadUnit
 */
public class DownloadUnitFactory
{
    public static DownloadUnit fromWatchData(WatchData watchData)
    {
        return fromNyaaEntry(watchData.getNyaaEntry());
    }

    public static DownloadUnit fromNyaaEntry(NyaaEntry nyaaEntry)
    {
        String outputFileName = String.format("[%s] %s - %d", nyaaEntry.getFansub(), nyaaEntry.getTitle(), nyaaEntry.getCurrentEpisode());
        String notificationTitle = String.format("[%s] %s - %d", nyaaEntry.getFansub(), nyaaEntry.getTitle(), nyaaEntry.getCurrentEpisode());
        return new DownloadUnit(nyaaEntry.getId(), nyaaEntry.getTorrentLink(), outputFileName, notificationTitle);
    }
}
