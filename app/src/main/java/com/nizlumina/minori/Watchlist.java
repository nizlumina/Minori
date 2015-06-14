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

package com.nizlumina.minori;

import android.os.Handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nizlumina.minori.model.MinoriModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Watchlist
{
    private static volatile Watchlist mInstance;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ArrayList<MinoriModel> watchDatas = new ArrayList<>();
    private final Type gsonType = new TypeToken<ArrayList<MinoriModel>>() {}.getType();
    private final Object IOLock = new Object();
    private boolean loaded = false;

    private Watchlist()
    {
    }

    public static Watchlist getInstance()
    {
        if (mInstance == null)
        {
            synchronized (Watchlist.class)
            {
                mInstance = new Watchlist();
            }
        }
        return mInstance;
    }

    /**
     * Obtain the {@link Watchlist} singleton watch data and will lazily loads them from disk if not previously initialized.
     *
     * @param onFinishListener Listener bearing the result. The listener will be called in the background thread hence don't forget to do hands-off if necessary.
     */
    public void getWatchDatasAsync(final OnFinishListener<ArrayList<MinoriModel>> onFinishListener, final Handler handler)
    {
        if (!loaded)
        {
            executorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    ArrayList<MinoriModel> results = null;
                    synchronized (IOLock)
                    {
                        final Gson gson = new GsonBuilder().serializeNulls().create();
                        final File file = new File(MinoriApplication.getAppContext().getFilesDir(), "watchlist.json");
                        if (file.exists() && file.canRead())
                        {
                            final BufferedReader bufferedReader;
                            try
                            {
                                bufferedReader = new BufferedReader(new FileReader(file));
                                results = gson.fromJson(bufferedReader, gsonType);
                            }
                            catch (FileNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        loaded = true;
                    }
                    if (results != null)
                    {
                        watchDatas.addAll(results);
                    }
                }
            });

        }
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                onFinishListener.onFinish(watchDatas);
            }
        });

    }

    public interface OnFinishListener<T>
    {
        void onFinish(T result);
    }

}
