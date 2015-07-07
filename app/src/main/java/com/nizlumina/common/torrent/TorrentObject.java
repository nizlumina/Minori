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

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.ref.WeakReference;
import java.util.Random;

/**
 * A model class that implements basic properties to be used by internal {@link com.nizlumina.common.torrent.TorrentEngine}
 */
public class TorrentObject implements Parcelable
{
    public static final Parcelable.Creator<TorrentObject> CREATOR = new Parcelable.Creator<TorrentObject>()
    {
        public TorrentObject createFromParcel(Parcel source) {return new TorrentObject(source);}

        public TorrentObject[] newArray(int size) {return new TorrentObject[size];}
    };
    private String id;
    private Status status;
    private String metafilePath;
    private WeakReference<TorrentListener> listenerWeakRef;

    public TorrentObject() {}

    protected TorrentObject(Parcel in)
    {
        this.id = in.readString();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : Status.values()[tmpStatus];
        this.metafilePath = in.readString();
    }

    /**
     * Util method to generate a random String id with size 10. It may or may not be used depending on the engine/subclass id implementation.
     */
    private static String generateId()
    {
        //Pseudo imgur. YOLO.
        final int length = 10;
        final char[] randomChars = new char[length];
        Random random = new Random(42);
        for (int i = 0; i < length; ++i)
        {
            final int randomVal = random.nextInt() * 52;
            final char base;
            if (randomVal < 26)
                base = 'A';
            else
                base = 'a';

            randomChars[i] = (char) (base + randomVal % 26);
        }
        return String.valueOf(randomChars);

    }

    public String getMetafilePath()
    {
        return metafilePath;
    }

    public TorrentObject setMetafilePath(String metafilePath)
    {
        this.metafilePath = metafilePath;
        return this;
    }

    public TorrentListener getListener()
    {
        return listenerWeakRef.get();
    }

    public TorrentObject setListener(TorrentListener listener)
    {
        this.listenerWeakRef = new WeakReference<>(listener);
        return this;
    }

    public String getId()
    {
        return id;
    }

    public TorrentObject setId(String id)
    {
        this.id = id;
        return this;
    }

    public Status getStatus()
    {
        return status;
    }

    public TorrentObject setStatus(Status status)
    {
        this.status = status;
        return this;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.id);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeString(this.metafilePath);
    }

    public enum Status
    {
        PAUSED, COMPLETED, DOWNLOADING
    }

    public interface TorrentListener
    {
        void onUpdate(long downloaded, long uploaded, long completedBytes, long size, int activePeersNumber, int seedersNumber);
    }
}
