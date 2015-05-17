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

import java.io.File;
import java.io.Serializable;

/**
 * A model class that implements basic properties to be used by internal {@link com.nizlumina.common.torrent.TorrentEngine}
 */
public class TorrentObject implements Serializable
{
    private String id;
    private Status status;
    private File metafile;

    public TorrentObject() {}

    /**
     * Generate a random String with size 10.
     */
    public static String generateId()
    {
        //Pseudo imgur. YOLO.
        final int length = 10;
        final char[] randomChars = new char[length];

        for (int i = 0; i < length; ++i)
        {
            final int randomVal = (int) (Math.random() * 52);
            final char base;
            if (randomVal < 26)
                base = 'A';
            else
                base = 'a';

            randomChars[i] = (char) (base + randomVal % 26);
        }
        return String.valueOf(randomChars);

    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public File getMetafile()
    {
        return metafile;
    }

    public void setMetafile(File metafile)
    {
        this.metafile = metafile;
    }

    public enum Status
    {
        PAUSED, COMPLETED, DOWNLOADING
    }
}
