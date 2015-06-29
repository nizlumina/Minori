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

package com.nizlumina.minori.service.global;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

public abstract class DirectorTask<ResultType> extends ResultReceiver implements Runnable
{
    private static final int CODE_PROGRESS = 0;
    private static final int CODE_FINISH = 1;
    private final RequestThread requestThread;
    private String id;
    private ResultType result;
    private boolean completed;
    private int mProgress;
    private WeakReference<Listener<ResultType>> mListenerWeakReference;

    public DirectorTask(String id, RequestThread requestThread)
    {
        super(new Handler());
        this.id = id;
        this.requestThread = requestThread;
    }

    public DirectorTask(String id, RequestThread requestThread, @NonNull Handler handler)
    {
        super(handler);
        this.id = id;
        this.requestThread = requestThread;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    protected void setCompleted(boolean completed)
    {
        this.completed = completed;
    }

    protected RequestThread getRequestThread()
    {
        return requestThread;
    }

    /**
     * If you override this, make sure to call super.
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData)
    {
        super.onReceiveResult(resultCode, resultData);
        Listener<ResultType> listener = mListenerWeakReference.get();

        if (listener != null)
        {
            if (resultCode == CODE_PROGRESS)
            {
                listener.onProgress(mProgress);
            }
            else if (resultCode == CODE_FINISH)
            {
                listener.onFinish(result);
            }
        }
    }

    public void publishProgress(int progress)
    {
        this.mProgress = progress;
        this.send(CODE_PROGRESS, null);
    }

    public void publishResult(ResultType result)
    {
        this.result = result;
        setCompleted(true);
        this.send(CODE_FINISH, null);
    }

    /**
     * Set a weak referenced listener to receive callback from both {@link #publishProgress(int)} and {@link #publishResult(Object)} if they were called in the run block.
     * As w.r.t Android lifecycle, don't forget to call setListener(null) in Activity/Fragment exit methods. (Best practice is to call it in onPause() to make sure views called inside the listener isn't leaked.)
     */
    public void setListener(Listener<ResultType> listener)
    {
        mListenerWeakReference = new WeakReference<>(listener);
    }

    /**
     * Helper method for Android lifecycles. When returning from configuration changes, in the case where the broadcast was missed (e.g. called during rotation/listener being null), calling this will republish result to the listener (set it first before calling this).
     *
     * @return True if the task is completed and republished. False if the task hasn't been completed yet.
     */
    public boolean republishResultIfCompleted()
    {
        if (completed)
            publishResult(result);
        return completed;
    }

    public void enqueue()
    {
        Director.getInstance().enqueue(this);
    }

    public void attach(Listener<ResultType> listener)
    {
        setListener(listener);
        Director.getInstance().cancelShutdown();
    }

    public void detach()
    {
        setListener(null);
    }

    public enum RequestThread
    {
        DISK, NETWORK, GENERAL
    }

    /**
     * Listener to be set for a ServiceTask. This listener is hold by a WeakReference.
     *
     * @param <T> Type for the result.
     */
    public interface Listener<T>
    {
        void onProgress(int progress);

        void onFinish(T result);
    }
}
