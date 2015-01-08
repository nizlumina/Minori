package com.nizlumina.minori.android.network;

import android.net.Uri;

import com.nizlumina.minori.core.Nyaa.NyaaEntry;

/**
 * Basic CoreQuery object
 */
public class CoreQuery
{
    public static class Hummingbird
    {
        static final String endpoint = "http://hummingbird.me/api/v1";
        static final String searchKey = "search";
        static final String typeAnime = "anime";
        static final String typeManga = "manga";
        static final String searchArg = "query";

        public static Uri searchQuery(String terms)
        {
            return Uri.parse(endpoint).buildUpon().appendPath(searchKey).appendPath(typeAnime).appendQueryParameter(searchArg, terms).build();
        }

        public static Uri getAnimeByID(String hummingbirdId)
        {
            return Uri.parse(endpoint).buildUpon().appendPath(typeAnime).appendPath(hummingbirdId).build();
        }
    }

    public static class Nyaa
    {
        //static final String sitePath = "http://www.nyaa.se/";
        private static final String authority = "www.nyaa.se";

        public static Uri getEnglishSubRSS(String query, NyaaEntry.Trust trust)
        {
            String filter = "";

            switch (trust)
            {
                case APLUS:
                    filter = "3";
                    break;
                case TRUSTED:
                    filter = "2";
                    break;
                case REMAKES:
                    filter = "1";
                    break;
                case ALL:
                    filter = "0";
                    break;
            }

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority(authority).appendPath("").appendQueryParameter("page", "rss").appendQueryParameter("cats", "1_37").appendQueryParameter("filter", filter).appendQueryParameter("term", query);
            return builder.build();
        }

        public static Uri getEnglishSubRSS(String query)
        {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority(authority).appendPath("").appendQueryParameter("page", "rss").appendQueryParameter("cats", "1_37").appendQueryParameter("term", query);
            return builder.build();

        }

        public static Uri getRSS(final String query, final NyaaEntry.Category category)
        {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority(authority).appendPath("").appendQueryParameter("page", "rss").appendQueryParameter("cats", category.getQueryString(category)).appendQueryParameter("term", query);
            return builder.build();

        }

        public static Uri viewID(int torrentId)
        {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority(authority).appendPath("").appendQueryParameter("page", "view").appendQueryParameter("tid", String.valueOf(torrentId));
            return builder.build();
        }

        public static Uri englishSubSearchView(String query)
        {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority(authority).appendPath("").appendQueryParameter("page", "search").appendQueryParameter("cats", "1_37").appendQueryParameter("term", query);
            return builder.build();
        }

        public static Uri getDownload(int torrentId)
        {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http").authority(authority).appendPath("").appendQueryParameter("page", "download").appendQueryParameter("tid", String.valueOf(torrentId));
            return builder.build();
        }
    }
}
