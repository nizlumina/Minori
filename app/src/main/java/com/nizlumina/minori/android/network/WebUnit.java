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

package com.nizlumina.minori.android.network;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.nizlumina.minori.android.utility.Util;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Slave class for common HTTP responses and request.
 * Downloading stuffs is not fully-implemented yet with this class.
 * Otherwise, instances are thread-safe.
 * <p>
 * Any WebUnit calls should be wrapped in a worker thread.
 * Enqueuing any other request apart from String request will be added later as needed
 * (it will follow this <a href="http://stackoverflow.com/a/23965563/3939904">link</a>)
 * </p>
 */
public class WebUnit
{
    private static final String userAgentKey = "User-Agent";
    private static final String userAgent = "minori-android";
    private final OkHttpClient mClient;

    public WebUnit()
    {
        mClient = new OkHttpClient();
    }

    public OkHttpClient getClient()
    {
        return mClient;
    }

    public void enableCache(Context context)
    {
        int cacheSize = 10 * 1024 * 1024;
        try
        {
            Cache cache = new Cache(context.getCacheDir(), cacheSize);
            mClient.setCache(cache);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Get a string object from response body of the given url. For threaded calls, use {@link #enqueueGetString}
     *
     * @param url     The URL for the request
     * @return The body in a string object. Returns null on failure.
     * @throws IOException
     */
    public synchronized String getString(final String url) throws IOException
    {
        final Response response = executeSync(url);

        if (response != null && response.isSuccessful())
        {
            return response.body().string();
        }
        return null;
    }

    /**
     * Invoke methods on a returned Stream via a Callable
     *
     * @param url      The URL for the request
     * @param callable The callable instance to apply on the fully received inputstream
     * @throws IOException
     */
    public synchronized void invokeOnStream(final String url, final StreamCallable callable) throws IOException
    {
        final Response response = executeSync(url);

        if (response != null && response.isSuccessful())
        {
            callable.onStreamReceived(response.body().byteStream());
        }
    }


    private Request getFormattedRequest(String url)
    {
        return new Request.Builder()
                .url(url)
                .addHeader(userAgentKey, userAgent).build();
    }

    /**
     * Enqueue a request to the given URL and receive the response body in a String object via a listener
     *
     * @param url      The URL for the request
     * @param listener Optional listener to retrieve the string object of the received response body. Check for null. This runs on the original thread that first calls the method.
     * @throws IOException
     */
    public synchronized void enqueueGetString(final String url, final WebUnitListener listener) throws IOException
    {
        Util.logThread("req enqueued");
        final Handler handler = new Handler();
        executeAsync(url, new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (listener != null) listener.onFailure();
                    }
                });
            }

            @Override
            public void onResponse(final Response response) throws IOException
            {
                final String finalResponse = response.body().string();
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (listener != null) listener.onFinish(finalResponse);
                    }
                });

            }
        });
    }

    private Response executeSync(final String url) throws IOException
    {
        final Request request = getFormattedRequest(url);
        return getClient().newCall(request).execute();
    }

    private void executeAsync(final String url, final Callback internalCallback) throws IOException
    {
        final Request request = getFormattedRequest(url);
        Log.v("currentreq", request.urlString());
        getClient().newCall(request).enqueue(internalCallback);
        Log.v("currentreq finished", "req finish");
    }

    /**
     * A simple specialized callable interface for WebUnit stream responses
     */
    public interface StreamCallable
    {
        /**
         * This is called when the stream is fully received
         *
         * @param inputStream The full InputStream received
         */
        public void onStreamReceived(InputStream inputStream);
    }

    /**
     * A simple listener interface for any WebUnit asynchronous operations
     */
    public interface WebUnitListener
    {
        /**
         * Fires if the request failed in some way
         */
        public void onFailure();

        /**
         * Fires upon the completion of the request
         *
         * @param responseBody A string object of the response body
         */
        public void onFinish(String responseBody);
    }
}
