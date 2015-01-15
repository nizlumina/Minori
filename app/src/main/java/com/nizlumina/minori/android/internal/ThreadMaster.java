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

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Main wrapper class for all threading task. Avoid double checked locking for now.
 */
public final class ThreadMaster
{
    private static final ThreadMaster ourInstance = new ThreadMaster();
    private ExecutorService executorService;

    private ThreadMaster()
    {
        executorService = Executors.newCachedThreadPool();
    }

    public static ThreadMaster getInstance()
    {
        return ourInstance;
    }

    public synchronized ExecutorService getExecutorService()
    {
        return executorService;
    }


    /**
     * While using ExecutorService.submit() is enough for most cases, this method expands on that by providing a guaranteed listener pattern that fires upon completion back on the original thread.
     *
     * @param callable
     * @param listener
     * @param <V>
     * @return
     */
    public <V> Future<V> enqueue(final Callable<V> callable, final Listener<V> listener)
    {
        if (Looper.myLooper() == null) Looper.prepare();
        final Handler handler = new Handler(Looper.myLooper());
        Callable<V> internalCallable = new Callable<V>()
        {
            @Override
            public V call() throws Exception
            {
                final V returnObject = callable.call();
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onFinish(returnObject);
                    }
                });

                return returnObject;
            }
        };
        return executorService.submit(internalCallable);
    }

    public interface Listener<V>
    {
        void onFinish(V result);
    }
}
