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

import android.util.Log;

import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.android.internal.HummingbirdInternalCache;
import com.nizlumina.minori.android.internal.Minori;
import com.nizlumina.minori.android.internal.ThreadMaster;
import com.nizlumina.minori.android.listener.NetworkListener;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.listener.WebUnitListener;
import com.nizlumina.minori.android.network.WebUnit;
import com.nizlumina.minori.android.utility.HummingbirdScraper;
import com.nizlumina.minori.android.utility.Util;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Controller for populating data to the UI. Image loading is handled by ImageLoaderController. <br/>
 * Make sure to cache the controller instance as well since it also holds the reference to the internal cache mechanism. <br/><br/>
 * While the implementation of the HummingbirdInternalCache is thread-safe, this class is not fully thread safe (yet).
 */
public class HummingbirdNetworkController
{

    private final HummingbirdInternalCache mHummingbirdInstanceCache;

    public HummingbirdNetworkController()
    {
        mHummingbirdInstanceCache = new HummingbirdInternalCache();
    }

    public synchronized void getUpcomingAnimeObjectAsync(final NetworkListener<AnimeObject> networkListener, final boolean refreshCache)
    {
        final ThreadMaster master = ThreadMaster.getInstance();
        final long startTime = System.nanoTime();
        Callable<List<AnimeObject>> backgroundTask = new Callable<List<AnimeObject>>()
        {
            @Override
            public List<AnimeObject> call() throws Exception
            {

                Util.logThread(getClass().getSimpleName());
                final List<AnimeObject> results = new ArrayList<>();
                WebUnit webUnit = new WebUnit();
                webUnit.setCache(true);

                final long connectTime = System.nanoTime();
                String body = webUnit.getString(HummingbirdScraper.getEndpoint());
                Util.logTime(connectTime, "Get Upcoming");

                if (body != null)
                {
                    List<String> slugs = HummingbirdScraper.scrapeUpcoming(body);
                    Log.v(getClass().getSimpleName(), "Slugs size: " + slugs.size());
                    final CountDownLatch latch = new CountDownLatch(slugs.size()); //Block thread until all finishes
                    mHummingbirdInstanceCache.loadInstanceCache(Minori.getAppContext());
                    Log.v(getClass().getSimpleName(), "All is good " + "Cache size is " + mHummingbirdInstanceCache.getCache().size());
                    for (final String slug : slugs)
                    {
                        final AnimeObject cachedAnimeObject = mHummingbirdInstanceCache.getAnimeObjectFromCache(slug);

                        if (cachedAnimeObject == null || refreshCache)
                        {
                            if (!refreshCache)
                                Log.v(getClass().getSimpleName(), "No cache for: " + slug);

                            final long t = System.nanoTime();
                            WebUnitListener<AnimeObject> listener = new WebUnitListener<AnimeObject>()
                            {
                                @Override
                                public void onFailure()
                                {
                                    latch.countDown();
                                }

                                @Override
                                public void onFinish(AnimeObject result)
                                {
                                    Util.logTime(t, "Response read! and CD is: " + latch.getCount());
                                    //the order is very important here!
                                    if (result != null) results.add(result);
                                    latch.countDown();
                                    mHummingbirdInstanceCache.insert(result);
                                    networkListener.onEachSuccessfulResponses(result);
                                }
                            };

                            CoreNetworkFactory.getAnimeObjectAsync(slug, listener, webUnit);
                        }
                        else
                        {
                            Log.v(getClass().getSimpleName(), "Cache hit for: " + slug);
                            results.add(cachedAnimeObject);
                            latch.countDown();
                            networkListener.onEachSuccessfulResponses(cachedAnimeObject);
                        }
                    }
                    latch.await();

                    //Sanitize cache
                    List<AnimeObject> removalList = new ArrayList<>();
                    for (AnimeObject cachedAnimeObject : results)
                    {
                        if (!slugs.contains(cachedAnimeObject.slug)) //check if cached slug is not included in the newer slugs
                            removalList.add(cachedAnimeObject);
                    }

                    results.removeAll(removalList);
                    removalList.clear();
                }

                return results;
            }
        };

        master.enqueue(backgroundTask, new ThreadMaster.Listener<List<AnimeObject>>()
        {
            @Override
            public void onFinish(List<AnimeObject> animeObjects)
            {
                networkListener.onFinish();
                //process cache!
                Util.logTime(startTime, "Thread Finishes!");
                //debugList(animeObjects);
                Log.v(getClass().getSimpleName(), "Final size: " + animeObjects.size());
                mHummingbirdInstanceCache.writeToDisk(Minori.getAppContext());
            }
        });
    }

    public synchronized void searchAnimeAsync(final String terms, final OnFinishListener<List<AnimeObject>> resultListener)
    {
        CoreNetworkFactory.searchAnimeAsync(terms, new WebUnitListener<List<AnimeObject>>()
        {
            @Override
            public void onFailure()
            {

            }

            @Override
            public void onFinish(List<AnimeObject> result)
            {
                if (resultListener != null) resultListener.onFinish(result);
            }
        });
    }

    private void debugList(List<AnimeObject> animeObjects)
    {
        for (AnimeObject animeObject : animeObjects)
        {
            String log = String.format("%s \n %s", animeObject.slug, animeObject.imageUrl);
            Log.v(getClass().getSimpleName(), log);
        }
    }

}
