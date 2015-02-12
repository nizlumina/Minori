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

import com.nizlumina.minori.android.listener.OnFinishListener;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Main wrapper class for all threading task. Avoid double checked locking for now.
 */
public final class ThreadMaster
{
    private ExecutorService executorService;

    public ThreadMaster()
    {
        executorService = Executors.newCachedThreadPool();
    }

    public synchronized ExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * While using ExecutorService.submit() is enough for most cases, this method expands on that by providing a guaranteed listener pattern that fires upon completion back on the original thread.
     *
     * @param backgroundTask   The callable task to be done in background thread
     * @param onFinishListener Optional listener that pass the result in OnFinish
     * @param <Result>         Type for the result.
     * @return a generic Future
     */
    public <Result> Future<Result> enqueue(final Callable<Result> backgroundTask, final OnFinishListener<Result> onFinishListener)
    {
        if (Looper.myLooper() == null) Looper.prepare();
        final Handler handler = new Handler(Looper.myLooper());
        Callable<Result> internalCallable = new Callable<Result>()
        {
            @Override
            public Result call() throws Exception
            {
                final Result returnObject = backgroundTask.call();
                if (onFinishListener != null)
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            onFinishListener.onFinish(returnObject);
                        }
                    });
                }
                return returnObject;
            }
        };
        return executorService.submit(internalCallable);
    }

}
