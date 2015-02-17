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
import com.google.gson.reflect.TypeToken;
import com.nizlumina.minori.android.internal.FirebaseConfig;
import com.nizlumina.minori.android.internal.StringCache;
import com.nizlumina.minori.android.internal.ThreadWorker;
import com.nizlumina.minori.android.network.WebUnit;
import com.nizlumina.minori.android.utility.Loggy;
import com.nizlumina.syncmaru.model.Season;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeasonDataIndexController
{
    private final WebUnit mFirebaseIndexWebUnit = new WebUnit();
    //    //This is only for initial fragment
//    private void getCurrentSeason()
//    {
//        String currentSeasonName = SeasonType.getCurrentSeason().name().toLowerCase();
//        int year = Calendar.getInstance().get(Calendar.YEAR);
//        mCurrentSeason = mSeasonHashIndex.get(Season.makeIndexKey(currentSeasonName, year));
//    }
    private final Loggy loggySmall = new Loggy();
    private final TypeToken<List<Season>> mSeasonHashListToken = new TypeToken<List<Season>>() {};
    private final Map<String, Season> mSeasonHashIndex = new HashMap<String, Season>(0);

    public void processIndex(ThreadWorker threadWorker)
    {
        loggySmall.logTimedStart("PROCESS: Index Start");
        StringCache indexCache = new StringCache(FirebaseConfig.INDEX_CACHEFILE);

        //Check intitial cached index
        indexCache.loadCache();
        loggySmall.logTimed("Index cache load ends");

        //Force HTTP GET if not valid or stale
        if (!indexCache.isValid() || indexCache.isStale(FirebaseConfig.staleThreshold, FirebaseConfig.staleTimeUnit))
        {
            String indexJson = null;
            try
            {
                indexJson = mFirebaseIndexWebUnit.getString(FirebaseConfig.INDEX_ENDPOINT);
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
}
