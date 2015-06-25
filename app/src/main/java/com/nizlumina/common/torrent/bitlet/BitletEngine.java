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

import com.nizlumina.common.torrent.EngineConfig;
import com.nizlumina.common.torrent.TorrentEngine;
import com.nizlumina.common.torrent.TorrentObject;
import com.nizlumina.common.torrent.Utils;

import org.bitlet.wetorrent.Torrent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A wrapper for Bitlet pseudo thread-based implementation of Torrent. Since each {@link Torrent} extended from a Thread, we might as well utilize that.
 */
public class BitletEngine implements TorrentEngine
{
    private static final String METAFILE_INDEX_NAME = "metafile_index.dat";
    //internal map for the actual torrents
    private final Map<String, BitletObject> mTorrentsMap = new HashMap<>();
    //a sparse key-value index for identifying ID with its metafile names.
    private final Object mEngineLock = new Object();
    private final ExecutorService mExecutorService = Executors.newCachedThreadPool();
    private EngineConfig mEngineConfig;

    private boolean AUTO_START_DOWNLOAD = true;

    @Override
    public void onReceiveNewTorrentObject(final TorrentObject torrentObject)
    {
        final Runnable addRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (mEngineLock)
                {
                    final String id = torrentObject.getId();
                    final BitletObject bitletObjectInMap = mTorrentsMap.get(id);

                    if (bitletObjectInMap == null) //only proceed if not already have it
                    {
                        final File metafileDirectory = mEngineConfig.getMetafileDirectory();

                        final File initialMetafile = torrentObject.getMetafile();
                        final File finalMetafile = new File(metafileDirectory, id);
                        final boolean renamed = initialMetafile.renameTo(finalMetafile);

                        if (renamed && finalMetafile.exists() && finalMetafile.canRead())
                        {
                            final BitletObject finalBitletObject = new BitletObject();
                            finalBitletObject.setId(id);
                            finalBitletObject.setStatus(TorrentObject.Status.PAUSED);
                            finalBitletObject.setMetafile(finalMetafile);

                            final boolean successInit = finalBitletObject.initTorrent(finalMetafile, mEngineConfig.getDownloadDirectory(), mEngineConfig.getPort());
                            if (successInit)
                            {
                                getTorrentMap().put(id, finalBitletObject);
                                if (AUTO_START_DOWNLOAD)
                                    download(id);
                            }
                        }
                    }
                    else
                    {
                        //print out already in List
                    }
                }
            }
        };

        mExecutorService.submit(addRunnable);
    }

    @Override
    public void download(final String id)
    {
        try
        {
            mExecutorService.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    final BitletObject bitletObject = getTorrentMap().get(id);
                    if (bitletObject != null)
                    {
                        bitletObject.setStatus(TorrentObject.Status.DOWNLOADING);
                        final Torrent torrent = bitletObject.getTorrent();
                        try
                        {
                            torrent.startDownload();
                            final TimerTask timerTask = new TimerTask()
                            {
                                @Override
                                public void run()
                                {
                                    final boolean isValid = !torrent.isCompleted() && !torrent.isInterrupted();
                                    if (isValid)
                                    {
                                        torrent.tick();
                                    }
                                    else this.cancel();
                                }
                            };

                            final Timer timer = new Timer();
                            timer.schedule(timerTask, 1000);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void pause(final String id)
    {
        final BitletObject bitletObject = getTorrentMap().get(id);
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

                    bitletObject.setStatus(TorrentObject.Status.PAUSED);
                }
            });

        }
    }

    @Override
    public synchronized void remove(final String id)
    {
        final BitletObject bitletObject = getTorrentMap().get(id);
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

                    File metafile = bitletObject.getMetafile();
                    if (metafile.exists())
                    {
                       boolean delete = metafile.delete();
                        if(!delete){

                        }
                    }

                    getTorrentMap().remove(id);
                }
            });

        }
    }

    //This return a sparse clone copy of the BitletObject list.
    //We want to limit interaction only via IDs and abstract the internal engine implementation here.
    @Override
    public synchronized List<TorrentObject> getTorrentObjects()
    {
        final List<TorrentObject> outList = new ArrayList<>();
        for (final BitletObject bitletObject : getTorrentMap().values())
        {
            final TorrentObject torrentObject = new TorrentObject();
            torrentObject.setId(bitletObject.getId());
            torrentObject.setStatus(bitletObject.getStatus());
            outList.add(torrentObject);
        }
        return outList;
    }

    @Override
    public synchronized void startEngine()
    {

        final Runnable engineRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //to make sure nobody is using IO during state resume
                synchronized (mEngineLock)
                {
                    final File metafileDirectory = mEngineConfig.getMetafileDirectory();
                    try
                    {

                        final Map<String, BitletObject> index = Utils.loadSerializable(METAFILE_INDEX_NAME, metafileDirectory);
                        mapIndexToMetafiles(metafileDirectory, index);
                    }
                    catch (ClassCastException e)
                    {
                        e.printStackTrace();
                    }
                }
                coldResumeTorrents();
            }
        };

        mExecutorService.submit(engineRunnable);

    }

    /**
     * Check torrent status (paused or previously interrupted) and resume as necessary.
     */
    private void coldResumeTorrents()
    {
        for (final BitletObject bitletObject : getTorrentMap().values())
        {
            if (bitletObject.getStatus() == TorrentObject.Status.DOWNLOADING)
                download(bitletObject.getId());
        }
    }


    /**
     * Map from index of String - TorrentObject to a the engine main String - BitletObject map.
     * Since {@link com.nizlumina.common.torrent.bitlet.BitletObject#initTorrent(java.io.File, java.io.File, int)} is called here, make sure this method is encapsulated in a background thread.
     *
     * @param metafileDirectory Directory for metafiles sources.
     * @param index             The index to be used as reference.
     */
    private void mapIndexToMetafiles(final File metafileDirectory, final Map<String, BitletObject> index)
    {
        if (metafileDirectory.exists() && metafileDirectory.isDirectory() && metafileDirectory.canRead())
        {
            final File[] files = metafileDirectory.listFiles();
            //After viewing Data Oriented Design by Mike Acton
            final File saveDir = mEngineConfig.getDownloadDirectory();
            final int port = mEngineConfig.getPort();

            //The below works since we each metafile.torrent downloaded have their name changed to an ID made via TorrentObject.generateID().
            //We have either the choice of serializing the metafile as well to the index or map each file separately by an agreed ID.
            //Since there's no guarantee of the metafiles being changed by underlying implementations (in the future), we chose the latter and opt to not bake in a heavy dependency with the said metafiles.
            final Map<String, File> fileMap = new HashMap<>();

            for (final File file : files)
            {
                fileMap.put(file.getName(), file); //name as key.
            }

            for (final String id : index.keySet())
            {
                final File metafile = fileMap.get(id); //this check if the key in the loaded index corresponds to a metafile found from the directory
                if (metafile != null)
                {
                    final BitletObject bitletObject = index.get(id);
                    bitletObject.initTorrent(metafile, saveDir, port);
                    mTorrentsMap.put(id, bitletObject);
                }
            }
        }
    }

    @Override
    public synchronized void stopEngine()
    {
        mExecutorService.submit(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (mEngineLock)
                {
                    Utils.saveSerializable(METAFILE_INDEX_NAME, mEngineConfig.getMetafileDirectory(), mTorrentsMap);
                }
            }
        });
    }

    @Override
    public synchronized void initializeSettings(EngineConfig engineConfig)
    {
        this.mEngineConfig = engineConfig;
    }

    private Map<String, BitletObject> getTorrentMap()
    {
        return mTorrentsMap;
    }
}
