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
    private final WebUnit mFirebaseWebUnit = new WebUnit();

    //For GSON
    private final TypeToken<List<Season>> mSeasonHashListToken = new TypeToken<List<Season>>() {};
    private final TypeToken<List<CompositeData>> mCompositeDataListToken = new TypeToken<List<CompositeData>>() {};

    //For index
    private final Map<String, Season> mSeasonHashIndex = new HashMap<String, Season>(0);

    //For season in view
    private Season mCurrentSeason;
    private List<CompositeData> mCompositeDatas = new ArrayList<CompositeData>();

    //Extras
    private List<Future> tasks = new ArrayList<Future>();

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
        ThreadMaster backgroundThreadMaster = new ThreadMaster();
        processIndex(backgroundThreadMaster);
        setCurrentSeason();
        processSeasonCache(mCurrentSeason);

        return mCompositeDatas;
    }

    //Index is initialized here
    private void processIndex(ThreadMaster threadMaster)
    {
        StringCache indexCache = new StringCache(INDEX_CACHEFILE);

        //Check cached index
        boolean indexStringCacheExist = false;
        if (indexCache.getCache() != null)
        {
            indexCache.loadCache();
            if (indexCache.getCache() != null)
                indexStringCacheExist = true;
        }

        //Validate cached index
        boolean indexValid = false;
        if (indexStringCacheExist)
        {
            indexValid = loadHashMapIndex(indexCache.getCache());
        }

        //Force HTTP GET if not valid or stale
        if (!indexValid || indexCache.isStale(staleThreshold, Calendar.DAY_OF_YEAR))
        {
            String indexJson = null;
            try
            {
                indexJson = mFirebaseWebUnit.getString(INDEX_ENDPOINT);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (indexJson != null) loadHashMapIndex(indexJson);
        }
    }

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

    private void setCurrentSeason()
    {
        String currentSeasonName = SeasonType.getCurrentSeason().name().toLowerCase();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        mCurrentSeason = mSeasonHashIndex.get(Season.makeIndexKey(currentSeasonName, year));
    }

//    private Season getNextSeason()
//    {
//        if (mCurrentSeason != null)
//        {
//            int nextLocation = mSeasonList.indexOf(mCurrentSeason) + 1;
//            if (nextLocation < mSeasonList.size() - 1)
//            {
//                return mSeasonList.get(nextLocation);
//            }
//        }
//        return null;
//    }

    private void processSeasonCache(Season season)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();

        StringCache stringCache = new StringCache(season.getIndexKey());
        stringCache.loadCache();

        List<CompositeData> gsonCompositeDatas = null;
        if (!stringCache.isValid())
        {
            String seasonJSON = null;
            try
            {
                seasonJSON = mFirebaseWebUnit.getString(FIREBASE_ENDPOINT + getRelativeURL(season));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (seasonJSON != null)
            {
                gsonCompositeDatas = gson.fromJson(seasonJSON, mCompositeDataListToken.getType());
            }
        }
        else
        {
            gsonCompositeDatas = gson.fromJson(stringCache.getCache(), mCompositeDataListToken.getType());
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
