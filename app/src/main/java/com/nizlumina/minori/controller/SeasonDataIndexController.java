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

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nizlumina.minori.internal.FirebaseConfig;
import com.nizlumina.minori.internal.ThreadWorker;
import com.nizlumina.minori.internal.network.WebUnit;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.model.SeasonType;
import com.nizlumina.minori.utility.Loggy;
import com.nizlumina.minori.utility.StringCache;
import com.nizlumina.syncmaru.model.Season;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class SeasonDataIndexController
{
    private final WebUnit mFirebaseIndexWebUnit = new WebUnit();
    private final Loggy loggy = new Loggy(this.getClass().getSimpleName());
    private final TypeToken<List<Season>> mSeasonHashListToken = new TypeToken<List<Season>>() {};
    private final Map<String, Season> mSeasonHashIndex = new HashMap<>(0);
    private boolean mLoading = false;

    public List<Season> getSeasonList()
    {
        return new ArrayList<>(mSeasonHashIndex.values());
    }

    public Season getCurrentSeason()
    {
        String currentSeasonName = SeasonType.getCurrentSeason().name().toLowerCase();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        return mSeasonHashIndex.get(Season.makeIndexKey(currentSeasonName, year));
    }

    public void loadIndex(@Nullable final OnFinishListener<Void> onFinishListener, final boolean forceRefresh)
    {
        mLoading = true;
        ThreadWorker<Void> soullessWorker = new ThreadWorker<>();
        soullessWorker.postAsyncTask(processIndex(forceRefresh), onFinishListener);
    }

    public boolean indexLoaded()
    {
        return mSeasonHashIndex.size() > 0 && !mLoading;
    }


    private Callable<Void> processIndex(final boolean forceRefresh)
    {
        return new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                loggy.logStartTime("PROCESS: Index Start");
                StringCache indexCache = new StringCache(FirebaseConfig.INDEX_CACHEFILE);

                //Check intitial cached index
                indexCache.loadCache();
                loggy.logTimeDelta("Index initial cache load ends");

                //Force HTTP GET if not valid or stale or forced
                if (!indexCache.isValid() || indexCache.isStale(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit) || forceRefresh)
                {
                    String indexJson = null;
                    try
                    {
                        loggy.logTimeDelta("Firebase get index Starts");
                        String url = FirebaseConfig.INDEX_ENDPOINT;
                        Log.v("GETTING ", "[" + url + "]");
                        indexJson = mFirebaseIndexWebUnit.getString(url);
                        Log.v("Size " + indexJson.length(), indexJson);
                        indexCache.setCache(indexJson);
                        loggy.logTimeDelta("Firebase get index Ends");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    if (indexJson != null)
                    {
                        loadHashMapIndex(indexJson);
                        loggy.logTimeDelta("GSON - Load index to mSeasonHashIndex");
                    }
                    else loggy.logTimeDelta("GSON - Index JSON is null");
                }
                else
                {
                    loggy.logTimeDelta("Cache is valid");
                    loadHashMapIndex(indexCache.getCache());
                    loggy.logTimeDelta("Cache - Loaded index to mSeasonHashIndex");
                    mLoading = false;
                }

                if (indexCache.getWriteFlag(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit, forceRefresh))
                    indexCache.saveCache();
                return null;
            }
        };
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
}
