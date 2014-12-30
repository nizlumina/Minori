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

package com.nizlumina.minori.android.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for displaying base WatchData
 */

public class WatchDataPresenter
{
    private WatchData mWatchData;

    public WatchDataPresenter(WatchData watchData)
    {
        mWatchData = watchData;
    }

    public static ArrayList<WatchDataPresenter> listFrom(List<WatchData> watchDataList)
    {
        ArrayList<WatchDataPresenter> watchDataPresenters = new ArrayList<WatchDataPresenter>();
        for (WatchData watchData : watchDataList)
        {
            watchDataPresenters.add(new WatchDataPresenter(watchData));
        }

        return watchDataPresenters;
    }

    public synchronized final WatchData getWatchData()
    {
        return mWatchData;
    }

    //Presenter methods - use these for displaying data to users

    public int getID()
    {
        return getWatchData().getId();
    }

    public String getTitle()
    {
        return getWatchData().getAnimeObject().title;
    }

    public String getGroup()
    {
        return getWatchData().getNyaaEntry().fansub;
    }

    public String getEpisode()
    {
        return getWatchData().getNyaaEntry().episodeString;
    }

    public String getCoverImageURI()
    {
        return getWatchData().getAnimeObject().cachedImageURI;
    }
}
