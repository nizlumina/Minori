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

package com.nizlumina.common.torrent.bitlet;

import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nizlumina.common.torrent.EngineConfig;
import com.nizlumina.common.torrent.TorrentEngine;
import com.nizlumina.common.torrent.TorrentObject;

import org.apache.commons.io.FileUtils;
import org.bitlet.wetorrent.Metafile;
import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.peer.PeersManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A wrapper for Bitlet pseudo thread-based implementation of Torrent. Since each {@link Torrent} extended from a Thread, we might as well utilize that.
 */
public class BitletEngine implements TorrentEngine
{
    private static final String METAFILE_INDEX_NAME = "metafile_index.dat";
    private static final int mScheduledPoolSize = 20;
    private static final int mTorrentTickRate = 2;
    private static boolean AUTO_START_DOWNLOAD = true;
    private final ConcurrentHashMap<String, BitletObject> mTorrentsMap = new ConcurrentHashMap<>(); //internal map for the actual torrents. Key is base64 encoded version of its the torrent infohash.
    private final Object mEngineLock = new Object();
    private ExecutorService mExecutorService;
    private ScheduledThreadPoolExecutor mScheduledExecutor;
    private EngineConfig mEngineConfig;
    private final Runnable stopEngineRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            //to make sure nobody is using IO during state resume
            synchronized (mEngineLock)
            {
                final File metafileDirectory = mEngineConfig.getMetafileDirectory();
                final HashMap<String, TorrentObject> outMap = new HashMap<>(mTorrentsMap.size());
                for (final Map.Entry<String, BitletObject> entrySet : mTorrentsMap.entrySet())
                {
                    outMap.put(entrySet.getKey(), entrySet.getValue());
                }
                Gson gson = new Gson();
                File indexFile = new File(metafileDirectory, METAFILE_INDEX_NAME);
                Type type = new TypeToken<Map<String, TorrentObject>>() {}.getType();
                String outString = gson.toJson(outMap, type);
                if (outString != null)
                {
                    try
                    {
                        FileUtils.writeStringToFile(indexFile, outString, "UTF-8");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    private Runnable noMoreTaskCallback;
    private Runnable onEngineStartedListener;
    private final Runnable startEngineRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            //to make sure nobody is using IO during state resume
            synchronized (mEngineLock)
            {
                final File metafileDirectory = mEngineConfig.getMetafileDirectory();
                if (metafileDirectory.exists() && metafileDirectory.isDirectory() && metafileDirectory.canRead())
                {
                    try
                    {
                        Gson gson = new Gson();
                        File indexFile = new File(metafileDirectory, METAFILE_INDEX_NAME);
                        if (indexFile.exists() && indexFile.canRead())
                        {
                            String jsonString = FileUtils.readFileToString(indexFile, "UTF-8");
                            Type type = new TypeToken<Map<String, TorrentObject>>() {}.getType();
                            Map<String, TorrentObject> index = gson.fromJson(jsonString, type); //load the index

                            if (index != null && index.size() > 0)
                            {
                                File saveDir = mEngineConfig.getDownloadDirectory();
                                int port = mEngineConfig.getPort();

                                //check if any files in the directory corresponds to the index
                                for (final Map.Entry<String, TorrentObject> entrySet : index.entrySet())
                                {
                                    File metafile = new File(entrySet.getValue().getMetafilePath());
                                    if (metafile.exists() && metafile.isFile() && metafile.canRead())
                                    {
                                        final BitletObject bitletObject = new BitletObject(entrySet.getValue());
                                        if (bitletObject.initTorrent(metafile, saveDir, port))
                                        {
                                            mTorrentsMap.put(entrySet.getKey(), bitletObject);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    catch (ClassCastException | IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            for (final BitletObject bitletObject : mTorrentsMap.values())
            {
                if (bitletObject.getStatus() == TorrentObject.Status.DOWNLOADING)
                    resume(bitletObject.getId());
            }
            if(onEngineStartedListener != null)
                onEngineStartedListener.run();
        }
    };

    @Override
    public void add(final TorrentObject torrentObject)
    {
        final Runnable addRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (mEngineLock)
                {
                    final File metafileRef = new File(torrentObject.getMetafilePath());
                    if (metafileRef.exists() && metafileRef.isFile() && metafileRef.canRead())
                    {
                        String key = null;
                        Metafile metafile = null;
                        BufferedInputStream bufferedInputStream = null;
                        try
                        {
                            bufferedInputStream = new BufferedInputStream(new FileInputStream(metafileRef));
                            metafile = new Metafile(bufferedInputStream);
                            key = generateTorrentId(metafile.getInfoSha1()); //important part here. ID is originally null. The actual ID (and eventually becoming the hashmap key) is initialized via encoding the torrent infosha in Base64.
                        }
                        catch (IOException | NoSuchAlgorithmException e)
                        {
                            e.printStackTrace();
                        }
                        finally
                        {
                            if (bufferedInputStream != null)
                            {
                                try
                                {
                                    bufferedInputStream.close();
                                }
                                catch (IOException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (key != null) //metafile != null is implicitly true if key isn't null.
                        {
                            if (!mTorrentsMap.containsKey(key))
                            {
                                BitletObject bitletObject = new BitletObject();
                                bitletObject.setId(key).setStatus(TorrentObject.Status.PAUSED).setMetafilePath(torrentObject.getMetafilePath());
                                boolean successInit = bitletObject.initTorrent(metafile, mEngineConfig.getDownloadDirectory(), mEngineConfig.getPort());
                                if (successInit)
                                {
                                    mTorrentsMap.put(key, bitletObject);
                                    if (AUTO_START_DOWNLOAD)
                                        resume(key);
                                }
                            }
                            else
                            {
                                //TODO: Emit error message for already added
                            }
                        }
                    }
                }
            }
        };

        mExecutorService.submit(addRunnable);
    }

    private String generateTorrentId(byte[] infoSha1)
    {
        return Base64.encodeToString(infoSha1, Base64.DEFAULT);
    }

    @Override
    public void resume(final String id)
    {
        final BitletObject bitletObject = mTorrentsMap.get(id);
        if (bitletObject != null && bitletObject.getStatus() != TorrentObject.Status.DOWNLOADING)
        {
            bitletObject.setStatus(TorrentObject.Status.DOWNLOADING);
            final Torrent torrent = bitletObject.getTorrent();
            final Runnable runnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        torrent.startDownload();
                        final boolean isValid = !torrent.isCompleted() && !torrent.isInterrupted() && bitletObject.getStatus() == TorrentObject.Status.DOWNLOADING;
                        if (isValid)
                        {
                            torrent.tick();

                            final PeersManager peersManager = torrent.getPeersManager();
                            final TorrentObject.TorrentListener listener = bitletObject.getListener();
                            if (listener != null)
                            {
                                listener.onUpdate(peersManager.getDownloaded(), peersManager.getDownloaded(), torrent.getTorrentDisk().getCompleted(), torrent.getMetafile().getLength(), peersManager.getActivePeersNumber(), peersManager.getSeedersNumber());
                            }
                        }
                        if (torrent.isCompleted())
                        {
                            bitletObject.setStatus(TorrentObject.Status.COMPLETED);
                            if (!isAnyTorrentDownloading())
                            {
                                if (noMoreTaskCallback != null)
                                    noMoreTaskCallback.run();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            mScheduledExecutor.scheduleAtFixedRate(runnable, 0, mTorrentTickRate, TimeUnit.SECONDS);
        }
    }

    @Override
    public synchronized void pause(final String id)
    {
        final BitletObject bitletObject = mTorrentsMap.get(id);
        if (bitletObject != null)
        {
            Torrent torrent = bitletObject.getTorrent();
            if (torrent != null)
                torrent.stopDownload();
            bitletObject.setStatus(TorrentObject.Status.PAUSED);
        }
    }

    @Override
    public synchronized void remove(final String id)
    {
        final BitletObject bitletObject = mTorrentsMap.get(id);
        if (bitletObject != null)
        {
            mExecutorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    Torrent torrent = bitletObject.getTorrent();
                    if (torrent != null)
                        torrent.stopDownload();

                    File metafile = new File(bitletObject.getMetafilePath());
                    if (metafile.exists())
                    {
                        FileUtils.deleteQuietly(metafile);
                    }
                    mTorrentsMap.remove(id);
                }
            });

        }
    }

    @Override
    public Collection<String> getTorrentIds()
    {
        return mTorrentsMap.keySet();
    }

    @Override
    public boolean isAnyTorrentDownloading()
    {
        for (Map.Entry<String, BitletObject> entry : mTorrentsMap.entrySet())
        {
            if (entry.getValue().getStatus() == TorrentObject.Status.DOWNLOADING)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getTorrentName(String id)
    {
        return mTorrentsMap.get(id).getTorrent().getMetafile().getName();
    }

    @Override
    public String getTorrentDetails(String id)
    {
        return null;
    }

    @Override
    public void setListener(String id, TorrentObject.TorrentListener listener)
    {
        mTorrentsMap.get(id).setListener(listener);
    }

    @Override
    public void setOnNoMoreRunningTaskListener(Runnable listener)
    {
        this.noMoreTaskCallback = listener;
    }

    @Override
    public void setOnEngineStartedListener(Runnable listener)
    {
        this.onEngineStartedListener = listener;
    }

    @Override
    public synchronized void startEngine()
    {
        mExecutorService = Executors.newCachedThreadPool();
        mScheduledExecutor = new ScheduledThreadPoolExecutor(mScheduledPoolSize);
        mScheduledExecutor.setKeepAliveTime(1, TimeUnit.SECONDS);
        mScheduledExecutor.allowCoreThreadTimeOut(true);
        mExecutorService.submit(startEngineRunnable);
    }

    @Override
    public synchronized void stopEngine()
    {
        mScheduledExecutor.shutdown();
        mExecutorService.submit(stopEngineRunnable);
        mExecutorService.shutdown();
    }

    @Override
    public synchronized void initializeSettings(EngineConfig engineConfig)
    {
        this.mEngineConfig = engineConfig;
    }
}
