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
import com.nizlumina.minori.internal.network.WebUnit;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.utility.StringCache;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.Season;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//A simple controller for interacting with season data from Syncmaru + Firebase backend
public class SeasonController
{
    private static final TypeToken<List<CompositeData>> mCompositeDataListToken = new TypeToken<List<CompositeData>>() {};
    private static final Gson gson = new GsonBuilder().serializeNulls().create();
    private final WebUnit mFirebaseSeasonWebUnit = new WebUnit();
    private Season mCurrentSeason;

    public SeasonController(Season season)
    {
        mCurrentSeason = season;
    }

    private static String getRelativeURL(Season input)
    {
        return "/" + input.getSeason() + "/" + input.getYear() + ".json";
    }

    private static boolean fromJSON(String indexJson, List<CompositeData> outList)
    {
        if (indexJson != null)
        {
            List<CompositeData> gsonResult = gson.fromJson(indexJson, mCompositeDataListToken.getType());
            if (gsonResult != null)
            {
                outList.addAll(gsonResult);
                return true;
            }
        }
        return false;
    }

    public Season getSeason()
    {
        return mCurrentSeason;
    }

    //Process requested season
    public void getSeasonData(boolean forceRefreshCache, final OnFinishListener<List<CompositeData>> onFinishListener)
    {
        final StringCache seasonDataCache = new StringCache(mCurrentSeason.getIndexKey());

        boolean cacheOK = seasonDataCache.loadCache();

        final List<CompositeData> outList = new ArrayList<>();
        boolean jsonOk = false;

        if (!cacheOK || !seasonDataCache.isValid() || seasonDataCache.isStale(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit) || forceRefreshCache)
        {
            String seasonJSON = null;
            try
            {
                final String url = FirebaseConfig.SEASON_ENDPOINT + getRelativeURL(mCurrentSeason);
                seasonJSON = mFirebaseSeasonWebUnit.getString(url);
                seasonDataCache.setCache(seasonJSON);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (seasonJSON != null)
            {
                jsonOk = fromJSON(seasonJSON, outList);
            }
        }
        else
        {
            jsonOk = fromJSON(seasonDataCache.getCache(), outList);
        }

        //Finally set
        if (jsonOk)
        {
            //Initiate save
            if (seasonDataCache.getWriteFlag(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit, forceRefreshCache))
            {
                seasonDataCache.saveCache();
            }
            onFinishListener.onFinish(outList);
        }
        else onFinishListener.onFinish(null);

    }
}
