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

/**
 * MAL Object as returned by MAL
 */
public class MALObject implements Parcelable
{
    //As returned by the search API
    public static final String IDENTIFIER = "anime";
    public static final String ENTRY_ARRAY_IDENTIFIER = "entry";
    public static final Parcelable.Creator<MALObject> CREATOR = new Parcelable.Creator<MALObject>()
    {
        public MALObject createFromParcel(Parcel source) {return new MALObject(source);}

        public MALObject[] newArray(int size) {return new MALObject[size];}
    };
    private int id;
    private String endDate;
    private String title;
    private String status;
    private float score;
    private String image;
    private String synopsis;
    private String synonyms;
    private String type;
    private String startDate;
    private int episodes;
    private String english;

    public MALObject() {}

    public MALObject(int id, String endDate, String title, String status, float score, String image, String synopsis, String synonyms, String type, String startDate, int episodes, String english)
    {
        this.id = id;
        this.endDate = endDate;
        this.title = title;
        this.status = status;
        this.score = score;
        this.image = image;
        this.synopsis = synopsis;
        this.synonyms = synonyms;
        this.type = type;
        this.startDate = startDate;
        this.episodes = episodes;
        this.english = english;
    }

    private MALObject(Parcel in)
    {
        this.id = in.readInt();
        this.endDate = in.readString();
        this.title = in.readString();
        this.status = in.readString();
        this.score = in.readFloat();
        this.image = in.readString();
        this.synopsis = in.readString();
        this.synonyms = in.readString();
        this.type = in.readString();
        this.startDate = in.readString();
        this.episodes = in.readInt();
        this.english = in.readString();
    }

    public int getId()
    {
        return id;
    }

    public String getEndDate()
    {
        return endDate;
    }

    public String getTitle()
    {
        return title;
    }

    public String getStatus()
    {
        return status;
    }

    public float getScore()
    {
        return score;
    }

    public String getImage()
    {
        return image;
    }

    public String getSynopsis()
    {
        return synopsis;
    }

    public String getSynonyms()
    {
        return synonyms;
    }

    public String getType()
    {
        return type;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public int getEpisodes()
    {
        return episodes;
    }

    public String getEnglish()
    {
        return english;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.id);
        dest.writeString(this.endDate);
        dest.writeString(this.title);
        dest.writeString(this.status);
        dest.writeFloat(this.score);
        dest.writeString(this.image);
        dest.writeString(this.synopsis);
        dest.writeString(this.synonyms);
        dest.writeString(this.type);
        dest.writeString(this.startDate);
        dest.writeInt(this.episodes);
        dest.writeString(this.english);
    }

    public static class Builder
    {

        private int id;
        private String endDate;
        private String title;
        private String status;
        private float score;
        private String image;
        private String synopsis;
        private String synonyms;
        private String type;
        private String startDate;
        private int episodes;
        private String english;

        public Builder setId(int id)
        {
            this.id = id;
            return this;
        }

        public Builder setEndDate(String endDate)
        {
            this.endDate = endDate;
            return this;
        }

        public Builder setTitle(String title)
        {
            this.title = title;
            return this;
        }

        public Builder setStatus(String status)
        {
            this.status = status;
            return this;
        }

        public Builder setScore(float score)
        {
            this.score = score;
            return this;
        }

        public Builder setImage(String image)
        {
            this.image = image;
            return this;
        }

        public Builder setSynopsis(String synopsis)
        {
            this.synopsis = synopsis;
            return this;
        }

        public Builder setSynonyms(String synonyms)
        {
            this.synonyms = synonyms;
            return this;
        }

        public Builder setType(String type)
        {
            this.type = type;
            return this;
        }

        public Builder setStartDate(String startDate)
        {
            this.startDate = startDate;
            return this;
        }

        public Builder setEpisodes(int episodes)
        {
            this.episodes = episodes;
            return this;
        }

        public Builder setEnglish(String english)
        {
            this.english = english;
            return this;
        }

        public MALObject createMALObject()
        {
            return new MALObject(id, endDate, title, status, score, image, synopsis, synonyms, type, startDate, episodes, english);
        }
    }
}
