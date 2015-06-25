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
import com.google.gson.reflect.TypeToken;
import com.nizlumina.minori.internal.FirebaseConfig;
import com.nizlumina.minori.internal.network.WebUnit;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.model.SeasonType;
import com.nizlumina.minori.utility.StringCache;
import com.nizlumina.syncmaru.model.Season;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SeasonIndexController
{
    private static final Comparator<Season> seasonComparator = new Comparator<Season>()
    {
        @Override
        public int compare(Season lhs, Season rhs)
        {
            if (lhs.getYear() == rhs.getYear())
            {
                final SeasonType a = SeasonType.fromString(lhs.getSeason());
                final SeasonType b = SeasonType.fromString(rhs.getSeason());
                if (a != null && b != null)
                {
                    return a.compareTo(b);
                }
            }
            return lhs.getYear() - rhs.getYear();
        }
    };
    private static final TypeToken<List<Season>> mSeasonHashListToken = new TypeToken<List<Season>>() {};
    private static final Gson gson = new Gson();

    private final WebUnit mFirebaseIndexWebUnit = new WebUnit();

    //Load JSON into list. Returns false if can't be parsed by GSON.
    private static boolean fromJSON(String indexJson, List<Season> outList)
    {
        if (indexJson != null)
        {
            List<Season> gsonResult = gson.fromJson(indexJson, mSeasonHashListToken.getType());
            if (gsonResult != null)
            {
                outList.addAll(gsonResult);
                return true;
            }
        }
        return false;
    }

    /**
     * Load the main seasonal index and publish the result in the list. This is not an async call (so you have wrap it in one).
     * The {@link WebUnit} used is instanced to this class instance, meaning multiple requests to a {@link SeasonIndexController} instance will be queued on the single {@link WebUnit} an instance holds.
     *
     * @param forceRefreshCache Set to true if you want to force load new data instead of feeding it from cache
     * @param onFinishListener  Result listener. The {@link Season} list result can be NULL if the JSON file can't be read. The list is also sorted with the latest season being the last in the list.
     */
    public void getIndex(final boolean forceRefreshCache, final OnFinishListener<List<Season>> onFinishListener)
    {
        final StringCache indexCacheFile = new StringCache(FirebaseConfig.INDEX_CACHEFILE);

        //Check intitial cached index
        boolean cacheOK = indexCacheFile.loadCache();

        //End variables
        final List<Season> outList = new ArrayList<>();
        boolean jsonOK = false;

        //Force HTTP GET if not valid or stale or forced
        if (!cacheOK || !indexCacheFile.isValid() || indexCacheFile.isStale(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit) || forceRefreshCache)
        {
            String indexJson = null;
            try
            {
                String url = FirebaseConfig.INDEX_ENDPOINT;
                indexJson = mFirebaseIndexWebUnit.getString(url);
                indexCacheFile.setCache(indexJson);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            if (indexJson != null)
            {
                jsonOK = fromJSON(indexJson, outList);
            }
        }
        else
        {
            jsonOK = fromJSON(indexCacheFile.getCache(), outList);
        }

        if (jsonOK)
        {
            if (indexCacheFile.getWriteFlag(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit, forceRefreshCache))
                indexCacheFile.saveCache();

            Collections.sort(outList, seasonComparator);
            onFinishListener.onFinish(outList);
        }
        else
            onFinishListener.onFinish(null);
    }
}
