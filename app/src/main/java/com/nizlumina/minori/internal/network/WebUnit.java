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

package com.nizlumina.minori.internal.network;

import com.nizlumina.minori.MinoriApplication;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Internal singleton master for all WebUnit requests.
 */
final class WebUnitMaster
{
    private static volatile WebUnitMaster INSTANCE = null;
    private final OkHttpClient mainClient;
    private Cache mCache;

    private WebUnitMaster()
    {
        mainClient = new OkHttpClient();
        int cacheSize = 10 * 1024 * 1024; //10 MB
        mCache = new Cache(MinoriApplication.getAppContext().getCacheDir(), cacheSize);
        mainClient.setCache(mCache);
    }

    public static WebUnitMaster getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (WebUnitMaster.class)
            {
                if (INSTANCE == null)
                    INSTANCE = new WebUnitMaster();
            }
        }
        return INSTANCE;
    }

    public Cache getCache()
    {
        return mCache;
    }

    public OkHttpClient getMainClient()
    {
        return mainClient;
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
    private final OkHttpClient mClient;
    private Map<String, String> mHeaders = new HashMap<String, String>(0);
    private String mUserAgent;

    public WebUnit()
    {
        mClient = new OkHttpClient();
    }

    public void setUserAgent(String mUserAgent)
    {
        this.mUserAgent = mUserAgent;
    }

    public void setCredentials(String user, String password)
    {
        String credentials = Credentials.basic(user, password);
        System.out.print("Authorization: " + credentials);
        mHeaders.put("Authorization", credentials);
    }

    public void addHeaders(String key, String value)
    {
        mHeaders.put(key, value);
    }

    public OkHttpClient getClient()
    {
        return mClient;
    }

    public void setCache(boolean enabled)
    {
        if (enabled)
            mClient.setCache(WebUnitMaster.getInstance().getCache());
        else
            mClient.setCache(null);
    }

    /**
     * Get a string object from response body of the given url. For threaded calls, use {@link #enqueueGetString}
     *
     * @param url The URL for the request
     * @return The body in a string object. Returns null on failure.
     * @throws java.io.IOException
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
     * @throws java.io.IOException
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
        String agent;
        if (mUserAgent != null) agent = mUserAgent;
        else agent = userAgent;

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .addHeader(userAgentKey, agent);

        for (Map.Entry<String, String> header : mHeaders.entrySet())
        {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }

        return requestBuilder.build();
    }

    /**
     * Enqueue a request to the given URL and receive the response body in a String object via a listener
     *
     * @param url      The URL for the request
     * @param listener Optional listener to retrieve the string object of the received response body. Check for null. This runs on the original thread that first calls the method.
     * @throws java.io.IOException
     */
    public synchronized void enqueueGetString(final String url, final WebUnitStringListener listener) throws IOException
    {
        executeAsync(url, new Callback()
        {
            @Override
            public void onFailure(Request request, IOException e)
            {
                if (listener != null) listener.onFailure();
            }

            @Override
            public void onResponse(final Response response) throws IOException
            {
                final String finalResponse = response.body().string();
                if (listener != null) listener.onFinish(finalResponse);
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
        getClient().newCall(request).enqueue(internalCallback);
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
        void onStreamReceived(InputStream inputStream);
    }

    /**
     * A simple listener interface for any WebUnit asynchronous operations
     */
    public interface WebUnitStringListener
    {
        /**
         * Fires if the request failed in some way
         */
        void onFailure();

        /**
         * Fires upon the completion of the request
         *
         * @param responseBody A string object of the response body
         */
        void onFinish(String responseBody);
    }
}
