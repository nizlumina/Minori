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
import android.os.AsyncTask;
import android.widget.ProgressBar;

import com.nizlumina.minori.android.adapter.GalleryAdapter;
import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.android.network.ConnectionUnit;
import com.nizlumina.minori.android.presenter.AnimeObjectPresenter;
import com.nizlumina.minori.android.utility.HummingbirdScraper;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for populating data to the UI. <br/>
 * Make sure to cache the controller instance as well since it also holds the reference to the internal cache mechanism. <br/><br/>
 * While the implementation of the HummingbirdCacheController is thread-safe, this class is not thread safe (yet).
 */
public class HummingbirdNetworkController
{
    static final String endpoint = "https://hummingbird.me/anime/upcoming/";

    private final HummingbirdCacheController mCacheController;

    public HummingbirdNetworkController()
    {
        mCacheController = new HummingbirdCacheController();
    }

    public void populateUpcomingAnime(final Context context, final GalleryAdapter<AnimeObjectPresenter> galleryAdapter, final ProgressBar progressBar)
    {
        AsyncTask<Void, Integer, Void> populateTask = new AsyncTask<Void, Integer, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                List<AnimeObject> results = populateResults(context);
                if (results.size() > 0)
                {
                    List<AnimeObjectPresenter> animeObjectPresenters = AnimeObjectPresenter.listFrom(results);
                    galleryAdapter.add(animeObjectPresenters);
                }
                publishProgress();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                if (galleryAdapter != null) galleryAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onProgressUpdate(Integer... values)
            {
                if (progressBar != null) progressBar.setProgress(values[0]);
            }
        };
        populateTask.execute(null, null, null);
    }

    private List<AnimeObject> populateResults(final Context context)
    {
        List<AnimeObject> results = new ArrayList<>();

        ConnectionUnit unit = new ConnectionUnit(true);
        String response = unit.getResponseString(endpoint);

        if (response != null)
        {
            List<String> animeSlugs = HummingbirdScraper.scrapePath(response);
            processCache(context, results, animeSlugs);
        }

        return results;
    }

    //The code below will generate lots of request to the HummingbirdAPI.
    //Make sure to cache stuffs if possible.
    private void processCache(Context context, List<AnimeObject> results, List<String> animeSlugs)
    {
        mCacheController.loadInstanceCache(context);

        for (String animeSlug : animeSlugs)
        {
            AnimeObject cachedAnimeObject = mCacheController.getAnimeObjectFromCache(animeSlug);
            //If not in cache
            if (cachedAnimeObject == null)
            {
                //initiate response
                cachedAnimeObject = CoreNetworkFactory.getAnimeObject(animeSlug);

                //skip if still null
                if (cachedAnimeObject == null)
                    continue;
            }
            results.add(cachedAnimeObject);
        }

        //apply image sources
        for (AnimeObject animeObject : results)
        {

        }

        //delegate save task
        mCacheController.writeToDiskAsync(context, null);
    }
}
