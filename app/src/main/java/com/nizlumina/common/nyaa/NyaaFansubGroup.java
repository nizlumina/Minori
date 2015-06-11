package com.nizlumina.common.nyaa;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public final class NyaaFansubGroup implements Parcelable
{
    public static final Parcelable.Creator<NyaaFansubGroup> CREATOR = new Parcelable.Creator<NyaaFansubGroup>()
    {
        public NyaaFansubGroup createFromParcel(Parcel source) {return new NyaaFansubGroup(source);}

        public NyaaFansubGroup[] newArray(int size) {return new NyaaFansubGroup[size];}
    };
    private final String mGroupName;
    private final ArrayList<NyaaEntry> mNyaaEntries = new ArrayList<>(); //blind list for all entries under this fansub name
    private final ArrayList<String> mQualities = new ArrayList<>(); //only for display
    private final List<NyaaEntry.Resolution> mResolutions = new ArrayList<>(); //size will always be > 0 due to Resolution.Default added to an AnimeEntry if its resolution cannot be parsed
    private String mSeriesTitle;
    private int mId; //set in NyaaXmlParser while setting latest episode. it always point to the latest id
    private NyaaEntry.Trust mTrustCategory;
    private int mLatestEpisode; //set up during first Parsing in NyaaXmlParser
    private int mModes;

    public NyaaFansubGroup(String groupName)
    {
        this.mGroupName = groupName;
    }

    protected NyaaFansubGroup(Parcel in)
    {
        this.mGroupName = in.readString();
        in.readList(this.mNyaaEntries, List.class.getClassLoader());
        in.readStringList(this.mQualities);
        in.readList(this.mResolutions, List.class.getClassLoader());
        this.mSeriesTitle = in.readString();
        this.mId = in.readInt();
        int tmpMTrustCategory = in.readInt();
        this.mTrustCategory = tmpMTrustCategory == -1 ? null : NyaaEntry.Trust.values()[tmpMTrustCategory];
        this.mLatestEpisode = in.readInt();
        this.mModes = in.readInt();
    }

    public int getId()
    {
        return mId;
    }

    /**
     * This is used in parsers. The ID corresponds the latest episode ID. This is intended for sorting/unique random ID.
     *
     * @param id The ID that corresponds to the latest episode ID for this group.
     */
    public void setId(int id)
    {
        this.mId = id;
    }

    public NyaaEntry.Trust getTrustCategory()
    {
        return mTrustCategory;
    }

    public void setTrustCategory(NyaaEntry.Trust trustCategory)
    {
        this.mTrustCategory = trustCategory;
    }

    public int getLatestEpisode()
    {
        return mLatestEpisode;
    }

    public void setLatestEpisode(int latestEpisode)
    {
        this.mLatestEpisode = latestEpisode;
    }

    public int getModes()
    {
        return mModes;
    }

    public void setModes(int mModes)
    {
        this.mModes = mModes;
    }

    public String getGroupName()
    {
        return mGroupName;
    }

    public ArrayList<NyaaEntry> getNyaaEntries()
    {
        return mNyaaEntries;
    }

    public ArrayList<String> getQualities()
    {
        return mQualities;
    }

    public List<NyaaEntry.Resolution> getResolutions()
    {
        return mResolutions;
    }

    //Android parcel implementation

    public String getSeriesTitle()
    {
        return mSeriesTitle;
    }

    public void setSeriesTitle(String seriesTitle)
    {
        this.mSeriesTitle = seriesTitle;
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.mGroupName);
        dest.writeList(this.mNyaaEntries);
        dest.writeStringList(this.mQualities);
        dest.writeList(this.mResolutions);
        dest.writeString(this.mSeriesTitle);
        dest.writeInt(this.mId);
        dest.writeInt(this.mTrustCategory == null ? -1 : this.mTrustCategory.ordinal());
        dest.writeInt(this.mLatestEpisode);
        dest.writeInt(this.mModes);
    }

    //end Android parcel implementation
}
