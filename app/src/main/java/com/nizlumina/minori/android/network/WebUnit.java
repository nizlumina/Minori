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
    final OkHttpClient okHttpClient;

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
 * Slave class for common HTTP responses. Downloading stuffs is not implemented yet with this class.
 */
public class WebUnit
{
    private static final String userAgentKey = "User-Agent";
    private static final String userAgent = "minori-android";

    public String getString(Context context, String url) throws IOException
    {
        final Response response = initRequestAndExecute(context, url);

        if (response != null && response.isSuccessful())
        {
            return response.body().string();
        }
        return null;
    }

    public void invokeOnStream(Context context, String url, StreamCallable callable) throws IOException
    {
        final Response response = initRequestAndExecute(context, url);

        if (response != null && response.isSuccessful())
        {
            callable.onStreamReceived(response.body().byteStream());
        }
    }

    private Response initRequestAndExecute(Context context, String url) throws IOException
    {
        final Request request = new Request.Builder()
                .url(url)
                .addHeader(userAgentKey, userAgent).build();
        return WebUnitMaster.getInstance(context).getClient().newCall(request).execute();
    }

    public interface StreamCallable
    {
        /**
         * This is called when the stream is fully received
         *
         * @param inputStream The full InputStream received
         */
        public void onStreamReceived(InputStream inputStream);
    }

}
