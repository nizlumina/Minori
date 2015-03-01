package com.nizlumina.minori.android.controller;

import android.content.Context;
import android.util.Log;

import com.nizlumina.minori.android.internal.ThreadWorker;
import com.nizlumina.minori.android.internal.WatchlistSingleton;
import com.nizlumina.minori.android.internal.factory.JSONStorageFactory;
import com.nizlumina.minori.android.internal.factory.WatchDataJSONFactory;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.model.WatchData;
import com.nizlumina.minori.android.model.alarm.Alarm;
import com.nizlumina.minori.common.Hummingbird.AnimeObject;
import com.nizlumina.minori.common.Nyaa.NyaaEntry;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
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
        List<WatchData> watchDatas = WatchlistSingleton.getInstance().getDataList();
        for (WatchData watchData : watchDatas)
        {
            if (watchData.getId() == id)
                return watchData;
        }
        return null;
    }

    public synchronized List<WatchData> getWatchDataList()
    {
        return WatchlistSingleton.getInstance().getDataList();
    }

    public synchronized void saveData(final Context context)
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

    public synchronized void loadDataAsync(final Context context, final OnFinishListener<List<WatchData>> onFinishListener)
    {
        //Normal background task.
        Callable<List<WatchData>> backgroundTask = new Callable<List<WatchData>>()
        {
            @Override
            public List<WatchData> call() throws Exception
            {
                forceLoadData(context);
                return null;
            }
        };
        new ThreadWorker<List<WatchData>>().postAsyncTask(backgroundTask, new OnFinishListener<List<WatchData>>()
        {

            @Override
            public void onFinish(List<WatchData> result)
            {
                if (onFinishListener != null)
                    onFinishListener.onFinish(WatchlistSingleton.getInstance().getDataList());
            }
        });
    }

    //force initialize watchlist data
    public synchronized void forceLoadData(final Context context)
    {
        try
        {
            final FileInputStream inputStream = context.openFileInput(watchlistFileName);
            JSONArray jsonArray = JSONStorageFactory.loadJSONArray(inputStream);
            WatchlistSingleton.getInstance().setDataList(WatchDataJSONFactory.fromJSONArray(jsonArray));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public synchronized void addNewWatchData(final Context context, NyaaEntry selectedNyaaEntry, String selectedEpisode, AnimeObject selectedAnimeObject, Alarm.Mode selectedMode)
    {
        boolean debug = true;
        if (debug)
        {
            String debugString = String.format(
                    "NyaaEntry: \n %s \n\n Eps: %s \n\n AnimeObject %s \n\n Mode: %s",
                    selectedNyaaEntry.stringData(),
                    selectedEpisode,
                    selectedAnimeObject.stringData(),
                    selectedMode.name());
            Log.v(getClass().getSimpleName(), debugString);
        }
        selectedNyaaEntry.setCurrentEpisode(Integer.parseInt(selectedEpisode)); //sneakily set it as current episode.

        Alarm alarm = new Alarm(selectedNyaaEntry.getPubDate(), selectedMode, null);

        WatchData watchData = new WatchData(WatchlistSingleton.getInstance().getAvailableID(), selectedNyaaEntry, alarm, selectedAnimeObject);
        WatchlistSingleton.getInstance().getDataList().add(watchData);
        //Todo: Uncomment after finalizing
        //queue Nyaa scan
        //context.sendBroadcast(IntentFactory.getScanIntent(context, watchData, true));
    }

    public synchronized void removeWatchData(WatchData watchData)
    {
        WatchlistSingleton.getInstance().getDataList().remove(watchData);
    }
}
