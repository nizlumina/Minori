/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Unless specified, permission is hereby granted, free of charge, to any person obtaining a copy of nyaaEntry software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and nyaaEntry permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.android.wrapper;

import android.os.Parcel;
import android.os.Parcelable;

import com.nizlumina.minori.core.Nyaa.NyaaEntry;

public class ParcelableNyaaEntry implements Parcelable
{
    public static final Parcelable.Creator<ParcelableNyaaEntry> CREATOR = new Parcelable.Creator<ParcelableNyaaEntry>()
    {
        public ParcelableNyaaEntry createFromParcel(Parcel source) {return new ParcelableNyaaEntry(source);}

        public ParcelableNyaaEntry[] newArray(int size) {return new ParcelableNyaaEntry[size];}
    };
    private NyaaEntry nyaaEntry;

    public ParcelableNyaaEntry(NyaaEntry nyaaEntry)
    {
        this.nyaaEntry = nyaaEntry;
    }

    private ParcelableNyaaEntry(Parcel in)
    {
        nyaaEntry = new NyaaEntry();
        nyaaEntry.id = in.readInt();
        nyaaEntry.currentEpisode = in.readInt();
        nyaaEntry.failToParse = in.readByte() != 0;
        nyaaEntry.title = in.readString();
        nyaaEntry.rawTitle = in.readString();
        nyaaEntry.fansub = in.readString();
        nyaaEntry.episodeString = in.readString();
        nyaaEntry.fileType = in.readString();
        nyaaEntry.resolutionString = in.readString();
        nyaaEntry.hash = in.readString();
        nyaaEntry.quality = in.readString();
        nyaaEntry.torrentLink = in.readString();
        nyaaEntry.description = in.readString();
        nyaaEntry.pubDate = in.readString();
        nyaaEntry.extras = in.readString();
        int tmpTrustCategory = in.readInt();
        nyaaEntry.trustCategory = tmpTrustCategory == -1 ? null : NyaaEntry.Trust.values()[tmpTrustCategory];
        int tmpResolution = in.readInt();
        nyaaEntry.resolution = tmpResolution == -1 ? null : NyaaEntry.Resolution.values()[tmpResolution];
        int tmpCategory = in.readInt();
        nyaaEntry.category = tmpCategory == -1 ? null : NyaaEntry.Category.values()[tmpCategory];
        nyaaEntry.markedForDownload = in.readByte() != 0;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(nyaaEntry.id);
        dest.writeInt(nyaaEntry.currentEpisode);
        dest.writeByte(nyaaEntry.failToParse ? (byte) 1 : (byte) 0);
        dest.writeString(nyaaEntry.title);
        dest.writeString(nyaaEntry.rawTitle);
        dest.writeString(nyaaEntry.fansub);
        dest.writeString(nyaaEntry.episodeString);
        dest.writeString(nyaaEntry.fileType);
        dest.writeString(nyaaEntry.resolutionString);
        dest.writeString(nyaaEntry.hash);
        dest.writeString(nyaaEntry.quality);
        dest.writeString(nyaaEntry.torrentLink);
        dest.writeString(nyaaEntry.description);
        dest.writeString(nyaaEntry.pubDate);
        dest.writeString(nyaaEntry.extras);
        dest.writeInt(nyaaEntry.trustCategory == null ? -1 : nyaaEntry.trustCategory.ordinal());
        dest.writeInt(nyaaEntry.resolution == null ? -1 : nyaaEntry.resolution.ordinal());
        dest.writeInt(nyaaEntry.category == null ? -1 : nyaaEntry.category.ordinal());
        dest.writeByte(nyaaEntry.markedForDownload ? (byte) 1 : (byte) 0);
    }

    public NyaaEntry getNyaaEntry()
    {
        return nyaaEntry;
    }

    public void setNyaaEntry(NyaaEntry nyaaEntry)
    {
        this.nyaaEntry = nyaaEntry;
    }
}
