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

package com.nizlumina.minori.internal;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nizlumina.minori.listener.OnFinishListener;

import java.util.concurrent.Callable;

/**
 * Main wrapper class for all threading task. Avoid double checked locking for now.
 */
public final class ThreadWorker<Result>
{
    private AsyncTask<Result, Void, Result> mAsyncTask;

    public boolean cancelRunningTask()
    {
        return mAsyncTask == null || mAsyncTask.cancel(true);
    }

    public void postAsyncTask(@NonNull final Callable<Result> callable, @Nullable final OnFinishListener<Result> onFinishListener)
    {
        @SuppressWarnings("unchecked") AsyncTask<Result, Void, Result> asyncTask = new AsyncTask<Result, Void, Result>()
        {
            @Override
            protected Result doInBackground(Result... params)
            {
                try
                {
                    return callable.call();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Result result)
            {
                if (onFinishListener != null) onFinishListener.onFinish(result);
            }
        };

        this.mAsyncTask = asyncTask.execute(null, null, null);
    }
}
