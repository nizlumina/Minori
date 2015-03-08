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

package com.nizlumina.minori.android.torrent;

import java.io.File;

/**
 * A Config class to be use by {@link com.nizlumina.minori.android.torrent.TorrentEngine}.
 * This class is initialized in a Service (and utilize SharedPrefs) and passed as parameter for {@link com.nizlumina.minori.android.torrent.TorrentEngine} initialization.
 */
public final class EngineConfig
{
    private File saveDirectory;
    private int port;

    public EngineConfig(File saveDirectory, int port)
    {
        this.saveDirectory = saveDirectory;
        this.port = port;
    }

    public File getSaveDirectory()
    {
        return saveDirectory;
    }

    public int getPort()
    {
        return port;
    }
}
