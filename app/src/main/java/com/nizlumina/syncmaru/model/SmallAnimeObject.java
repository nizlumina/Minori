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

package com.nizlumina.syncmaru.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SmallAnimeObject implements Parcelable
{
    public static final Parcelable.Creator<SmallAnimeObject> CREATOR = new Parcelable.Creator<SmallAnimeObject>()
    {
        public SmallAnimeObject createFromParcel(Parcel source) {return new SmallAnimeObject(source);}

        public SmallAnimeObject[] newArray(int size) {return new SmallAnimeObject[size];}
    };
    private int id;
    private String slug;
    private String showType;
    private String posterImage;

    public SmallAnimeObject() {}

    private SmallAnimeObject(Parcel in)
    {
        this.id = in.readInt();
        this.slug = in.readString();
        this.showType = in.readString();
        this.posterImage = in.readString();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getSlug()
    {
        return slug;
    }

    public void setSlug(String slug)
    {
        this.slug = slug;
    }

    public String getShowType()
    {
        return showType;
    }

    public void setShowType(String showType)
    {
        this.showType = showType;
    }

    public String getPosterImage()
    {
        return posterImage;
    }

    public void setPosterImage(String posterImage)
    {
        this.posterImage = posterImage;
    }

    public String getMediumPosterImage()
    {
        return posterImage.replace("/large/", "/medium/");
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.id);
        dest.writeString(this.slug);
        dest.writeString(this.showType);
        dest.writeString(this.posterImage);
    }
}
