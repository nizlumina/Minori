package com.nizlumina.minori.android.controller;

import android.content.Context;

import com.nizlumina.minori.android.alarm.Alarm;
import com.nizlumina.minori.android.data.WatchData;
import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.android.factory.IntentFactory;
import com.nizlumina.minori.android.factory.JSONStorageFactory;
import com.nizlumina.minori.android.factory.WatchDataJSONFactory;
import com.nizlumina.minori.android.internal.WatchlistSingleton;
import com.nizlumina.minori.android.network.NetworkState;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Controls data flow for WatchData.
 * Use this class methods from the main UI thread.
 */
public class WatchlistController
{
    private static final String watchlistFileName = "watchlist.json";

    public synchronized WatchData getWatchData(int id)
    {
        ArrayList<WatchData> watchDatas = WatchlistSingleton.getInstance().getDataList();
        for (WatchData watchData : watchDatas)
        {
            if (watchData.getId() == id)
                return watchData;
        }
        return null;
    }

    public synchronized ArrayList<WatchData> getWatchDataArray()
    {
        return WatchlistSingleton.getInstance().getDataList();
    }

    public void saveData(final Context context)
    {
        final String fileName = watchlistFileName;
        try
        {
            final FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);

            final Thread savingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    JSONStorageFactory.saveJSONArray(WatchDataJSONFactory.getJSONArray(WatchlistSingleton.getInstance().getDataList()), outputStream);
                }
            });

            savingThread.start();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    public synchronized void loadDataAsync(final Context context, Callable onFinish)
    {
        Callable loadTask = new Callable()
        {
            @Override
            public Object call() throws Exception
            {
                forceLoadData(context);
                return null;
            }
        };

        ThreadController.post(loadTask, onFinish);
    }

    //force initialize watchlist data
    public void forceLoadData(final Context context)
    {
        if (WatchlistSingleton.getInstance().getDataList() == null)
        {
            final String fileName = watchlistFileName;
            try
            {
                final FileInputStream inputStream = context.openFileInput(fileName);
                JSONArray jsonArray = JSONStorageFactory.loadJSONArray(inputStream);
                WatchDataJSONFactory.setFromJSONArray(jsonArray, WatchlistSingleton.getInstance().getDataList());
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void addNewWatchData(final Context context, NyaaEntry selectedNyaaEntry, String selectedEpisode, AnimeObject selectedAnimeObject, Alarm.Mode selectedMode)
    {

        selectedNyaaEntry.currentEpisode = Integer.parseInt(selectedEpisode);

        Alarm alarm = new Alarm(selectedNyaaEntry.pubDate, selectedMode, null);

        WatchData watchData = new WatchData(WatchlistSingleton.getInstance().getAvailableID(), selectedNyaaEntry, alarm, selectedAnimeObject);
        WatchlistSingleton.getInstance().getDataList().add(watchData);
        //queue Nyaa scan
        context.sendBroadcast(IntentFactory.getScanIntent(context, watchData, true));
    }

    public void removeWatchData(WatchData watchData)
    {
        WatchlistSingleton.getInstance().getDataList().remove(watchData);
    }

    public void buildSearchResult(final Context context, final String searchTerms, final ArrayList<NyaaEntry> outputList, final Callable onFinish)
    {
        if (NetworkState.networkOK(context))
        {
            Callable task = new Callable()
            {
                @Override
                public Object call() throws Exception
                {
                    CoreNetworkFactory.getNyaaEntries(context, searchTerms, outputList);
                    return null;
                }
            };
            ThreadController.post(task, onFinish);
        }
    }
}
