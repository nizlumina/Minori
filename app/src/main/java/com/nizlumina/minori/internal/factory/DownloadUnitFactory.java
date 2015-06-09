package com.nizlumina.minori.internal.factory;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.minori.internal.network.DownloadUnit;

/**
 * Simple factory for DownloadUnit
 */
public class DownloadUnitFactory
{
//    public static DownloadUnit fromWatchData(WatchData watchData)
//    {
//        return fromNyaaEntry(watchData.getNyaaEntry());
//    }

    public static DownloadUnit fromNyaaEntry(NyaaEntry nyaaEntry)
    {
        String outputFileName = String.format("[%s] %s - %d", nyaaEntry.getFansub(), nyaaEntry.getTitle(), nyaaEntry.getCurrentEpisode());
        String notificationTitle = String.format("[%s] %s - %d", nyaaEntry.getFansub(), nyaaEntry.getTitle(), nyaaEntry.getCurrentEpisode());
        return new DownloadUnit(nyaaEntry.getId(), nyaaEntry.getTorrentLink(), outputFileName, notificationTitle);
    }
}
