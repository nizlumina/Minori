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

package com.nizlumina.minori.android.wrapper;

import android.os.Parcel;
import android.os.Parcelable;

import com.nizlumina.minori.core.Nyaa.NyaaEntry;
import com.nizlumina.minori.core.Nyaa.NyaaFansubGroup;

import java.util.ArrayList;
import java.util.List;

public class ParcelableNyaaFansubGroup implements Parcelable
{
    public static final Parcelable.Creator<ParcelableNyaaFansubGroup> CREATOR = new Parcelable.Creator<ParcelableNyaaFansubGroup>()
    {
        public ParcelableNyaaFansubGroup createFromParcel(Parcel source) {return new ParcelableNyaaFansubGroup(source);}

        public ParcelableNyaaFansubGroup[] newArray(int size) {return new ParcelableNyaaFansubGroup[size];}
    };
    private NyaaFansubGroup mNyaaFansubGroup;

    public ParcelableNyaaFansubGroup(NyaaFansubGroup nyaaFansubGroup)
    {
        this.mNyaaFansubGroup = nyaaFansubGroup;
    }

    private ParcelableNyaaFansubGroup(Parcel in)
    {
        mNyaaFansubGroup = new NyaaFansubGroup(in.readString());

        in.readList(mNyaaFansubGroup.getNyaaEntries(), ParcelableNyaaEntry.class.getClassLoader());

        in.readStringList(mNyaaFansubGroup.getQualities());

        List<Integer> resList = new ArrayList<>();
        in.readList(resList, Integer.class.getClassLoader());
        for (int res : resList)
        {
            mNyaaFansubGroup.getResolutions().add(NyaaEntry.Resolution.getResolutionFromOrdinals(res));
        }

        mNyaaFansubGroup.setSeriesTitle(in.readString());
        mNyaaFansubGroup.setId(in.readInt());
        mNyaaFansubGroup.setTrustCategory(NyaaEntry.Trust.getTrustFromOrdinals(in.readInt()));
        mNyaaFansubGroup.setLatestEpisode(in.readInt());
    }

    public NyaaFansubGroup getNyaaFansubGroup()
    {
        return mNyaaFansubGroup;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mNyaaFansubGroup.getGroupName());

        List<ParcelableNyaaEntry> parcelableNyaaEntries = new ArrayList<>();
        for (NyaaEntry nyaaEntry : mNyaaFansubGroup.getNyaaEntries())
        {
            parcelableNyaaEntries.add(new ParcelableNyaaEntry(nyaaEntry));
        }
        dest.writeList(parcelableNyaaEntries);

        dest.writeStringList(mNyaaFansubGroup.getQualities());

        List<Integer> resList = new ArrayList<>();
        for (NyaaEntry.Resolution resolution : mNyaaFansubGroup.getResolutions())
        {
            resList.add(resolution.ordinal());
        }
        dest.writeList(resList);

        dest.writeString(mNyaaFansubGroup.getSeriesTitle());
        dest.writeInt(mNyaaFansubGroup.getId());
        dest.writeInt(mNyaaFansubGroup.getTrustCategory().ordinal());
        dest.writeInt(mNyaaFansubGroup.getLatestEpisode());
    }
}
