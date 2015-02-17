/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Unless specified, permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.android.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nizlumina.minori.android.internal.FirebaseConfig;
import com.nizlumina.minori.android.internal.StringCache;
import com.nizlumina.minori.android.internal.ThreadWorker;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.network.WebUnit;
import com.nizlumina.minori.android.utility.Loggy;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.Season;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

//A simple controller for interacting with season data from Syncmaru + Firebase backend
public class SeasonDataController
{
    private final WebUnit mFirebaseSeasonWebUnit = new WebUnit();
    //For GSON

    private final TypeToken<List<CompositeData>> mCompositeDataListToken = new TypeToken<List<CompositeData>>() {};
    //For index

    //Test timers
    Loggy loggySmall = new Loggy();
    Loggy loggyMain = new Loggy();
    //For season in view
    private Season mCurrentSeason;
    private List<CompositeData> mCompositeDatas = new ArrayList<CompositeData>();
    //Extras
    private List<Future> tasks = new ArrayList<Future>();

    public SeasonDataController() {}

    public SeasonDataController(Season season)
    {
        mCurrentSeason = season;
    }

    //The main method from UI
    public void getCompositeDatas(final OnFinishListener<List<CompositeData>> onFinishListener, final boolean userManualRefresh)
    {
        Callable<List<CompositeData>> backgroundThread = new Callable<List<CompositeData>>()
        {
            @Override
            public List<CompositeData> call() throws Exception
            {
                loggyMain.logTimedStart("MainTask start");

                processSeasonCache(mCurrentSeason, userManualRefresh);

                loggyMain.logTimed("MainTask End");
                return mCompositeDatas;
            }
        };

        new ThreadWorker<List<CompositeData>>().postAsyncTask(backgroundThread, onFinishListener);
    }

    //Index is initialized here




    public void cancelRunningTask()
    {
        for (Future task : tasks)
            task.cancel(true);
    }


    //Process requested season
    private void processSeasonCache(Season season, boolean forceWriteCache)
    {
        loggySmall.logTimedStart("PROCESS: Season Cache");
        Gson gson = new GsonBuilder().serializeNulls().create();

        final StringCache seasonDataCache = new StringCache(season.getIndexKey());

        seasonDataCache.loadCache();
        loggySmall.logTimed("Season cache load ends");

        List<CompositeData> gsonCompositeDatas = null;
        if (!seasonDataCache.isValid())
        {
            String seasonJSON = null;
            try
            {
                loggySmall.logTimed("No cache for season. Firebase season GET started");
                seasonJSON = mFirebaseSeasonWebUnit.getString(FirebaseConfig.FIREBASE_ENDPOINT + getRelativeURL(season));
                seasonDataCache.setCache(seasonJSON);
                loggySmall.logTimed("Season data obtained from firebase");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (seasonJSON != null)
            {
                gsonCompositeDatas = gson.fromJson(seasonJSON, mCompositeDataListToken.getType());
                loggySmall.logTimed("GSON - Read season JSON from firebase");
            }
        }
        else
        {
            gsonCompositeDatas = gson.fromJson(seasonDataCache.getCache(), mCompositeDataListToken.getType());
            loggySmall.logTimed("Cache found. Gson read season JSON from cache");
        }

        //Finally set
        if (gsonCompositeDatas != null)
        {
            if (mCompositeDatas.size() > 0) mCompositeDatas.clear();
            mCompositeDatas.addAll(gsonCompositeDatas);
        }

        //Initiate save
        if (seasonDataCache.getWriteFlag(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit, forceWriteCache))
        {
            new ThreadWorker<Void>().postAsyncTask(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    seasonDataCache.saveCache();
                    return null;
                }
            }, null);
        }
    }

    private String getRelativeURL(Season input)
    {
        return input.getSeason() + "/" + input.getYear() + ".json";
    }
}
