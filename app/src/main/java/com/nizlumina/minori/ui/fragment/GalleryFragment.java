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

package com.nizlumina.minori.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.WatchlistController;
import com.nizlumina.minori.model.WatchData;
import com.nizlumina.minori.ui.adapter.GenericAdapter;
import com.nizlumina.minori.ui.gallery.GalleryItemHolder;

import java.lang.ref.SoftReference;

public class GalleryFragment extends DrawerContentFragment
{
    private static final String FRAGMENT_TAG = "gallery_fragment";
    private GridView mGridView;
    private SoftReference<DrawerFragmentListener> mFragmentListenerRef = new SoftReference<DrawerFragmentListener>(null);

    public GalleryFragment() {}

    public static GalleryFragment newInstance(DrawerFragmentListener listener)
    {
        GalleryFragment galleryFragment = new GalleryFragment();
        galleryFragment.mFragmentListenerRef = new SoftReference<DrawerFragmentListener>(listener);
        return galleryFragment;
    }

    public static String getFragmentTag()
    {
        return FRAGMENT_TAG;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        mGridView = (GridView) inflater.inflate(R.layout.layout_gridview, container, false);
        return mGridView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        final Toolbar toolbar = getToolbarContract().getToolbar();
        toolbar.setTitle("Watchlist");
        setupGridView(mGridView);
    }

    private void setupGridView(final GridView gridView)
    {

        gridView.setOnScrollListener(getToolbarContract().getAutoDisplayToolbarListener());

        WatchlistController controller = new WatchlistController();
        GalleryItemHolder<WatchData> itemHolder = new GalleryItemHolder<WatchData>(new GalleryItemHolder.GalleryPresenter<WatchData>()
        {
            @Override
            public void loadInto(ImageView imageView, WatchData source)
            {
                Glide.with(GalleryFragment.this).load(source.getAnimeObject().getImageUrl()).into(imageView);
            }

            @Override
            public String getTitle(WatchData source)
            {
                return source.getAnimeObject().getTitle();
            }

            @Override
            public String getGroup(WatchData source)
            {
                return source.getNyaaEntry().getFansub();
            }

            @Override
            public String getEpisode(WatchData source)
            {
                return source.getNyaaEntry().getEpisodeString();
            }

            @Override
            public String getSourceText(WatchData source)
            {
                return null;
            }

            @Override
            public String getScore(WatchData source)
            {
                return null;
            }
        });

        GenericAdapter<WatchData> watchDataAdapter = new GenericAdapter<>(getActivity(), controller.getWatchDataList(), itemHolder);
        gridView.setAdapter(watchDataAdapter);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

}
