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
        static final String searchPath = "http://hummingbird.me/api/v1/search/anime";
        static final String searchArg = "query";

        static final String idPath = "http://hummingbird.me/api/v1/anime/";

        public static Uri searchQuery(String terms)
        {
            return Uri.parse(searchPath).buildUpon().appendQueryParameter(searchArg, terms).build();
        }

        public static Uri getAnimeByID(int id)
        {
            return Uri.parse(idPath).buildUpon().appendQueryParameter(searchArg, String.valueOf(id)).build();
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
