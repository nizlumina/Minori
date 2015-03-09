package com.nizlumina.minori.common.nyaa;

/**
 * Base data class for Nyaa stuffs
 */
public final class NyaaEntry
{
    private int id = -1; //you can use this for distinguishing alarms (especially for pendingIntents)
    private int currentEpisode = -1;
    private boolean failToParse = false;
    private String title;
    private String rawTitle;
    private String fansub;
    private String episodeString;
    private String fileType;
    private String resolutionString;
    private String hash;
    private String quality;
    private String torrentLink;
    private String description;
    private String pubDate;
    private String extras;
    private Trust trustCategory;
    private Resolution resolution;
    private Category category;

    //Downloader section
    private boolean markedForDownload = false;

    public NyaaEntry() {}

    public int nextEpisode()
    {
        return this.getCurrentEpisode() + 1;
    }

    //Only for logging
    public String stringData()
    {
        return "RawTitle [" + this.getRawTitle() + "]\n[" +
                this.getTitle() + "]\n[ID: " +
                this.getId() + "]\n[" +
                this.getFansub() + "]\n[" +
                this.getFileType() + "]\n[RESSTR: " +
                this.getResolutionString() + "]\n[RES: " +
                this.getResolution() + "]\n[CurrEP: " +
                this.getCurrentEpisode() + "]\n[CurrEPString: " +
                this.getEpisodeString() + "]\n[HASH: " +
                this.getHash() + "]\n[Q: " +
                this.getQuality() + "]\n[" +
                this.getTorrentLink() + "]\n[DESC: " +
                this.getDescription() + "]\n[EXTRA: " +
                this.getExtras() + "]\n[" +
                this.getPubDate() + "]\n[TRUST: " +
                this.getTrustCategory() + "]\n[CAT: " +
                this.getCategory() + "]";
    }

    public String quickStringData()
    {
        return String.format("[%s] [%s] [%s] [%s] [%s]", this.getTitle(), this.getFansub(), this.getCurrentEpisode(), this.getResolutionString(), this.getTrustCategory().name());
    }

    public final String buildQuery()
    {
        String mQuality = getQuality(), mResolutionString = getResolutionString();
        if (mQuality == null) mQuality = Static.UNDEFINED_STRING;
        if (mResolutionString == null) mResolutionString = Static.UNDEFINED_STRING;
        return getTitle() + " " + getFansub() + " " + mResolutionString + " " + mQuality;
    }

    public final NyaaEntry createSparseCopy()
    {
        NyaaEntry sparseCopy = new NyaaEntry();
        sparseCopy.setTitle(this.getTitle());
        sparseCopy.setFansub(this.getFansub());
        sparseCopy.setResolutionString(this.getResolutionString());
        sparseCopy.setQuality(this.getQuality());
        return sparseCopy;
    }

    public final boolean compareToSparseCopy(NyaaEntry sparseCopy)
    {
        try
        {
            return sparseCopy.getTitle().equals(this.getTitle())
                    && sparseCopy.getFansub().equals(this.getFansub())
                    && sparseCopy.getResolutionString().equals(this.getResolutionString())
                    && sparseCopy.getQuality().equals(this.getQuality());
        }
        catch (NullPointerException e)
        {
            return false;
        }
    }

