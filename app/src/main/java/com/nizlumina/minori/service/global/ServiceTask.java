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

public abstract class ServiceTask implements Runnable
{
    private final String id;
    private RequestThread requestThread;
    private Object result;
    private boolean completed;
    private onProgressListener progressListener;

    public ServiceTask(String id, RequestThread requestThread)
    {
        this.id = id;
        this.requestThread = requestThread;
    }

    public String getId()
    {
        return id;
    }

    public <T> T getResult()
    {
        //noinspection unchecked
        return (T) result;
    }

    public <T> void setResult(T result)
    {
        this.result = result;
    }

    public boolean isCompleted()
    {
        return completed;
    }

    protected void setCompleted(boolean completed)
    {
        this.completed = completed;
    }

    public void publishProgress(int progress)
    {
        progressListener.onProgress(progress);
    }

    protected RequestThread getRequestThread()
    {
        return requestThread;
    }

    protected void setProgressListener(onProgressListener progressListener)
    {
        this.progressListener = progressListener;
    }

    public enum RequestThread
    {
        DISK, NETWORK, GENERAL
    }

    protected interface onProgressListener
    {
        void onProgress(int progress);
    }
}
