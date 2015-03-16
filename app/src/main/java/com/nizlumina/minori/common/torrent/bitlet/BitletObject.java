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
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class BitletObject extends TorrentObject
{
    private transient Torrent torrent;

    private BitletObject(String id, String name, String metafileName, Status status)
    {
        super(id, name, metafileName, status);
    }

    /**
     * Build a BitletObject from a metafile. Since this operation will read from disk, make sure to call it in a background thread.
     *
     * @param metafile          A .torrent file. Doesn't necessarily have to be .torrent though.
     *                          Look into {@link org.bitlet.wetorrent.Metafile} for its parsing options.
     * @param downloadDirectory Directory for the downloaded file.
     * @param port              Port for incoming connections.
     * @return A BitletObject ready to be used for downloading. Returns null on failure.
     */
    public static BitletObject buildBitletObject(File metafile, File downloadDirectory, int port)
    {
        String id = generateId();
        BitletObject bitletObject = new BitletObject(id, null, metafile.getName(), TorrentObject.Status.PAUSED);
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

    private static String generateId()
    {
        //Todo: What kind of ID would be right for this situation?
        return null;
    }

    public Torrent getTorrent()
    {
        return torrent;
    }

    public void setTorrent(Torrent torrent)
    {
        this.torrent = torrent;
    }

    public BitletObject getSparseClone()
    {
        return new BitletObject(getId(), getName(), getMetafileName(), getStatus());
    }
}
