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
    private NyaaEntry mNyaaEntry;

    public ParcelableNyaaEntry(NyaaEntry nyaaEntry)
    {
        this.mNyaaEntry = nyaaEntry;
    }

    private ParcelableNyaaEntry(Parcel in)
    {
        mNyaaEntry = new NyaaEntry();
        mNyaaEntry.id = in.readInt();
        mNyaaEntry.currentEpisode = in.readInt();
        mNyaaEntry.failToParse = in.readByte() != 0;
        mNyaaEntry.title = in.readString();
        mNyaaEntry.rawTitle = in.readString();
        mNyaaEntry.fansub = in.readString();
        mNyaaEntry.episodeString = in.readString();
        mNyaaEntry.fileType = in.readString();
        mNyaaEntry.resolutionString = in.readString();
        mNyaaEntry.hash = in.readString();
        mNyaaEntry.quality = in.readString();
        mNyaaEntry.torrentLink = in.readString();
        mNyaaEntry.description = in.readString();
        mNyaaEntry.pubDate = in.readString();
        mNyaaEntry.extras = in.readString();
        int tmpTrustCategory = in.readInt();
        mNyaaEntry.trustCategory = tmpTrustCategory == -1 ? null : NyaaEntry.Trust.values()[tmpTrustCategory];
        int tmpResolution = in.readInt();
        mNyaaEntry.resolution = tmpResolution == -1 ? null : NyaaEntry.Resolution.values()[tmpResolution];
        int tmpCategory = in.readInt();
        mNyaaEntry.category = tmpCategory == -1 ? null : NyaaEntry.Category.values()[tmpCategory];
        mNyaaEntry.markedForDownload = in.readByte() != 0;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mNyaaEntry.id);
        dest.writeInt(mNyaaEntry.currentEpisode);
        dest.writeByte(mNyaaEntry.failToParse ? (byte) 1 : (byte) 0);
        dest.writeString(mNyaaEntry.title);
        dest.writeString(mNyaaEntry.rawTitle);
        dest.writeString(mNyaaEntry.fansub);
        dest.writeString(mNyaaEntry.episodeString);
        dest.writeString(mNyaaEntry.fileType);
        dest.writeString(mNyaaEntry.resolutionString);
        dest.writeString(mNyaaEntry.hash);
        dest.writeString(mNyaaEntry.quality);
        dest.writeString(mNyaaEntry.torrentLink);
        dest.writeString(mNyaaEntry.description);
        dest.writeString(mNyaaEntry.pubDate);
        dest.writeString(mNyaaEntry.extras);
        dest.writeInt(mNyaaEntry.trustCategory == null ? -1 : mNyaaEntry.trustCategory.ordinal());
        dest.writeInt(mNyaaEntry.resolution == null ? -1 : mNyaaEntry.resolution.ordinal());
        dest.writeInt(mNyaaEntry.category == null ? -1 : mNyaaEntry.category.ordinal());
        dest.writeByte(mNyaaEntry.markedForDownload ? (byte) 1 : (byte) 0);
    }

    public NyaaEntry getNyaaEntry()
    {
        return mNyaaEntry;
    }
}
