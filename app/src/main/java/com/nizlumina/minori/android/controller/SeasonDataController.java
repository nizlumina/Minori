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
import com.nizlumina.minori.android.internal.StringCache;
import com.nizlumina.minori.android.internal.ThreadMaster;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.model.SeasonType;
import com.nizlumina.minori.android.network.WebUnit;
import com.nizlumina.minori.android.utility.Loggy;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.Season;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

//A simple controller for interacting with season data from Syncmaru + Firebase backend
public class SeasonDataController
{
    private static final String FIREBASE_ENDPOINT = "https://minori.firebaseio.com";
    private static final String INDEX_ENDPOINT = FIREBASE_ENDPOINT + "/seasons.json";
    private static final String INDEX_CACHEFILE = "season_index";
    private static final int staleThreshold = 30;
    private static final int staleTimeUnit = Calendar.DAY_OF_YEAR;
    private final WebUnit mFirebaseWebUnit = new WebUnit();
    //For GSON
    private final TypeToken<List<Season>> mSeasonHashListToken = new TypeToken<List<Season>>() {};
    private final TypeToken<List<CompositeData>> mCompositeDataListToken = new TypeToken<List<CompositeData>>() {};
    //For index
    private final Map<String, Season> mSeasonHashIndex = new HashMap<String, Season>(0);
    //Test timers
    Loggy loggySmall = new Loggy();
    Loggy loggyMain = new Loggy();
    //For season in view
    private Season mCurrentSeason;
    private List<CompositeData> mCompositeDatas = new ArrayList<CompositeData>();
    //Extras
    private List<Future> tasks = new ArrayList<Future>();
    private List<StringCache> mCacheList = new ArrayList<StringCache>();

    public SeasonDataController() {}

    //The main method from UI
    public void getCompositeDatas(final OnFinishListener<List<CompositeData>> onFinishListener)
    {
        Callable<List<CompositeData>> backgroundThread = new Callable<List<CompositeData>>()
        {
            @Override
            public List<CompositeData> call() throws Exception
            {
                return processRequest();
            }
        };

        new ThreadMaster().enqueue(backgroundThread, onFinishListener);
    }

    //This is being done in background
    private List<CompositeData> processRequest()
    {
        loggyMain.logTimedStart("MainTask start");

        ThreadMaster backgroundThreadMaster = new ThreadMaster();
        processIndex(backgroundThreadMaster);
        setCurrentSeason();
        processSeasonCache(mCurrentSeason);
        delegateCacheWrite(backgroundThreadMaster);

        loggyMain.logTimed("MainTask End");
        return mCompositeDatas;
    }

    //Delegate save cache task
    private void delegateCacheWrite(ThreadMaster threadMaster)
    {
        threadMaster.enqueue(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                loggySmall.logTimedStart("Saving Cache start");
                for (StringCache stringCache : mCacheList)
                {
                    if (stringCache.isValid())
                    {
                        //if new cache or stale
                        if (stringCache.getLastSavedTime() < 0 || stringCache.isStale(staleThreshold, staleTimeUnit))
                            stringCache.saveCache();
                    }
                }

                loggySmall.logTimed("Saving cache end");
                return null;
            }
        }, new OnFinishListener<Void>()
        {
            @Override
            public void onFinish(Void result)
            {
                mCacheList.clear();
            }
        });
    }

    //Index is initialized here
    private void processIndex(ThreadMaster threadMaster)
    {
        loggySmall.logTimedStart("PROCESS: Index Start");
        StringCache indexCache = new StringCache(INDEX_CACHEFILE);
        mCacheList.add(indexCache);

        //Check intitial cached index
        indexCache.loadCache();
        loggySmall.logTimed("Index cache load ends");

        //Force HTTP GET if not valid or stale
        if (!indexCache.isValid() || indexCache.isStale(staleThreshold, staleTimeUnit))
        {
            String indexJson = null;
            try
            {
                indexJson = mFirebaseWebUnit.getString(INDEX_ENDPOINT);
                indexCache.setCache(indexJson);
                loggySmall.logTimed("Firebase get index");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (indexJson != null)
            {
                loadHashMapIndex(indexJson);
                loggySmall.logTimed("GSON - Load index to mSeasonHashIndex");
            }
        }
        else
        {
            loadHashMapIndex(indexCache.getCache());
        }
    }

    //Load JSON into the a map of the index
    private boolean loadHashMapIndex(String indexJson)
    {
        if (indexJson != null)
        {
            Gson gson = new Gson();
            List<Season> gsonResult = gson.fromJson(indexJson, mSeasonHashListToken.getType());
            if (gsonResult != null)
            {
                if (mSeasonHashIndex.size() > 0) mSeasonHashIndex.clear();
                for (Season season : gsonResult)
                {
                    mSeasonHashIndex.put(season.getIndexKey(), season);
                }
                return true;
            }
        }
        return false;
    }

    public void cancelRunningTask()
    {
        for (Future task : tasks)
            task.cancel(true);
    }


    //This is only for initial fragment
    private void setCurrentSeason()
    {
        String currentSeasonName = SeasonType.getCurrentSeason().name().toLowerCase();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        mCurrentSeason = mSeasonHashIndex.get(Season.makeIndexKey(currentSeasonName, year));
    }


    //Process requested season
    private void processSeasonCache(Season season)
    {
        loggySmall.logTimedStart("PROCESS: Season Cache");
        Gson gson = new GsonBuilder().serializeNulls().create();

        StringCache seasonDataCache = new StringCache(season.getIndexKey());
        mCacheList.add(seasonDataCache);

        seasonDataCache.loadCache();
        loggySmall.logTimed("Season cache load ends");

        List<CompositeData> gsonCompositeDatas = null;
        if (!seasonDataCache.isValid())
        {
            String seasonJSON = null;
            try
            {
                loggySmall.logTimed("No cache for season. Firebase season GET started");
                seasonJSON = mFirebaseWebUnit.getString(FIREBASE_ENDPOINT + getRelativeURL(season));
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
    }

    private String getRelativeURL(Season input)
    {
        return "/anime/" + input.getSeason() + "/" + input.getYear() + ".json";
    }
}
