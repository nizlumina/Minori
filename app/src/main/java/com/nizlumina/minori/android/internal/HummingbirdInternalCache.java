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

import android.content.Context;

import com.nizlumina.minori.android.factory.CoreJSONFactory;
import com.nizlumina.minori.android.factory.JSONStorageFactory;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.utility.Util;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A shoe-horned instance-based cache class. Might not be correct in some parts yet (trying to soft-mitigate race condition). Will rewrite this class later.
 * Currently the cache uses the same file (so just one file) for saving and loading. While a more generic class is much better, the cache currently only handles the corner case for caching Hummingbird specific stuffs. Until MAL have a better API and an intermediary server becomes feasible enough for OAuth-ing to AniList, this should be adequate for now.
 */
public class HummingbirdInternalCache
{
    static final String cacheName = "HummingbirdCache";
    private final static Object mCacheIOLock = new Object(); //force synchronized IO read/writes
    private volatile HashMap<String, AnimeObject> mCachedAnimeObjects = new HashMap<>();

    /**
     * A factory method to get an instance cache from a list of AnimeObject.
     *
     * @param animeObjects The list to create the cache from.
     * @return A WeakHashMap cache with the AnimeObject "slugs" being the key.
     */
    public static HashMap<String, AnimeObject> createInstanceCacheFrom(List<AnimeObject> animeObjects)
    {
        HashMap<String, AnimeObject> resultsCache = new HashMap<>();
        for (AnimeObject animeObject : animeObjects)
        {
            resultsCache.put(animeObject.slug, animeObject);
        }
        return resultsCache;
    }

    public HashMap<String, AnimeObject> getCache()
    {
        return mCachedAnimeObjects;
    }

    public synchronized List<AnimeObject> getCachedAnimeObjectsCopy()
    {
        List<AnimeObject> animeObjects = new ArrayList<>();
        for (AnimeObject animeObject : mCachedAnimeObjects.values())
            animeObjects.add(animeObject);
        return animeObjects;
    }

    public synchronized AnimeObject getAnimeObjectFromCache(String slug)
    {
        return mCachedAnimeObjects.get(slug);
    }

    public synchronized void applyNewCache(HashMap<String, AnimeObject> newCache)
    {
        mCachedAnimeObjects.clear();
        mCachedAnimeObjects.putAll(newCache);
    }

    /**
     * After instance creation, call this first to fill up the neccesary data for getCachedAnimeObjects().
     * The heavy work is offloaded to a background thread.
     *
     * @param context          Any context available. getCacheDir() is called on this.
     * @param onFinishListener Fires after loading (populating the instance list) is finished.
     */
    public void loadInstanceCacheAsync(final Context context, final OnFinishListener<Void> onFinishListener)
    {
        synchronized (mCacheIOLock)
        {
            final Callable<Void> backgroundTask = new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    loadInstanceCache(context);
                    return null;
                }
            };
            ThreadMaster.getInstance().enqueue(backgroundTask, onFinishListener);
        }

    }

    public void loadInstanceCache(Context context)
    {
        try
        {
            Util.logThread("loadInstanceCache");
            synchronized (mCacheIOLock)
            {
                Util.logThread("loadInstanceCache - synchronized");
                final File cacheFile = new File(context.getCacheDir(), cacheName);
                if (cacheFile.exists())
                {
                    final FileInputStream fileInputStream = new FileInputStream(cacheFile);
                    List<AnimeObject> diskCachedAnimeObjects = CoreJSONFactory.animeObjectsFromJSON(JSONStorageFactory.loadJSONArray(fileInputStream), true);

                    for (AnimeObject animeObject : diskCachedAnimeObjects)
                    {
                        mCachedAnimeObjects.put(animeObject.slug, animeObject);
                    }
                    fileInputStream.close();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Write instance cache to disk in background.
     *
     * @param context          Any context available. getCacheDir() is called on this.
     * @param onFinishListener Fires after saving is finished.
     */
    public void writeToDiskAsync(final Context context, final OnFinishListener<Void> onFinishListener)
    {
        synchronized (mCacheIOLock)
        {
            final Callable<Void> backTask = new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    writeToDisk(context);
                    return null;
                }
            };

            ThreadMaster.getInstance().enqueue(backTask, onFinishListener);
        }
    }

    public void writeToDisk(Context context)
    {
        synchronized (mCacheIOLock)
        {
            try
            {
                final File cacheFile = new File(context.getCacheDir(), cacheName);
                JSONStorageFactory.saveJSONArray(CoreJSONFactory.toJSONArray(getCachedAnimeObjectsCopy(), false), new FileOutputStream(cacheFile));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public synchronized void insert(AnimeObject animeObject)
    {
        mCachedAnimeObjects.put(animeObject.slug, animeObject);
    }

    public synchronized void remove(AnimeObject animeObject)
    {
        mCachedAnimeObjects.remove(animeObject.slug);
    }
}
