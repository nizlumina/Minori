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

import com.nizlumina.minori.android.factory.CoreJSONFactory;
import com.nizlumina.minori.android.factory.JSONStorageFactory;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A shoe-horned static cache class. Might not be correct in some parts yet (trying to soft-mitigate race condition). Will rewrite this class later.
 */
public final class HummingbirdCacheController
{
    static final String cacheName = "HummingbirdCache";
    static final List<AnimeObject> cachedAnimeObjects = new ArrayList<>();

    public synchronized static List<AnimeObject> getCachedAnimeObjects()
    {
        return cachedAnimeObjects;
    }

    public synchronized static void loadToMemory(final Context context, final OnFinishListener onFinishListener)
    {
        final Callable<Void> backTask = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                try
                {
                    final File cacheFile = new File(context.getCacheDir(), cacheName);
                    if (cacheFile.exists())
                    {
                        final FileInputStream fileInputStream = new FileInputStream(cacheFile);
                        cachedAnimeObjects.addAll(CoreJSONFactory.animeObjectsFromJSON(JSONStorageFactory.loadJSONArray(fileInputStream), true));
                    }
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        };


        final Callable<Void> onFinish = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                if (onFinishListener != null) onFinishListener.onFinish();
                return null;
            }
        };

        ThreadController.post(backTask, onFinish);

    }

    public synchronized static void writeToDisk(final Context context, final OnFinishListener onFinishListener)
    {
        final Callable<Void> backTask = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                try
                {
                    final File cacheFile = new File(context.getCacheDir(), cacheName);
                    if (!cacheFile.exists()) cacheFile.createNewFile();

                    JSONStorageFactory.saveJSONArray(CoreJSONFactory.toJSONArray(cachedAnimeObjects, true), new FileOutputStream(cacheFile));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                return null;
            }
        };

        final Callable<Void> onFinish = new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                if (onFinishListener != null) onFinishListener.onFinish();
                return null;
            }
        };

        ThreadController.post(backTask, onFinish);
    }
}
