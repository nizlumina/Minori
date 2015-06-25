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

package com.nizlumina.minori.utility;

import android.util.Log;

import com.nizlumina.minori.MinoriApplication;

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
    private long mLastSavedTime = -1;

    public StringCache(String cacheFileName)
    {
        this.mCacheFileName = cacheFileName;
    }

    /**
     * @return The string that was set to this {@link StringCache}
     */
    public String getCache()
    {
        return mStringCache;
    }

    /**
     * Set the string for this {@link StringCache}
     */
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

    /**
     * Safely read the cache from disk and populate {@link #getCache()}.
     * For all loading task, this must be called before any other methods is called.
     * Returns true if cache exist AND loaded succesfully. False otherwise.
     */
    public boolean loadCache()
    {
        File cacheFileInput = getCacheFile();
        if (cacheFileInput.exists())
        {
            try
            {
                synchronized (mLock)
                {
                    FileInputStream fileInputStream = new FileInputStream(cacheFileInput);
                    String rawCache = IOUtils.toString(fileInputStream, encoding);
                    mStringCache = deformatCache(rawCache);
                }
                if (mStringCache != null)
                    return true;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
            Log.w(getCacheFileName(), "File does not exist");
        return false;
    }

    /**
     * Safely save the cache to disk.
     */
    public void saveCache()
    {
        File cacheFileOutput = getCacheFile();
        if (mStringCache != null)
        {
            synchronized (mLock)
            {
                try
                {
                    this.setLastSavedTime(System.currentTimeMillis());
                    FileOutputStream fileOutputStream = new FileOutputStream(cacheFileOutput);
                    IOUtils.write(formatCache(mStringCache), fileOutputStream, encoding);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        else Log.e(getCacheFileName(), "Cached string is null!");
    }

    /**
     * Release the cached string of the current StringCache object
     */
    public void releaseCache()
    {
        mStringCache = null;
    }

    /**
     * @return The cache file instance from the application cache dir.
     */
    public File getCacheFile()
    {
        return new File(MinoriApplication.getAppContext().getCacheDir(), mCacheFileName);
    }

    /**
     * Format the current string cache for storage. LastSavedTime value is set and appended to the beginning string here.
     *
     * @param input The original string from {@link #getCache()}
     * @return A formatted string of {@link #getCache()}
     */
    private String formatCache(String input)
    {
        return timeIdentifier + System.currentTimeMillis() + timeIdentifier + input;
    }

    /**
     * @return The saved time of this cache.  This will return -1 if the cache isn't saved yet.
     */
    public long getLastSavedTime()
    {
        return mLastSavedTime;
    }

    /**
     * This is set to private since LastSavedTime is only set when the cache is saved.
     */
    private void setLastSavedTime(long mLastSavedTime)
    {
        this.mLastSavedTime = mLastSavedTime;
    }

    /**
     * Deformat raw cache from storage
     *
     * @param input The raw {@link #getCache()} from storage
     * @return The original string ({@link #getCache()}) before being saved. Null if it cannot be deformatted.
     */
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

    /**
     * Helper method for cache writing logic.
     * This returns true if the cache stale, or forced to be written.
     * this however returns false if the cache is not initialized correctly ({@link #getCache()} must not be null and empty)
     *
     * @param staleThreshold The threshold value to determine staleness
     * @param calendarUnit   Unit of the threshold value e.g {@link java.util.Calendar#DAY_OF_YEAR}
     * @param forceWrite     Flag to force writing the cache
     */
    public boolean getWriteFlag(int staleThreshold, int calendarUnit, boolean forceWrite)
    {
        return this.isValid() && (this.getLastSavedTime() < 0 || this.isStale(staleThreshold, calendarUnit) || forceWrite);
    }

    /**
     * Check whether {@link #getCache()} is valid (not null and not empty).
     */
    public boolean isValid()
    {
        return this.getCache() != null && this.getCache().length() > 0;
    }

}
