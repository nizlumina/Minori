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

package com.nizlumina.minori.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieHandler;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NetworkService extends IntentService
{

    private static final String ACTION_SINGLE_NETWORKCALL = NetworkService.class.getName() + "$ACTION_SINGLE_NETWORKCALL";
    private static final String IKEY_URL = "IKEY_URL";
    private static final String IKEY_ID = "IKEY_ID";
    private static final int DEFAULT_NON_ID = -42;
    private Cache cache;

    public NetworkService()
    {
        super("NetworkService");
    }

    public NetworkService(String threadName)
    {
        super(threadName);
    }

    public static void startRequest(@NonNull Context context, int requestId, @NonNull String url, BroadcastReceiver receiver)
    {
        Intent intent = new Intent(context, NetworkService.class);
        intent.setAction(ACTION_SINGLE_NETWORKCALL);
        intent.putExtra(IKEY_ID, requestId);
        intent.putExtra(IKEY_URL, url);
        context.startService(intent);
    }

    public static void startRequest(@NonNull Context context, @NonNull String[] urls, BroadcastReceiver receiver)
    {

    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String action = intent.getAction();
            if (action != null && action.equals(ACTION_SINGLE_NETWORKCALL))
            {
                String url = intent.getStringExtra(IKEY_URL);
                int id = intent.getIntExtra(IKEY_ID, DEFAULT_NON_ID);
                if (url != null && id != DEFAULT_NON_ID)
                {
                    final OkHttpClient client = new OkHttpClient();
                    if (cache == null)
                    {
                        cache = new Cache(getCacheDir(), Config.CACHE_SIZE);
                    }
                    client.setCache(cache);
                    client.setCookieHandler(CookieHandler.getDefault());
                    final Request request = new Request.Builder().addHeader(Config.HEADERKEY_USER_AGENT, Config.USER_AGENT).get().url(url).build();
                    String result = null;
                    try
                    {
                        Response response = client.newCall(request).execute();
                        if (response != null && response.isSuccessful())
                        {
                            result = response.body().string();
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        final Intent out = BroadcastReceiver.getFinishIntent(id, result);
                        LocalBroadcastManager.getInstance(NetworkService.this).sendBroadcast(out);
                    }
                }
            }
        }
    }

    private static class Config
    {
        private static final String HEADERKEY_USER_AGENT = "User-Agent";
        private static final String USER_AGENT = "minori-android";
        private static final int CACHE_SIZE = 10 * 1024 * 1024;
    }

    public static abstract class BroadcastReceiver extends android.content.BroadcastReceiver
    {
        private static final String FULLNAME = BroadcastReceiver.class.getName();
        private static final String ACTION_DOWNLOAD_COMPLETED = FULLNAME + "$completed";
        private static final String IKEY_COMPLETEDSTRING = FULLNAME + "$result";
        private static final String ACTION_NETWORK_PROGRESS = FULLNAME + "$progress";
        private static final String IKEY_PROGRESSINT = FULLNAME + "$progressint";
        private static final String IKEY_REQUESTID = FULLNAME + "$requestID";

        public static Intent getFinishIntent(int id, String finishedResponse)
        {
            Intent outIntent = new Intent(ACTION_DOWNLOAD_COMPLETED);
            outIntent.putExtra(IKEY_REQUESTID, id);
            outIntent.putExtra(IKEY_COMPLETEDSTRING, finishedResponse);
            return outIntent;
        }

        public static Intent getProgressUpdateIntent(int id, int progress)
        {
            final Intent outIntent = new Intent(ACTION_NETWORK_PROGRESS);
            outIntent.putExtra(IKEY_REQUESTID, id);
            outIntent.putExtra(IKEY_PROGRESSINT, progress);
            return outIntent;
        }

        public static IntentFilter getIntentFilter()
        {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DOWNLOAD_COMPLETED);
            filter.addAction(ACTION_NETWORK_PROGRESS);
            return filter;
        }

        /**
         * If you override this class onReceive(), make sure to call super.onReceive(Context, Intent).
         *
         * @param context {@inheritDoc}
         * @param intent  {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action != null)
            {
                int requestId = intent.getIntExtra(IKEY_REQUESTID, -42);
                if (requestId != DEFAULT_NON_ID)
                {
                    if (action.equals(ACTION_DOWNLOAD_COMPLETED))
                    {
                        onFinish(requestId, intent.getStringExtra(IKEY_COMPLETEDSTRING));
                    }
                    else if (action.equals(ACTION_NETWORK_PROGRESS))
                    {
                        onReceiveProgress(requestId, intent.getIntExtra(IKEY_PROGRESSINT, 0));
                    }
                }
            }
        }

        /**
         * Called when the service publish progress updates.
         *
         * @param progress Integer progress (0 to 100).
         */
        public abstract void onReceiveProgress(int requestId, int progress);

        /**
         * Broadcasted when the service finished.
         *
         * @param result The response result. Nullable.
         */
        public abstract void onFinish(int requestId, @Nullable String result);
    }


}
