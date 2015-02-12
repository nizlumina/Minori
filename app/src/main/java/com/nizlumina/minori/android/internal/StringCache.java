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

package com.nizlumina.minori.android.internal;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

//A simple utility cache class for strings (UTF-8) with stale methods baked in
public class StringCache
{
    private static final String timeIdentifier = "%{t}%";
    private static final String encoding = "UTF-8";
    private final Object mLock = new Object();
    private String mCacheFileName;
    private String mStringCache;
    private long mLastSavedTime;

    public StringCache(String cacheFileName)
    {
        this.mCacheFileName = cacheFileName;
    }

    public String getCache()
    {
        return mStringCache;
    }

    public void setCache(String input)
    {
        this.mStringCache = input;
    }

    public String getCacheFileName()
    {
        return mCacheFileName;
    }

    public void setCacheFileName(String mCacheFileName)
    {
        this.mCacheFileName = mCacheFileName;
    }

    public void loadCache()
    {
        File cacheFileInput = getCacheFile();
        if (cacheFileInput.exists())
        {
            synchronized (mLock)
            {
                try
                {
                    FileInputStream fileInputStream = new FileInputStream(cacheFileInput);
                    String rawCache = IOUtils.toString(fileInputStream, encoding);
                    mStringCache = deformatCache(rawCache);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveCache()
    {
        File cacheFileOutput = getCacheFile();
        if (mStringCache != null)
        {
            synchronized (mLock)
            {
                try
                {
                    FileOutputStream fileOutputStream = new FileOutputStream(cacheFileOutput);
                    IOUtils.write(formatCache(mStringCache), fileOutputStream, encoding);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void releaseCache()
    {
        mStringCache = null;
    }

    private File getCacheFile()
    {
        return new File(Minori.getAppContext().getCacheDir(), mCacheFileName);
    }

    private String formatCache(String input)
    {
        return timeIdentifier + System.currentTimeMillis() + timeIdentifier + input;
    }

    public long getLastSavedTime()
    {
        return mLastSavedTime;
    }

    private String deformatCache(String input)
    {
        if (input != null)
        {
            int startIndex = input.indexOf(timeIdentifier);
            int lastIndex = input.lastIndexOf(timeIdentifier);

            if (startIndex >= 0 && lastIndex > 0)
            {
                String lastSavedTime = input.substring(timeIdentifier.length() + startIndex, lastIndex);
                mLastSavedTime = Long.parseLong(lastSavedTime);
                return input.substring(lastIndex + timeIdentifier.length());
            }
        }
        return null;
    }

    /**
     * Check cache staleness.
     *
     * @param staleThreshold The threshold value to determine staleness
     * @param calendarUnit   Unit of the threshold value e.g {@link java.util.Calendar#DAY_OF_YEAR}
     */
    public boolean isStale(int staleThreshold, int calendarUnit)
    {

        if (mLastSavedTime < 0) return true; //force stale if invalid date

        Calendar indexCalendar = Calendar.getInstance();
        indexCalendar.setTimeInMillis(mLastSavedTime);
        int lastUpdatedDay = indexCalendar.get(calendarUnit);

        return Calendar.getInstance().get(calendarUnit) - lastUpdatedDay > staleThreshold;
    }
}
