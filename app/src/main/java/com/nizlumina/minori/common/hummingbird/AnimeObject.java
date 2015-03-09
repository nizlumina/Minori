package com.nizlumina.minori.common.hummingbird;

/**
 * Hummingbird API JSON object
 */
public class AnimeObject
{
    public static final String JSON_API_ID = "id";
    public static final String JSON_API_SLUG = "slug";
    public static final String JSON_API_STATUS = "status";
    public static final String JSON_API_URL = "url";
    public static final String JSON_API_TITLE = "title";
    public static final String JSON_API_EPS_COUNT = "episode_count";
    public static final String JSON_API_COVER_IMG_URL = "cover_image";
    public static final String JSON_API_SYNOPSIS = "synopsis";
    public static final String JSON_API_STARTED_AIRING = "started_airing";
    public static final String JSON_API_FINISHED_AIRING = "finished_airing";
    //Non-API
    public static final String JSON_CACHED_IMG_URI = "cached_image_uri";

    private int id;
    private String slug;
    private String status;
    private String title;
    private String url;
    private int episodeCount;
    private String cachedImageURI;
    private String imageUrl;
    private String synopsis;
    private String startedAiring;
    private String finishedAiring;

    //For loggings
    public String stringData()
    {
        return String.format("ID [%s]\nSlug [%s]\nStatus [%s]\nTitle [%s]\nURL [%s]\nEps Count[%s]\nCache IMG URI [%s]\nWeb IMG URI [%s]\n\nSynopsis:\n %s\n\nAir Start [%s]\nAir Finish[%s]\n ", getId(), getSlug(), getStatus(), getTitle(), getUrl(), getEpisodeCount(), getCachedImageURI(), getImageUrl(), getSynopsis(), getStartedAiring(), getFinishedAiring());
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

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public int getEpisodeCount()
    {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount)
    {
        this.episodeCount = episodeCount;
    }

    public String getCachedImageURI()
    {
        return cachedImageURI;
    }

    public void setCachedImageURI(String cachedImageURI)
    {
        this.cachedImageURI = cachedImageURI;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public String getSynopsis()
    {
        return synopsis;
    }

    public void setSynopsis(String synopsis)
    {
        this.synopsis = synopsis;
    }

    public String getStartedAiring()
    {
        return startedAiring;
    }

    public void setStartedAiring(String startedAiring)
    {
        this.startedAiring = startedAiring;
    }

    public String getFinishedAiring()
    {
        return finishedAiring;
    }

    public void setFinishedAiring(String finishedAiring)
    {
        this.finishedAiring = finishedAiring;
    }
}
