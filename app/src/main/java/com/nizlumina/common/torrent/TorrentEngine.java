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

package com.nizlumina.common.torrent;

import java.util.List;

/**
 * An interface for implementating a generic TorrentEngine.
 * Each method is called from the main thread hence concrete implementation
 * must delegate method calls into its own thread managers.
 */
public interface TorrentEngine
{
    /**
     * Called after the framework fully received a raw metafile/magnet/etc. The TorrentObject is immediately created with a generated id plus references to the raw files/sources set.
     * In a sense, this method hands off the raw source to the engine and expect its implementation to fully initialize it.
     *
     * @param torrentObject A TorrentObject created from any valid source (metafile/magnet/etc).
     */
    void onReceiveNewTorrentObject(TorrentObject torrentObject);

    void download(String id);

    void pause(String id);

    void remove(String id);

    List<TorrentObject> getTorrentObjects();

    void startEngine();

    void stopEngine();

    void initializeSettings(EngineConfig engineConfig);
}
