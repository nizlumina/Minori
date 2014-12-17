package com.nizlumina.minori.core.Hummingbird;

/**
 * Hummingbird API JSON object
 */
public class AnimeObject
{
    public static final String JSON_ID = "id";
    public static final String JSON_STATUS = "status";
    public static final String JSON_URL = "url";
    public static final String JSON_TITLE = "title";
    public static final String JSON_EPS_COUNT = "episode_count";
    public static final String JSON_COVER_IMG = "cover_image";
    public static final String JSON_SYNOPSIS = "synopsis";
    public static final String JSON_STARTED_AIRING = "started_airing";
    public static final String JSON_FINISHED_AIRING = "finished_airing";
    public static final String JSON_NONAPI_CACHED_IMG_URI = "nonapi_cached_image_uri";

    public int id;
    public String status;
    public String url, title;
    public int episodeCount;
    public String cachedImageURI;
    public String imageUrl;
    public String synopsis;
    public String startedAiring;
    public String finishedAiring;
}
