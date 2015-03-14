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

import org.bitlet.wetorrent.Metafile;
import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.disk.PlainFileSystemTorrentDisk;
import org.bitlet.wetorrent.disk.TorrentDisk;
import org.bitlet.wetorrent.peer.IncomingPeerListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
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

    //internal map for the actual torrents
    private final Map<String, Torrent> mTorrentsMap = new HashMap<>();
    private final Object mIOLock = new Object();
    private EngineConfig mEngineConfig;

    @Override
    public void download(String id)
    {
        try
        {
            getTorrentMap().get(id).startDownload();
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
        for (Torrent torrent : getTorrentMap().values())
        {
            outList.add(new BitletObject(torrent.getName(), torrent.getMetafile().getName(), null));
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
                loadIndexFromDisk(mEngineConfig.getMetafileDirectory());
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
                for (Torrent torrent : getTorrentMap().values())
                {

                }
            }
        };

        Thread engineThread = new Thread(engineRunnable, "torrent-engine-thread");
        engineThread.start();

    }

    private void mapIndexToMetafiles(final File metafileDirectory, final Map<String, TorrentObject> index)
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

            for (File metafile : files)
            {
                //Todo:Build BitLetObject here?
            }
        }
    }

    /**
     * Build a BitletObject from a metafile. Since this operation will read from disk, make sure to call it in a background thread.
     *
     * @param metafile          A .torrent file. Doesn't necessarily have to be .torrent though.
     *                          Look into {@link org.bitlet.wetorrent.Metafile} for its parsing options.
     * @param downloadDirectory Directory for the downloaded file.
     * @param port              Port for incoming connections.
     * @return A BitletObject ready to be used for downloading.
     */
    private BitletObject buildBitletObject(File metafile, File downloadDirectory, int port)
    {
        BitletObject bitletObject = new BitletObject(null, metafile.getName(), TorrentObject.Status.PAUSED);
        BufferedInputStream bufferedInputStream = null;
        try
        {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(metafile));
            Metafile metafileInstance = new Metafile(bufferedInputStream);

            TorrentDisk torrentDisk = new PlainFileSystemTorrentDisk(metafileInstance, downloadDirectory);
            torrentDisk.init();

            IncomingPeerListener peerListener = new IncomingPeerListener(port);

            Torrent torrent = new Torrent(metafileInstance, torrentDisk, peerListener);
            bitletObject.setTorrent(torrent);
            return bitletObject;

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
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


        return null;
    }

    /**
     * Populate index from disk
     */
    private Map<String, TorrentObject> loadIndexFromDisk(final File metafileDirectory)
    {
        final Map<String, TorrentObject> torrentObjectIndex = new HashMap<>();

        final File metafileIndex = new File(metafileDirectory, metafileIndexName);
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

    private Map<String, Torrent> getTorrentMap()
    {
        return mTorrentsMap;
    }
}