    public final boolean matchOriginalSignature(NyaaEntry original)
    {
        if (getTrustCategory().equals(original.getTrustCategory()))
        {
            if (getTitle().equalsIgnoreCase(original.getTitle()) && getFansub().equalsIgnoreCase(original.getFansub()))
            {
                return getResolution().equals(original.getResolution());
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
                    this.setTitle(selectedTitle);
                }
            }

            if (selectedGroup != null)
            {
                if (selectedGroup.trim().length() > 0)
                {
                    this.setFansub(selectedGroup);
                }
            }

            if (episodeString != null)
            {
                if (episodeString.trim().length() > 0)
                {
                    this.setEpisodeString(episodeString);
                    int parsedEpisode = Integer.parseInt(episodeString);
                    if (episodeStringIsCurrent) this.setCurrentEpisode(parsedEpisode);
                    else this.setCurrentEpisode(--parsedEpisode);
                }
            }


            if (selectedResString != null)
            {
                if (selectedResString.trim().length() > 0)
                {
                    this.setResolutionString(selectedResString);
                    this.setResolution(Resolution.matchResolution(getResolutionString()));
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }

    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public int getCurrentEpisode()
    {
        return currentEpisode;
    }

    public void setCurrentEpisode(int currentEpisode)
    {
        this.currentEpisode = currentEpisode;
    }

    public boolean isFailToParse()
    {
        return failToParse;
    }

    public void setFailToParse(boolean failToParse)
    {
        this.failToParse = failToParse;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getRawTitle()
    {
        return rawTitle;
    }

    public void setRawTitle(String rawTitle)
    {
        this.rawTitle = rawTitle;
    }

    public String getFansub()
    {
        return fansub;
    }

    public void setFansub(String fansub)
    {
        this.fansub = fansub;
    }

    public String getEpisodeString()
    {
        return episodeString;
    }

    public void setEpisodeString(String episodeString)
    {
        this.episodeString = episodeString;
    }

    public String getFileType()
    {
        return fileType;
    }

    public void setFileType(String fileType)
    {
        this.fileType = fileType;
    }

    public String getResolutionString()
    {
        return resolutionString;
    }

    public void setResolutionString(String resolutionString)
    {
        this.resolutionString = resolutionString;
    }

    public String getHash()
    {
        return hash;
    }

    public void setHash(String hash)
    {
        this.hash = hash;
    }

    public String getQuality()
    {
        return quality;
    }

    public void setQuality(String quality)
    {
        this.quality = quality;
    }

    public String getTorrentLink()
    {
        return torrentLink;
    }

    public void setTorrentLink(String torrentLink)
    {
        this.torrentLink = torrentLink;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getPubDate()
    {
        return pubDate;
    }

    public void setPubDate(String pubDate)
    {
        this.pubDate = pubDate;
    }

    public String getExtras()
    {
        return extras;
    }

    public void setExtras(String extras)
    {
        this.extras = extras;
    }

    public Trust getTrustCategory()
    {
        return trustCategory;
    }

    public void setTrustCategory(Trust trustCategory)
    {
        this.trustCategory = trustCategory;
    }

    public Resolution getResolution()
    {
        return resolution;
    }

    public void setResolution(Resolution resolution)
    {
        this.resolution = resolution;
    }

    public Category getCategory()
    {
        return category;
    }

    public void setCategory(Category category)
    {
        this.category = category;
    }

    public boolean isMarkedForDownload()
    {
        return markedForDownload;
    }

    public void setMarkedForDownload(boolean markedForDownload)
    {
        this.markedForDownload = markedForDownload;
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

        public static String getResolutionDisplayString(NyaaEntry displayEntry, boolean displayDefaultForNonStandard)
        {
            return getResolutionDisplayString(displayEntry.getResolution(), displayDefaultForNonStandard);
        }

        public static String getResolutionDisplayString(Resolution resolutionKey, boolean displayDefaultForNonStandard)
        {
            switch (resolutionKey)
            {
                case R480:
                    return Static.R480 + 'p';
                case R720:
                    return Static.R720 + 'p';
                case R1080:
                    return Static.R1080 + 'p';
                case DEFAULT:
                    if (displayDefaultForNonStandard)
                    {
                        return "Default";
                    }
                    else
                    {
                        return null;
                    }
                default:
                    return null;
            }
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

        public static Resolution getResolutionFromOrdinals(int ordinal)
        {
            if (ordinal == DEFAULT.ordinal()) return DEFAULT;
            if (ordinal == R480.ordinal()) return R480;
            if (ordinal == R720.ordinal()) return R720;
            if (ordinal == R1080.ordinal()) return R1080;
            return null;
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
