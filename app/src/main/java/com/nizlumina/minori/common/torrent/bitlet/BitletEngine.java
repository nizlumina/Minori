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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A wrapper for Bitlet pseudo thread-based implementation of Torrent. Since each {@link org.bitlet.wetorrent.Torrent} extended from a Thread, we might as well utilize that.
 */
public class BitletEngine implements TorrentEngine
{
    private static final String metafileIndexName = "metafile_index.dat";
    //a sparse key-value index for identifying ID with its metafile names.

    //internal map for the actual torrents
    private final Map<String, BitletObject> mTorrentsMap = new HashMap<>();
    private final Object mIOLock = new Object();
    private EngineConfig mEngineConfig;

    public EngineConfig getEngineConfig()
    {
        return mEngineConfig;
    }

    @Override
    public void onReceiveNewTorrentObject(TorrentObject torrentObject)
    {

    }

    @Override
    public void download(String id)
    {
        try
        {
            getTorrentMap().get(id).getTorrent().startDownload();
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

    //This return a sparse clone copy of the BitletObject list.
    //We want to limit interaction only via IDs and abstract the internal engine implementation here.
    @Override
    public synchronized List<TorrentObject> getTorrentObjects()
    {
        List<TorrentObject> outList = new ArrayList<>();
        for (BitletObject bitletObject : getTorrentMap().values())
        {
            outList.add(bitletObject.getSparseClone());
        }
        return outList;
    }

    @Override
    public synchronized void startEngine()
    {

        Runnable engineRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //to make sure nobody is using IO during state resume
                synchronized (mIOLock)
                {
                    final File metafileDirectory = mEngineConfig.getMetafileDirectory();
                    loadIndexFromDisk(metafileDirectory);
                    mapIndexToMetafiles(metafileDirectory, getTorrentMap());
                }

                //check torrent status (paused/running but exited)
                for (BitletObject bitletObject : getTorrentMap().values())
                {
                    final boolean isDownloading = bitletObject.getStatus() == TorrentObject.Status.DOWNLOADING;
                    final Torrent torrent = bitletObject.getTorrent();
                    if (torrent != null && !torrent.isCompleted() && isDownloading)
                    {
                        try
                        {
                            torrent.startDownload();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        Thread engineThread = new Thread(engineRunnable, "torrent-engine-thread");
        engineThread.start();

    }

    private void mapIndexToMetafiles(final File metafileDirectory, final Map<String, BitletObject> bitletObjectMap)
    {
        if (metafileDirectory.exists() && metafileDirectory.isDirectory() && metafileDirectory.canRead())
        {
            File[] files = metafileDirectory.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String filename)
                {
                    return filename.endsWith(".torrent");
                }
            });

            //After viewing Data Oriented Design by Mike Acton
            File saveDir = getEngineConfig().getSaveDirectory();
            int port = getEngineConfig().getPort();
            for (File metafile : files)
            {
                //Todo:Build BitLetObject here?
                BitletObject bitletObject = BitletObject.buildBitletObject(metafile, saveDir, port);

                if (bitletObject != null)
                {
                    bitletObjectMap.put(bitletObject.getName(), bitletObject);
                }
            }
        }
    }

    /**
     * Populate index from disk. This method must be encapsulated with a synchronized IO access
     * @param metafileDirectory
     * @return A map of String (ID) and TorrentObjects.
     */
    private Map<String, TorrentObject> loadIndexFromDisk(final File metafileDirectory)
    {
        final Map<String, TorrentObject> torrentObjectIndex = new HashMap<>();

        final File metafileIndex = new File(metafileDirectory, metafileIndexName);
        if (metafileIndex.exists())
        {
            FileInputStream fileInputStream = null;
            ObjectInputStream objectInputStream = null;
            try
            {
                fileInputStream = new FileInputStream(metafileIndex);
                objectInputStream = new ObjectInputStream(fileInputStream);
                //noinspection unchecked
                final Map<String, TorrentObject> torrentObjects = (Map<String, TorrentObject>) objectInputStream.readObject();

                torrentObjectIndex.putAll(torrentObjects);
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

        return torrentObjectIndex;
    }

    /**
     * Save index to disk
     */
    private boolean saveIndexToDisk(final File metafileDirectory, Map<String, TorrentObject> torrentObjectIndex)
    {
        boolean success = false;
        final File metafileIndex = new File(metafileDirectory, metafileIndexName);
        synchronized (mIOLock)
        {
            FileOutputStream fileOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            try
            {
                fileOutputStream = new FileOutputStream(metafileIndex);
                objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(torrentObjectIndex);
                success = true;
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
        return success;
    }

    @Override
    public synchronized void stopEngine()
    {
        //saveIndexToDisk(mEngineConfig.getMetafileDirectory());
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
