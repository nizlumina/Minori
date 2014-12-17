package com.nizlumina.minori.core.Nyaa;

/**
 * Base data class for Nyaa stuffs
 */
public final class NyaaEntry
{
    public int id = -1; //you can use this for distinguishing alarms (especially for pendingIntents)
    public int currentEpisode = -1;
    public boolean failToParse = false;
    public String title;
    public String rawTitle;
    public String fansub;
    public String episodeString;
    public String fileType;
    public String resolutionString;
    public String hash;
    public String quality;
    public String torrentLink;
    public String description;
    public String pubDate;
    public String extras;
    public Trust trustCategory;
    public Resolution resolution;
    public Category category;

    //Downloader section
    public boolean markedForDownload = false;

    public NyaaEntry() {}

    public int nextEpisode()
    {
        return this.currentEpisode + 1;
    }

    //Only for logging
    public String stringData()
    {
        return "RawTitle [" + this.rawTitle + "]\n[" +
                this.title + "]\n[ID: " +
                this.id + "]\n[" +
                this.fansub + "]\n[" +
                this.fileType + "]\n[RESSTR: " +
                this.resolutionString + "]\n[RES: " +
                this.resolution + "]\n[CurrEP: " +
                this.currentEpisode + "]\n[CurrEPString: " +
                this.episodeString + "]\n[HASH: " +
                this.hash + "]\n[Q: " +
                this.quality + "]\n[" +
                this.torrentLink + "]\n[DESC: " +
                this.description + "]\n[EXTRA: " +
                this.extras + "]\n[" +
                this.pubDate + "]\n[TRUST: " +
                this.trustCategory + "]\n[CAT: " +
                this.category + "]";
    }

    public String quickStringData()
    {
        return String.format("[%s] [%s] [%s] [%s] [%s]", this.title, this.fansub, this.currentEpisode, this.resolutionString, this.trustCategory.name());
    }

    public final String buildQuery()
    {
        String mQuality = quality, mResolutionString = resolutionString;
        if (mQuality == null) mQuality = Static.UNDEFINED_STRING;
        if (mResolutionString == null) mResolutionString = Static.UNDEFINED_STRING;
        return title + " " + fansub + " " + mResolutionString + " " + mQuality;
    }

    public final NyaaEntry createSparseCopy()
    {
        NyaaEntry sparseCopy = new NyaaEntry();
        sparseCopy.title = this.title;
        sparseCopy.fansub = this.fansub;
        sparseCopy.resolutionString = this.resolutionString;
        sparseCopy.quality = this.quality;
        return sparseCopy;
    }

    public final boolean compareToSparseCopy(NyaaEntry sparseCopy)
    {
        try
        {
            return sparseCopy.title.equals(this.title)
                    && sparseCopy.fansub.equals(this.fansub)
                    && sparseCopy.resolutionString.equals(this.resolutionString)
                    && sparseCopy.quality.equals(this.quality);
        }
        catch (NullPointerException e)
        {
            return false;
        }
    }

    public final boolean matchOriginalSignature(NyaaEntry original)
    {
        if (trustCategory.equals(original.trustCategory))
        {
            if (title.equalsIgnoreCase(original.title) && fansub.equalsIgnoreCase(original.fansub))
            {
                return resolution.equals(original.resolution);
            }
        }
        return false;
    }

