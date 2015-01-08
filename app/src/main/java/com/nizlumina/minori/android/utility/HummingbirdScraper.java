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

package com.nizlumina.minori.android.utility;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;

import com.nizlumina.minori.android.adapter.GalleryAdapter;
import com.nizlumina.minori.android.data.WatchDataPresenter;
import com.nizlumina.minori.android.network.ConnectionUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility scraper for some of the Hummingbird Pages.
 * As of current, it mainly access the Upcoming chart <a href="https://hummingbird.me/anime/upcoming/">here</a>
 */
public class HummingbirdScraper
{
    static final String endpoint = "https://hummingbird.me/anime/upcoming/";
    static final String identifierClass = "image";

    public void populateUpcomingAnime(final GalleryAdapter galleryAdapter, final ProgressBar progressBar)
    {
        AsyncTask<Void, Integer, Void> backTask = new AsyncTask<Void, Integer, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                populateResults();
                //galleryAdapter.add(populateResults());
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
        backTask.execute(null, null, null);
    }

    private List<WatchDataPresenter> populateResults()
    {
        List<WatchDataPresenter> results = new ArrayList<>();

        ConnectionUnit unit = new ConnectionUnit(true);
        String response = unit.getResponseString(endpoint);

        if (response != null)
        {
            Document document = Jsoup.parse(response);
            Elements identifierElements = document.getElementsByClass(identifierClass);
            Elements relativeLinks = identifierElements.select("a[href]");
            List<String> urls = new ArrayList<>();
            for (Element relativeLink : relativeLinks)
            {
                urls.add(relativeLink.attr("href").substring(7)); //it works in the first try!! Woohoo!!

                //Future logging
                Log.v(getClass().getSimpleName(), relativeLink.attr("href"));
            }
        }

        return results;
    }
}
