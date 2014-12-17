package com.nizlumina.minori.core.Nyaa;

import java.util.ArrayList;

public final class NyaaFansubGroup
{
    public final String name;
    public final ArrayList<NyaaEntry> animeEntries = new ArrayList<NyaaEntry>(); //blind list for all entries under this fansub name
    public final ArrayList<String> qualities = new ArrayList<String>(); //only for display
    public final ArrayList<NyaaEntry.Resolution> resolutions = new ArrayList<NyaaEntry.Resolution>(); //size will always be > 0 due to Resolution.Default added to an AnimeEntry if its resolution cannot be parsed
    public String seriesTitle;
    public int id; //set in NyaaXmlParser while setting latest episode. it always point to the latest id
    public NyaaEntry.Trust trustCategory;
    public int latestEpisode; //set up during first Parsing in NyaaXmlParser

    public NyaaFansubGroup(String fansubName)
    {
        this.name = fansubName;
    }

    public NyaaEntry getLatestEntry(NyaaEntry.Resolution resolution)
    {
        for (NyaaEntry entry : animeEntries)
        {
            if (entry.resolution == resolution)
            {
                if (entry.currentEpisode == latestEpisode)
                {
                    return entry;
                }
            }
        }
        return null;
    }
}
