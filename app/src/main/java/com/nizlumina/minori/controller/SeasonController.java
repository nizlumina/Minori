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

package com.nizlumina.minori.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nizlumina.minori.internal.FirebaseConfig;
import com.nizlumina.minori.internal.ThreadWorker;
import com.nizlumina.minori.internal.network.WebUnit;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.utility.Loggy;
import com.nizlumina.minori.utility.StringCache;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.Season;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

//A simple controller for interacting with season data from Syncmaru + Firebase backend
public class SeasonController
{
    //Test timers
    final Loggy loggySmall;
    //For GSON
    final Loggy loggyMain;
    //For index
    private final WebUnit mFirebaseSeasonWebUnit = new WebUnit();
    private final TypeToken<List<CompositeData>> mCompositeDataListToken = new TypeToken<List<CompositeData>>() {};
    //For season in view
    private Season mCurrentSeason;
    private List<CompositeData> mCompositeDatas = new ArrayList<>();
    //Extras
    private List<Future> tasks = new ArrayList<>();
    public SeasonController(Season season)
    {
        mCurrentSeason = season;
        loggySmall = new Loggy(this.getClass().getSimpleName() + " - " + mCurrentSeason.getIndexKey());
        loggyMain = new Loggy(this.getClass().getSimpleName() + " - " + mCurrentSeason.getIndexKey());
    }

    public Season getSeason()
    {
        return mCurrentSeason;
    }

    //The main method from UI
    public void getCompositeDatas(final OnFinishListener<List<CompositeData>> onFinishListener, final boolean userManualRefresh)
    {
        Callable<List<CompositeData>> backgroundThread = new Callable<List<CompositeData>>()
        {
            @Override
            public List<CompositeData> call() throws Exception
            {
                loggyMain.logStartTime("MainTask start - Season: " + mCurrentSeason.getIndexKey());

                processSeasonCache(mCurrentSeason, userManualRefresh);

                loggyMain.logTimeDelta("MainTask End");
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
        loggySmall.logStartTime("PROCESS: Season Cache");
        Gson gson = new GsonBuilder().serializeNulls().create();

        final StringCache seasonDataCache = new StringCache(season.getIndexKey());

        seasonDataCache.loadCache();
        loggySmall.logTimeDelta("Season cache load ends");

        List<CompositeData> gsonCompositeDatas = null;
        if (!seasonDataCache.isValid())
        {
            String seasonJSON = null;
            try
            {
                loggySmall.logTimeDelta("No cache for season. Firebase season GET started");
                String url = FirebaseConfig.SEASON_ENDPOINT + getRelativeURL(season);
                loggySmall.logTimeDelta(url);
                seasonJSON = mFirebaseSeasonWebUnit.getString(url);
                seasonDataCache.setCache(seasonJSON);
                loggySmall.logTimeDelta("Season data obtained from firebase");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (seasonJSON != null)
            {
                gsonCompositeDatas = gson.fromJson(seasonJSON, mCompositeDataListToken.getType());
                loggySmall.logTimeDelta("GSON - Read season JSON from firebase");
            }
            else loggySmall.logTimeDelta("GSON - Error, season JSON is null");
        }
        else
        {
            gsonCompositeDatas = gson.fromJson(seasonDataCache.getCache(), mCompositeDataListToken.getType());
            loggySmall.logTimeDelta("Cache found. Gson read season JSON from cache");
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
            loggySmall.logTimeDelta("Cache write flag true. Cache will be overwritten.");
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
        return "/" + input.getSeason() + "/" + input.getYear() + ".json";
    }
}
