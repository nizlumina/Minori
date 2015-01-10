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

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * Internal singleton master for all WebUnit requests.
 */
class WebUnitMaster
{
    private static volatile WebUnitMaster INSTANCE = null;
    private final OkHttpClient okHttpClient;

    private WebUnitMaster(Context context)
    {
        okHttpClient = new OkHttpClient();
        int cacheSize = 10 * 1024 * 1024;
        try
        {
            Cache cache = new Cache(context.getCacheDir(), cacheSize);
            okHttpClient.setCache(cache);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static WebUnitMaster getInstance(Context context)
    {
        if (INSTANCE == null)
        {
            synchronized (WebUnitMaster.class)
            {
                if (INSTANCE == null)
                    INSTANCE = new WebUnitMaster(context);

            }
        }
        return INSTANCE;
    }

    public OkHttpClient getClient()
    {
        return okHttpClient;
    }
}

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

    /**
     * Get a string object from response body of the given url. For threaded calls, use {@link #enqueueGetString}
     *
     * @param context Any applicable context for the initialization
     * @param url     The URL for the request
     * @return The body in a string object. Returns null on failure.
     * @throws IOException
     */
    public synchronized String getString(final Context context, final String url) throws IOException
    {
        final Response response = executeSync(context, url);

        if (response != null && response.isSuccessful())
        {
            return response.body().string();
        }
        return null;
    }

    /**
     * Invoke methods on a returned Stream via a Callable
     *
     * @param context  Any applicable context for the initialization
     * @param url      The URL for the request
     * @param callable The callable instance to apply on the fully received inputstream
     * @throws IOException
     */
    public synchronized void invokeOnStream(final Context context, final String url, final StreamCallable callable) throws IOException
    {
        final Response response = executeSync(context, url);

        if (response != null && response.isSuccessful())
        {
            callable.onStreamReceived(response.body().byteStream());
        }
    }

    private Response executeSync(final Context context, final String url) throws IOException
    {
        final Request request = getFormattedRequest(url);
        return WebUnitMaster.getInstance(context).getClient().newCall(request).execute();
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
     * @param context  Any applicable context for the initialization
     * @param url      The URL for the request
     * @param listener Optional listener to retrieve the string object of the received response body. Check for null.
     * @throws IOException
     */
    public synchronized void enqueueGetString(final Context context, final String url, final WebUnitListener listener) throws IOException
    {
        executeAsync(context, url, new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
                if (listener != null) listener.onFailure();
            }

            @Override
            public void onResponse(Response response) throws IOException
            {
                if (listener != null) listener.onFinish(response.body().toString());
            }
        });
    }


    private void executeAsync(final Context context, final String url, final Callback internalCallback) throws IOException
    {
        final Request request = getFormattedRequest(url);
        WebUnitMaster.getInstance(context).getClient().newCall(request).enqueue(internalCallback);
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
