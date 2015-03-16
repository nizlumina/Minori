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

import java.io.Serializable;

/**
 * A model class that implements basic properties to be used by internal {@link com.nizlumina.minori.common.torrent.TorrentEngine}
 */
public abstract class TorrentObject implements Serializable
{
    final String id;
    final String name;
    final String metafileName;
    final Status status;

    public TorrentObject(String id, String name, String metafileName, Status status)
    {
        this.id = id;
        this.name = name;
        this.metafileName = metafileName;
        this.status = status;
    }

    public String getId()
    {
        return id;
    }

    public String getMetafileName()
    {
        return metafileName;
    }

    public String getName()
    {
        return name;
    }

    public Status getStatus()
    {
        return status;
    }

    public enum Status
    {
        PAUSED, COMPLETED, DOWNLOADING
    }
}
