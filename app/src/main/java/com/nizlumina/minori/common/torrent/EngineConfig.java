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

package com.nizlumina.minori.common.torrent;

import java.io.File;

/**
 * A Config class to be use by {@link com.nizlumina.minori.common.torrent.TorrentEngine}.
 * This class is initialized in a Service (and utilize SharedPrefs) and passed as parameter for {@link com.nizlumina.minori.common.torrent.TorrentEngine} initialization.
 * <p/>
 * Use {@link com.nizlumina.minori.common.torrent.EngineConfig.Builder} for building it.
 */
public final class EngineConfig
{
    private int connectionCountLimit;
    private int uploadBandwithLimit;
    private int downloadBandwithLimit;
    private int maxRunningUploads;
    private int maxRunningDownloads;
    private int maxRunningTorrent;
    private File saveDirectory;
    private int port;

    public EngineConfig(int connectionCountLimit, int uploadBandwithLimit, int downloadBandwithLimit, int maxRunningUploads, int maxRunningDownloads, int maxRunningTorrent, File saveDirectory, int port)
    {
        this.connectionCountLimit = connectionCountLimit;
        this.uploadBandwithLimit = uploadBandwithLimit;
        this.downloadBandwithLimit = downloadBandwithLimit;
        this.maxRunningUploads = maxRunningUploads;
        this.maxRunningDownloads = maxRunningDownloads;
        this.maxRunningTorrent = maxRunningTorrent;
        this.saveDirectory = saveDirectory;
        this.port = port;
    }

    public EngineConfig() {}

    public int getConnectionCountLimit()
    {
        return connectionCountLimit;
    }

    public int getUploadBandwithLimit()
    {
        return uploadBandwithLimit;
    }

    public int getDownloadBandwithLimit()
    {
        return downloadBandwithLimit;
    }

    public int getMaxRunningUploads()
    {
        return maxRunningUploads;
    }

    public int getMaxRunningDownloads()
    {
        return maxRunningDownloads;
    }

    public int getMaxRunningTorrent()
    {
        return maxRunningTorrent;
    }

    public File getSaveDirectory()
    {
        return saveDirectory;
    }

    public int getPort()
    {
        return port;
    }

    public static class Builder
    {

        private int connectionCountLimit;
        private int uploadBandwithLimit;
        private int downloadBandwithLimit;
        private int maxRunningUploads;
        private int maxRunningDownloads;
        private int maxRunningTorrent;
        private File saveDirectory;
        private int port;

        public Builder setConnectionCountLimit(int connectionCountLimit)
        {
            this.connectionCountLimit = connectionCountLimit;
            return this;
        }

        public Builder setUploadBandwithLimit(int uploadBandwithLimit)
        {
            this.uploadBandwithLimit = uploadBandwithLimit;
            return this;
        }

        public Builder setDownloadBandwithLimit(int downloadBandwithLimit)
        {
            this.downloadBandwithLimit = downloadBandwithLimit;
            return this;
        }

        public Builder setMaxRunningUploads(int maxRunningUploads)
        {
            this.maxRunningUploads = maxRunningUploads;
            return this;
        }

        public Builder setMaxRunningDownloads(int maxRunningDownloads)
        {
            this.maxRunningDownloads = maxRunningDownloads;
            return this;
        }

        public Builder setMaxRunningTorrent(int maxRunningTorrent)
        {
            this.maxRunningTorrent = maxRunningTorrent;
            return this;
        }

        public Builder setSaveDirectory(File saveDirectory)
        {
            this.saveDirectory = saveDirectory;
            return this;
        }

        public Builder setPort(int port)
        {
            this.port = port;
            return this;
        }

        public EngineConfig build()
        {
            return new EngineConfig(connectionCountLimit, uploadBandwithLimit, downloadBandwithLimit, maxRunningUploads, maxRunningDownloads, maxRunningTorrent, saveDirectory, port);
        }
    }
}
