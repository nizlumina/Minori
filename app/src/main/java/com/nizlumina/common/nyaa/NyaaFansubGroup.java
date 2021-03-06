package com.nizlumina.common.nyaa;

import java.util.ArrayList;
import java.util.List;

public final class NyaaFansubGroup
{
    public static final String MULTI_QUALITY = "Multi-Quality", MULTI_RES = "Multi-Res";

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

//    public NyaaEntry getLatestEntry(NyaaEntry.Resolution resolution)
//    {
//        for (NyaaEntry entry : getNyaaEntries())
//        {
//            if (entry.getResolution() == resolution)
//            {
//                if (entry.getCurrentEpisode() == getLatestEpisode())
//                {
//                    return entry;
//                }
//            }
//        }
//        return null;
//    }

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

    public String getSeriesTitle()
    {
        return mSeriesTitle;
    }

    public void setSeriesTitle(String seriesTitle)
    {
        this.mSeriesTitle = seriesTitle;
    }


//    public String getResolutionDisplayString()
//    {
//        if (mResolutions.size() > 0)
//        {
//            if (mResolutions.size() == 1)
//            {
//                return NyaaEntry.Resolution.getResolutionDisplayString(mResolutions.get(0), true);
//            }
//            else return MULTI_RES;
//        }
//        else return null;
//    }

//    //For debugging
//    public String stringData()
//    {
//        return String.format("%s\n%s\n%s\n%s\n%s\n%s", getSeriesTitle(), getGroupName(), getId(), getLatestEpisode(), getResolutionDisplayString(), getTrustCategory().name());
//    }
}
