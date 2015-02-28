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

public final class ParcelableNyaaEntry implements Parcelable
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
        mNyaaEntry.setId(in.readInt());
        mNyaaEntry.setCurrentEpisode(in.readInt());
        mNyaaEntry.setFailToParse(in.readByte() != 0);
        mNyaaEntry.setTitle(in.readString());
        mNyaaEntry.setRawTitle(in.readString());
        mNyaaEntry.setFansub(in.readString());
        mNyaaEntry.setEpisodeString(in.readString());
        mNyaaEntry.setFileType(in.readString());
        mNyaaEntry.setResolutionString(in.readString());
        mNyaaEntry.setHash(in.readString());
        mNyaaEntry.setQuality(in.readString());
        mNyaaEntry.setTorrentLink(in.readString());
        mNyaaEntry.setDescription(in.readString());
        mNyaaEntry.setPubDate(in.readString());
        mNyaaEntry.setExtras(in.readString());
        int tmpTrustCategory = in.readInt();
        mNyaaEntry.setTrustCategory(tmpTrustCategory == -1 ? null : NyaaEntry.Trust.values()[tmpTrustCategory]);
        int tmpResolution = in.readInt();
        mNyaaEntry.setResolution(tmpResolution == -1 ? null : NyaaEntry.Resolution.values()[tmpResolution]);
        int tmpCategory = in.readInt();
        mNyaaEntry.setCategory(tmpCategory == -1 ? null : NyaaEntry.Category.values()[tmpCategory]);
        mNyaaEntry.setMarkedForDownload(in.readByte() != 0);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mNyaaEntry.getId());
        dest.writeInt(mNyaaEntry.getCurrentEpisode());
        dest.writeByte(mNyaaEntry.isFailToParse() ? (byte) 1 : (byte) 0);
        dest.writeString(mNyaaEntry.getTitle());
        dest.writeString(mNyaaEntry.getRawTitle());
        dest.writeString(mNyaaEntry.getFansub());
        dest.writeString(mNyaaEntry.getEpisodeString());
        dest.writeString(mNyaaEntry.getFileType());
        dest.writeString(mNyaaEntry.getResolutionString());
        dest.writeString(mNyaaEntry.getHash());
        dest.writeString(mNyaaEntry.getQuality());
        dest.writeString(mNyaaEntry.getTorrentLink());
        dest.writeString(mNyaaEntry.getDescription());
        dest.writeString(mNyaaEntry.getPubDate());
        dest.writeString(mNyaaEntry.getExtras());
        dest.writeInt(mNyaaEntry.getTrustCategory() == null ? -1 : mNyaaEntry.getTrustCategory().ordinal());
        dest.writeInt(mNyaaEntry.getResolution() == null ? -1 : mNyaaEntry.getResolution().ordinal());
        dest.writeInt(mNyaaEntry.getCategory() == null ? -1 : mNyaaEntry.getCategory().ordinal());
        dest.writeByte(mNyaaEntry.isMarkedForDownload() ? (byte) 1 : (byte) 0);
    }

    public NyaaEntry getNyaaEntry()
    {
        return mNyaaEntry;
    }
}
