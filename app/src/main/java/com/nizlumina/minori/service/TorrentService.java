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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.nizlumina.common.torrent.EngineConfig;
import com.nizlumina.common.torrent.TorrentEngine;
import com.nizlumina.common.torrent.bitlet.BitletEngine;

/**
 * Handles Torrent downloads.
 * We'll try to follow Flud interaction style (users must explicitly Shutdown instead of fully close on pressing back, unless all torrents are finished).
 * <p/>
 * Intent must specifies metafile location and name.
 * <p/>
 * Each {@link com.nizlumina.common.torrent.TorrentEngine} concrete implementation must handle its own threading task.
 * Contrary to popular Service implementations in Android, since we are highly likely to use different torrent library in the future, we delegate threading implementation within the libraries itself and controls any thread management from a TorrentEngine concrete interface.
 * <p/>
 * Currently each calls to the service is strictly from the main thread (since they are fairly light).
 */
public class TorrentService extends Service
{
    private static final String ACTION_STARTSERVICE = TorrentService.class.getSimpleName() + "$START_SERVICE";
    private final TorrentEngine mTorrentEngine = new BitletEngine();
    private final IBinder mServiceBinder = new TorrentServiceBinder(mTorrentEngine);

    public static void startService(Context context)
    {
        context.startService(new Intent(context, TorrentService.class).setAction(ACTION_STARTSERVICE));
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        log("OC");
        initEngine();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        log("OSM");
        handleIntent(intent);
        return Service.START_STICKY;
    }

    private void handleIntent(Intent intent)
    {
        if (intent.getAction().equals(ACTION_STARTSERVICE))
        {

        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mTorrentEngine.stopEngine();
        log("OD");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        log("OB");
        return mServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        log("UB");
        if (!mTorrentEngine.isAnyTorrentDownloading())
            stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent)
    {
        super.onRebind(intent);
        log("RB");
    }

    private void log(String s)
    {
        Log.v(getClass().getSimpleName(), s);
    }

    private void initEngine()
    {

        final EngineConfig engineConfig = new EngineConfig.Builder()
                .setMetafileDirectory(getCacheDir())
                .setSaveDirectory(getFilesDir())
                .setPort(6868)
                .build();

        //Concrete implementations. Enums for choosing engine might be implemented later as well.
        mTorrentEngine.initializeSettings(engineConfig);
        mTorrentEngine.setOnNoMoreRunningTaskListener(new Runnable()
        {
            @Override
            public void run()
            {
                stopSelf();
            }
        });

        mTorrentEngine.setOnEngineStartedListener(new Runnable()
        {
            @Override
            public void run()
            {
                if (!mTorrentEngine.isAnyTorrentDownloading())
                    stopSelf();
            }
        });
        //Start as soon as possible
        mTorrentEngine.startEngine();
    }

    private Intent makeServiceIntent()
    {
        return new Intent(getApplicationContext(), TorrentService.class).setAction("START_SERVICE");
    }

    /**
     * A Binder class following Android docs
     */
    public static class TorrentServiceBinder extends Binder
    {
        private final TorrentEngine torrentEngine;

        public TorrentServiceBinder(TorrentEngine torrentEngine)
        {
            this.torrentEngine = torrentEngine;
        }

        public TorrentEngine getTorrentEngine()
        {
            return torrentEngine;
        }

    }
}