    /**
     * Returns true if parameter is edited succesfully. Any of the parameter can be null. Return false on exception.
     */
    public final boolean editEntry(final String selectedTitle, final String selectedGroup, final String episodeString, final String selectedResString, boolean episodeStringIsCurrent)
    {
        try
        {
            if (selectedTitle != null)
            {
                if (selectedTitle.trim().length() > 0)
                {
                    this.title = selectedTitle;
                }
            }

            if (selectedGroup != null)
            {
                if (selectedGroup.trim().length() > 0)
                {
                    this.fansub = selectedGroup;
                }
            }

            if (episodeString != null)
            {
                if (episodeString.trim().length() > 0)
                {
                    this.episodeString = episodeString;
                    int parsedEpisode = Integer.parseInt(episodeString);
                    if (episodeStringIsCurrent) this.currentEpisode = parsedEpisode;
                    else this.currentEpisode = --parsedEpisode;
                }
            }


            if (selectedResString != null)
            {
                if (selectedResString.trim().length() > 0)
                {
                    this.resolutionString = selectedResString;
                    this.resolution = Resolution.matchResolution(resolutionString);
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }

    }

    public enum Trust
    {
        APLUS, TRUSTED, REMAKES, ALL;

        public static String getTrustString(Trust trustKey)
        {
            switch (trustKey)
            {
                case APLUS:
                    return Static.A;
                case TRUSTED:
                    return Static.TRUSTED;
                case REMAKES:
                    return Static.REMAKES;
                case ALL:
                    return Static.UNDEFINED_STRING;
            }
            return Static.UNDEFINED_STRING;
        }

        public static Trust getTrustFromOrdinals(int i)
        {
            if (i == APLUS.ordinal()) return APLUS;
            if (i == TRUSTED.ordinal()) return TRUSTED;
            if (i == REMAKES.ordinal()) return REMAKES;
            return ALL;
        }
    }

    public enum Resolution
    {
        DEFAULT, R480, R720, R1080;

        public static String getResolutionDisplayString(NyaaEntry displayEntry, boolean showDefault)
        {
            switch (displayEntry.resolution)
            {
                case R480:
                    return Static.R480 + 'p';
                case R720:
                    return Static.R720 + 'p';
                case R1080:
                    return Static.R1080 + 'p';
                case DEFAULT:
                    if (showDefault)
                    {
                        if (displayEntry.resolutionString == null) return "Default";
                        else if (displayEntry.resolutionString.equalsIgnoreCase(Static.UNDEFINED_STRING))
                            return "Default";
                        else return displayEntry.resolutionString;
                    }
                    else
                    {
                        if (displayEntry.resolutionString == null) return Static.UNDEFINED_STRING;
                        else if (displayEntry.resolutionString.equalsIgnoreCase(Static.UNDEFINED_STRING))
                            return Static.UNDEFINED_STRING;
                        else return displayEntry.resolutionString;
                    }

            }
            return null;
        }

        public static Resolution matchResolution(String searchString)
        {
            if (searchString.equalsIgnoreCase(Static.UNDEFINED_STRING))
                return DEFAULT;
            if (searchString.contains(Static.R480))
                return R480;
            if (searchString.contains(Static.R720))
                return R720;
            if (searchString.contains(Static.R1080))
                return R1080;
            return DEFAULT;

        }
    }

    public enum Category
    {
        ALL, RAWS, TRANS_NON_ENGLISH, TRANS_ENGLISH;
        final static String QUERY_ALL = "1_0";
        final static String QUERY_RAWS = "1_11";
        final static String QUERY_TRANS_NON_ENGLISH = "1_38";
        final static String QUERY_TRANS_ENGLISH = "1_37";

        public String getQueryString(Category category)
        {
            switch (category)
            {
                case ALL:
                    return QUERY_ALL;
                case RAWS:
                    return QUERY_RAWS;
                case TRANS_NON_ENGLISH:
                    return QUERY_TRANS_NON_ENGLISH;
                case TRANS_ENGLISH:
                    return QUERY_TRANS_ENGLISH;
            }
            return QUERY_ALL;
        }
    }

    public static final class Static
    {
        public static final String ID = "id";
        public static final String TITLE = "title";
        public static final String FANSUB = "fansub";
        public static final String TRUST = "trust";
        public static final String RESOLUTION = "resolution";
        public static final String QUALITY = "quality";
        public static final String CURRENT_EPISODE = "currentEpisode";
        public static final String CURRENT_EPISODE_STRING = "currentEpisodeString";
        public static final String PUBDATE = "pubDate";

        public static final String R480 = "480", R720 = "720", R1080 = "1080";
        public static final String A = "A+", TRUSTED = "Trusted", REMAKES = "Remake";
        public static final String UNDEFINED_STRING = ""; //Use this only for internal, never for query
        public static final int UNDEFINED_INT = -1; //same as above
    }

}
