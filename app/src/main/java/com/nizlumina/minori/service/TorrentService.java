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
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.nizlumina.common.torrent.EngineConfig;
import com.nizlumina.common.torrent.TorrentEngine;
import com.nizlumina.common.torrent.TorrentObject;
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
    private final TorrentEngine mTorrentEngine = new BitletEngine();
    private final IBinder mServiceBinder = new TorrentServiceBinder(mTorrentEngine);

    @Override
    public IBinder onBind(Intent intent)
    {
        return mServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Bundle bundle = intent.getExtras();
        if (bundle != null)
        {
            handleIntent(bundle);
        }
        return Service.START_STICKY;
    }

    private void handleIntent(Bundle bundle)
    {
        final TorrentObject torrentObject = (TorrentObject) bundle.getSerializable(TorrentObject.class.getName());
        if (torrentObject != null)
        {
            mTorrentEngine.onReceiveNewTorrentObject(torrentObject);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mTorrentEngine.stopEngine();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        initEngine();
    }

    /**
     * Init engine in a background thread.
     */
    private void initEngine()
    {

        final EngineConfig engineConfig = new EngineConfig.Builder()
                .setMetafileDirectory(getCacheDir())
                .setSaveDirectory(getFilesDir())
                .setPort(6868)
                .build();

        //Concrete implementations. Enums for choosing engine might be implemented later as well.
        mTorrentEngine.initializeSettings(engineConfig);

        //Start as soon as possible
        mTorrentEngine.startEngine();
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
