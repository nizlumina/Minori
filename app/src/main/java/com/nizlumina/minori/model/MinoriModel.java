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

package com.nizlumina.minori.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.nizlumina.common.nyaa.NyaaFansubGroup;
import com.nizlumina.syncmaru.model.CompositeData;

//A composite model that links all the models to be used by Minori
public final class MinoriModel implements Parcelable
{
    public static final Parcelable.Creator<MinoriModel> CREATOR = new Parcelable.Creator<MinoriModel>()
    {
        public MinoriModel createFromParcel(Parcel source) {return new MinoriModel(source);}

        public MinoriModel[] newArray(int size) {return new MinoriModel[size];}
    };
    private CompositeData compositeData;
    private NyaaFansubGroup nyaaFansubGroup;

    public MinoriModel() {}

    public MinoriModel(CompositeData compositeData, NyaaFansubGroup nyaaFansubGroup)
    {
        this.compositeData = compositeData;
        this.nyaaFansubGroup = nyaaFansubGroup;
    }

    protected MinoriModel(Parcel in)
    {
        this.compositeData = in.readParcelable(CompositeData.class.getClassLoader());
        this.nyaaFansubGroup = in.readParcelable(NyaaFansubGroup.class.getClassLoader());
    }

    public CompositeData getCompositeData()
    {
        return compositeData;
    }

    public NyaaFansubGroup getNyaaFansubGroup()
    {
        return nyaaFansubGroup;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeParcelable(this.compositeData, 0);
        dest.writeParcelable(this.nyaaFansubGroup, 0);
    }

//    public static class Presenter
//    {
//        String title, imageURL, currentNyaaEpisode, resolution, modes, group;
//
//        public Presenter(MinoriModel minoriModel)
//        {
//            title = minoriModel.getNyaaFansubGroup().getSeriesTitle();
//            imageURL =
//        }
//    }
}
