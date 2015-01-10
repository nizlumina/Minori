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

import android.content.Context;
import android.util.Log;

import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.android.factory.CoreNetworkFactory.NetworkFactoryListener;
import com.nizlumina.minori.android.internal.HummingbirdInternalCache;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.network.WebUnit;
import com.nizlumina.minori.android.utility.HummingbirdScraper;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Controller for populating data to the UI. Image loading is handled by ImageLoaderController. <br/>
 * Make sure to cache the controller instance as well since it also holds the reference to the internal cache mechanism. <br/><br/>
 * While the implementation of the HummingbirdInternalCache is thread-safe, this class is not fully thread safe (yet).
 */
public class HummingbirdNetworkController
{
    private static final String endpoint = "https://hummingbird.me/anime/upcoming/";

    private final HummingbirdInternalCache mHummingbirdInternalCache;

    public HummingbirdNetworkController()
    {
        mHummingbirdInternalCache = new HummingbirdInternalCache();
    }

    public synchronized void populateList(final Context context, final List<AnimeObject> results, NetworkListener networkListener)
    {
        final WebUnit unit = new WebUnit();
        String response = null;
        try
        {
            response = unit.getString(context, endpoint);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (response != null)
        {
            List<String> animeSlugs = HummingbirdScraper.scrapeUpcoming(response);
            this.processCache(context, results, animeSlugs, networkListener);
        }
    }

    public synchronized void populateListAsync(final Context context, final NetworkListener networkListener)
    {
        final WebUnit webUnit = new WebUnit();
        Log.v(getClass().getName(), String.valueOf(Thread.currentThread().getId()));
        try
        {
            webUnit.enqueueGetString(context, endpoint, new WebUnit.WebUnitListener()
            {
                @Override
                public void onFailure()
                {

                }

                @Override
                public void onFinish(final String responseBody)
                {

                    Log.v(getClass().getName(), String.valueOf(Thread.currentThread().getId()));
                    Task.callInBackground(new Callable<List<String>>()
                    {
                        @Override
                        public List<String> call() throws Exception
                        {
                            return HummingbirdScraper.scrapeUpcoming(responseBody);
                        }
                    }).continueWith(new Continuation<List<String>, Void>()
                    {
                        @Override
                        public Void then(Task<List<String>> task) throws Exception
                        {
                            List<String> animeSlugs = task.getResult();

                            for (String animeSlug : animeSlugs)
                            {
                                CoreNetworkFactory.getAnimeObjectAsync(context, animeSlug, new NetworkFactoryListener<AnimeObject>()
                                {
                                    @Override
                                    public void onFinish(AnimeObject result)
                                    {
                                        //Todo: Verification.class.verify(objectresult)
                                        if (networkListener != null)
                                            networkListener.onEachSuccessfulResponses(result);
                                    }
                                });
                            }
                            return null;
                        }
                    });


                }
            });


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //The code below will generate lots of request to the HummingbirdAPI.
    //Make sure to cache stuffs if possible.
    private void processCache(Context context, List<AnimeObject> results, List<String> animeSlugs, NetworkListener networkListener)
    {
        this.mHummingbirdInternalCache.loadInstanceCache(context);

        //init cache
        for (String animeSlug : animeSlugs)
        {
            AnimeObject cachedAnimeObject = mHummingbirdInternalCache.getAnimeObjectFromCache(animeSlug);
            //If not in cache
            if (cachedAnimeObject == null)
            {
                //initiate response
                cachedAnimeObject = CoreNetworkFactory.getAnimeObject(context, animeSlug);

                //skip if still null
                if (cachedAnimeObject == null)
                    continue;
            }
            if (networkListener != null)
                networkListener.onEachSuccessfulResponses(cachedAnimeObject);
            results.add(cachedAnimeObject);
            Log.v(getClass().getSimpleName(), String.format("Success: %s %s", cachedAnimeObject.title, cachedAnimeObject.id));
        }

        //remove old cache
        List<AnimeObject> removalList = new ArrayList<>();
        for (AnimeObject cachedAnimeObject : results)
        {
            if (!animeSlugs.contains(cachedAnimeObject.slug))
                removalList.add(cachedAnimeObject);
        }

        results.removeAll(removalList);
        Log.v(getClass().getSimpleName(), "Results " + String.valueOf(results.size()));

        if (networkListener != null) networkListener.onFinish();
    }

    public void saveCacheAsync(Context context, OnFinishListener onFinishListener)
    {
        //delegate save task
        mHummingbirdInternalCache.writeToDiskAsync(context, null);
        if (onFinishListener != null) onFinishListener.onFinish();
    }

    public WeakHashMap<String, AnimeObject> getCache()
    {
        return mHummingbirdInternalCache.getCache();
    }

    public static interface NetworkListener
    {
        void onEachSuccessfulResponses(AnimeObject animeObject);

        void onFinish();
    }
}
