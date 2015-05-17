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

import com.nizlumina.common.torrent.TorrentObject;

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
    private transient Torrent mTorrent;

    /**
     * Init the {@link Torrent} property for this object. Metafile will be read and decoded, port listener setup, and the disk will be ready for downloading w.r.t. the passed downloadDirectory.
     *
     * @param metafile          Metafile to be read as inputstream
     * @param downloadDirectory The directory for this torrent download
     * @param port              The port where this torrent will use for downloading
     * @return true on succesful init. False on any initialization exception.
     */
    public boolean initTorrent(File metafile, File downloadDirectory, int port)
    {
        boolean success = false;
        if (metafile.exists() && metafile.canRead())
        {
            BufferedInputStream bufferedInputStream = null;
            try
            {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(metafile));
                final Metafile metafileInstance = new Metafile(bufferedInputStream);

                final TorrentDisk torrentDisk = new PlainFileSystemTorrentDisk(metafileInstance, downloadDirectory);
                torrentDisk.init();

                final IncomingPeerListener peerListener = new IncomingPeerListener(port);

                final Torrent torrent = new Torrent(metafileInstance, torrentDisk, peerListener);

                this.setTorrent(torrent); //important setter
                success = true;
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
                        success = false;
                    }
                }
            }
        }

        return success;
    }


    public Torrent getTorrent()
    {
        return mTorrent;
    }

    public void setTorrent(Torrent torrent)
    {
        this.mTorrent = torrent;
    }
}
