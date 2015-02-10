package com.nizlumina.minori.core.Hummingbird;

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

    public int id;
    public String slug;
    public String status;
    public String title;
    public String url;
    public int episodeCount;
    public String cachedImageURI;
    public String imageUrl;
    public String synopsis;
    public String startedAiring;
    public String finishedAiring;

    //For loggings
    public String stringData()
    {
        return String.format("ID [%s]\nSlug [%s]\nStatus [%s]\nTitle [%s]\nURL [%s]\nEps Count[%s]\nCache IMG URI [%s]\nWeb IMG URI [%s]\n\nSynopsis:\n %s\n\nAir Start [%s]\nAir Finish[%s]\n ", id, slug, status, title, url, episodeCount, cachedImageURI, imageUrl, synopsis, startedAiring, finishedAiring);
    }
}
