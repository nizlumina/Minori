/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Unless specified, permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.android.presenter;

import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wrapper presenter class for AnimeObject. Use this for displaying any AnimeObject-only data in the UI
 */
public class AnimeObjectPresenter implements GalleryPresenter
{
    private SoftReference<AnimeObject> animeObjectWeakReference;

    public AnimeObjectPresenter(AnimeObject animeObject)
    {
        this.animeObjectWeakReference = new SoftReference<>(animeObject);
    }

    public static List<AnimeObjectPresenter> listFrom(Collection<AnimeObject> animeObjects)
    {
        List<AnimeObjectPresenter> presenters = new ArrayList<>();
        for (AnimeObject animeObject : animeObjects)
        {
            presenters.add(new AnimeObjectPresenter(animeObject));
        }
        return presenters;
    }

    @Override
    public void explicitlySetLocalImageURI(String uri)
    {
        animeObjectWeakReference.get().cachedImageURI = uri;
    }

    @Override
    public void explicitlySetOnlineImageURI(String uri)
    {
        animeObjectWeakReference.get().imageUrl = uri;
    }

    @Override
    public String getTitle()
    {
        return animeObjectWeakReference.get().title;
    }

    @Override
    public String getEpisode()
    {
        return null;
    }

    @Override
    public String getGroup()
    {
        return null;
    }

    @Override
    public String getLocalImageURI()
    {
        return animeObjectWeakReference.get().cachedImageURI;
    }

    @Override
    public String getOnlineImageURI()
    {
        return animeObjectWeakReference.get().imageUrl;
    }

//    public int getID()
//    {
//        return mAnimeObject.id;
//    }
//
//    public String getSlug()
//    {
//        return mAnimeObject.slug;
//    }
//
//    public String getStatus()
//    {
//        return mAnimeObject.status;
//    }
//
//    public String getUrl()
//    {
//        return mAnimeObject.url;
//    }
//
//    public int getEpisodeCount()
//    {
//        return mAnimeObject.episodeCount;
//    }
//
//    public String getCachedImageURI()
//    {
//        return mAnimeObject.cachedImageURI;
//    }
//
//    public String getImageUrl()
//    {
//        return mAnimeObject.imageUrl;
//    }
//
//    public String getSynopsis()
//    {
//        return mAnimeObject.synopsis;
//    }
//
//    public String getStartedAiring()
//    {
//        return mAnimeObject.startedAiring;
//    }
//
//    public String getFinishedAiring()
//    {
//        return mAnimeObject.finishedAiring;
//    }
}
