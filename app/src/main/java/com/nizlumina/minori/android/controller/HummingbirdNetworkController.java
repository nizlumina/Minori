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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.android.factory.CoreNetworkFactory.NetworkFactoryListener;
import com.nizlumina.minori.android.internal.HummingbirdInternalCache;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.network.WebUnit;
import com.nizlumina.minori.android.utility.HummingbirdScraper;
import com.nizlumina.minori.android.utility.Util;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

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

    private final HummingbirdInternalCache mHummingbirdInternalCache;

    public HummingbirdNetworkController()
    {
        mHummingbirdInternalCache = new HummingbirdInternalCache();
    }

    public synchronized void populateListSync(final Context context, final List<AnimeObject> results, NetworkListener<AnimeObject> networkListener)
    {
        final WebUnit unit = new WebUnit();
        String response = null;
        try
        {
            response = unit.getString(HummingbirdScraper.getEndpoint());
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

    public synchronized void populateListAsync(final Context context, final NetworkListener<AnimeObject> networkListener)
    {
        Util.logThread("populateListAsync");
        /**
         * Test
         */
        final OkHttpClient client = new OkHttpClient();
        final String responseKey = "upcoming_key";
        final Handler handler = new Handler(new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message msg)
            {
                Util.logThread("handler");

                Bundle bundah = msg.getData();
                if (bundah != null)
                {
                    final String response = bundah.getString(responseKey, null);
                    if (response != null)
                    {

                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final List<String> animeSlugs = HummingbirdScraper.scrapeUpcoming(response);
                                for (String animeSlug : animeSlugs)
                                {
                                    final long t = System.nanoTime();
                                    CoreNetworkFactory.getAnimeObjectAsync(context, animeSlug, new NetworkFactoryListener<AnimeObject>()
                                    {
                                        @Override
                                        public void onFinish(AnimeObject result)
                                        {
                                            Util.logTime(t);
                                            Log.v("Task", "Callback from network!");
                                            if (networkListener != null)
                                                networkListener.onEachSuccessfulResponses(result);
                                        }
                                    });
                                }
                                Log.v("Task", "Task thread exit!");
                            }
                        }).start();

                    }

                }
                return false;
            }
        });


        client.newCall(new Request.Builder().get().url(HummingbirdScraper.getEndpoint()).build()).enqueue(new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {

            }

            @Override
            public void onResponse(Response response) throws IOException
            {
                Util.logThread("On First Response");
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putString(responseKey, response.body().string());
                message.setData(bundle);
                message.setTarget(handler);
                message.sendToTarget();
            }
        });


        //////////////////////////


//        final WebUnit webUnit = new WebUnit();
//        try
//        {
//            webUnit.enqueueGetString(HummingbirdScraper.getEndpoint(), new WebUnit.WebUnitListener()
//            {
//                @Override
//                public void onFailure()
//                {
//
//                }
//
//                @Override
//                public void onFinish(final String responseBody)
//                {
//                    webUnit.enqueueGetString();
//                    processScraperResponse(context, responseBody, networkListener);
//                }
//            });
//
//
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
    }

    //The code below will generate lots of request to the HummingbirdAPI.
    //Make sure to cache stuffs if possible.
    private void processCache(Context context, List<AnimeObject> results, List<String> animeSlugs, NetworkListener<AnimeObject> networkListener)
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

    private void processScraperResponse(final Context context, final String response, final NetworkListener<AnimeObject> networkListener)
    {
        Util.logThread("A1:Process Scraper start");

        Task.callInBackground(new Callable<List<String>>()
        {
            @Override
            public List<String> call() throws Exception
            {
                final List<String> animeSlugs = new ArrayList<String>();
                // measured time avg for code below: 70~ ms
                // Still push it off to another thread? Awww yes.
                long t = System.nanoTime();
                animeSlugs.addAll(HummingbirdScraper.scrapeUpcoming(response));
                Util.logTime(t);
                Log.v(getClass().getSimpleName(), "Slugs count " + animeSlugs.size());
                return animeSlugs;
            }
        }).continueWith(new Continuation<List<String>, Void>()
        {
            @Override
            public Void then(Task<List<String>> task) throws Exception
            {
                Util.logThread("A2:Process mass unit responses start");
                List<String> animeSlugs = task.getResult();
                for (String animeSlug : animeSlugs)
                {
                    final long t = System.nanoTime();
                    CoreNetworkFactory.getAnimeObjectAsync(context, animeSlug, new NetworkFactoryListener<AnimeObject>()
                    {
                        @Override
                        public void onFinish(AnimeObject result)
                        {
                            Util.logTime(t);
                            Log.v("Task", "Callback from network!");
                            if (networkListener != null)
                                networkListener.onEachSuccessfulResponses(result);
                        }
                    });
                }
                Log.v("Task", "Task thread exit!");
                return null;
            }
        });
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

    public interface NetworkListener<Result>
    {
        void onEachSuccessfulResponses(Result result);

        void onFinish();
    }
}
