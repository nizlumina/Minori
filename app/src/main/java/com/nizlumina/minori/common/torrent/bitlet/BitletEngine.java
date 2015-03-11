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

package com.nizlumina.minori.common.torrent.bitlet;

import com.nizlumina.minori.common.torrent.EngineConfig;
import com.nizlumina.minori.common.torrent.TorrentEngine;
import com.nizlumina.minori.common.torrent.TorrentObject;

import org.bitlet.wetorrent.Torrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for bitlet Torrent pseudo thread-based implementation. Each {@link org.bitlet.wetorrent.Torrent} extended from a Thread, so we might as well utilize that.
 */
public class BitletEngine implements TorrentEngine
{
    private static final String metafileIndexName = "metafile_index.dat";
    //a sparse key-value index for identifying ID with its metafile names.
    private final Map<String, TorrentObject> mTorrentObjectIndex = new HashMap<>();
    //internal map for the actual torrents
    private final Map<String, Torrent> mTorrentsMap = new HashMap<>();
    private final Object mIOLock = new Object();
    private EngineConfig mEngineConfig;

    @Override
    public void download(String id)
    {
        try
        {
            mTorrentsMap.get(id).startDownload();
            //tick method etc. wrapped under a thread manager
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void pause(String id)
    {

    }

    @Override
    public synchronized void remove(String id)
    {

    }

    @Override
    public synchronized List<TorrentObject> getTorrents()
    {
        List<TorrentObject> outList = new ArrayList<>();
        for (Torrent torrent : mTorrentsMap.values())
        {
            outList.add(new TorrentObject(torrent.getName(), torrent.getMetafile().getName(), null));
        }
        return outList;
    }

    @Override
    public synchronized void startEngine()
    {
        //check cache dir for metafiles
        Runnable loadRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                loadMetafileIndex(mEngineConfig.getMetafileDirectory());
            }
        };

        Thread ioThread = new Thread(loadRunnable, "torrent-metafile-load");
        ioThread.start();

        Runnable engineRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (mIOLock)
                {

                }

                //check torrent status (paused/running but exited)
                for (Torrent torrent : mTorrentsMap.values())
                {

                }
            }
        };

        Thread engineThread = new Thread(engineRunnable, "Torrent-Engine-Thread");
        engineThread.start();

    }

    private void indexToMap()
    {
        for (TorrentObject torrentObject : mTorrentObjectIndex.values())
        {

        }
    }

    /**
     * Populate index from disk
     */
    private void loadMetafileIndex(final File metafileDirectory)
    {
        File metafileIndex = new File(metafileDirectory, metafileIndexName);
        synchronized (mIOLock)
        {
            if (metafileIndex.exists())
            {
                FileInputStream fileInputStream = null;
                ObjectInputStream objectInputStream = null;
                try
                {
                    fileInputStream = new FileInputStream(metafileIndex);
                    objectInputStream = new ObjectInputStream(fileInputStream);
                    //noinspection unchecked
                    Map<String, TorrentObject> torrentObjects = (Map<String, TorrentObject>) objectInputStream.readObject();

                    mTorrentObjectIndex.putAll(torrentObjects);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (fileInputStream != null && objectInputStream != null)
                    {
                        try
                        {
                            objectInputStream.close();
                            fileInputStream.close();
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Save index to disk
     */
    private void saveMetafileIndex(final File metafileDirectory)
    {
        File metafileIndex = new File(metafileDirectory, metafileIndexName);
        synchronized (mIOLock)
        {
            FileOutputStream fileOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try
            {
                fileOutputStream = new FileOutputStream(metafileIndex);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(mTorrentObjectIndex);

                objectOutputStream.close();
                fileOutputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (fileOutputStream != null && objectOutputStream != null)
                {
                    try
                    {
                        objectOutputStream.close();
                        fileOutputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public synchronized void stopEngine()
    {
        saveMetafileIndex(mEngineConfig.getMetafileDirectory());
    }

    @Override
    public synchronized void initializeSettings(EngineConfig engineConfig)
    {
        this.mEngineConfig = engineConfig;
    }
}
