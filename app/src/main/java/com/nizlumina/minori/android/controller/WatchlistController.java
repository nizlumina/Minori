package com.nizlumina.minori.android.controller;

import android.content.Context;
import android.os.Handler;

import com.nizlumina.minori.android.WatchlistSingleton;
import com.nizlumina.minori.android.data.WatchData;
import com.nizlumina.minori.android.data.alarma.Alarm;
import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.android.factory.IntentFactory;
import com.nizlumina.minori.android.factory.WatchDataJSONFactory;
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
                    final JSONStorageController controller = new JSONStorageController();
                    controller.saveJSONArray(WatchDataJSONFactory.getJSONArray(WatchlistSingleton.getInstance().getDataList()), outputStream);
                }
            });

            savingThread.start();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    public void loadData(final Context context, Handler handler)
    {
        final String fileName = watchlistFileName;
        try
        {
            final FileInputStream inputStream = context.openFileInput(fileName);

            Thread loadingThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    final JSONStorageController controller = new JSONStorageController();
                    JSONArray jsonArray = controller.loadJSONArray(inputStream);
                    WatchDataJSONFactory.setFromJSONArray(jsonArray, WatchlistSingleton.getInstance().getDataList());
                }
            });

            loadingThread.start();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
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
                final JSONStorageController controller = new JSONStorageController();
                JSONArray jsonArray = controller.loadJSONArray(inputStream);
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
                    CoreNetworkFactory.getNyaaEntries(searchTerms, outputList, null);
                    return null;
                }
            };
            ThreadController.postman(task, onFinish);
        }
    }
}
