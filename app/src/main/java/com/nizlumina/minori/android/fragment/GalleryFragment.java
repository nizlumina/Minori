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

package com.nizlumina.minori.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nizlumina.minori.R;
import com.nizlumina.minori.android.activity.DrawerActivity;
import com.nizlumina.minori.android.adapter.GenericAdapter;
import com.nizlumina.minori.android.controller.WatchlistController;
import com.nizlumina.minori.android.model.WatchData;

public class GalleryFragment extends Fragment
{
    private GridView mGridView;

    public GalleryFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //return super.onCreateView(inflater, container, savedInstanceState);
        mGridView = (GridView) inflater.inflate(R.layout.fragment_gridview, container, false);
        return mGridView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        DrawerActivity drawerActivity = ((DrawerActivity) getActivity());
        Toolbar toolbar = drawerActivity.getToolbar();
        toolbar.setTitle(R.string.app_name);

        drawerActivity.getFabMain().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                getFragmentManager().beginTransaction().replace(R.id.base_contentfragment, new SearchFragment()).addToBackStack(GalleryFragment.class.getSimpleName()).commit();
            }
        });

        drawerActivity.setupFab(drawerActivity.getFabMain(), R.drawable.ic_add_black_24dp);
        drawerActivity.setupFab(drawerActivity.getFabMini(), R.drawable.ic_refresh_black_24dp);

        setupGridView(mGridView);
    }

    private void setupGridView(GridView gridView)
    {
        WatchlistController controller = new WatchlistController();
        GenericAdapter<WatchData> watchDataAdapter = new GenericAdapter<>(getActivity(), controller.getWatchDataList(), new GalleryItemHolder());
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

    private static class GalleryItemHolder implements GenericAdapter.ViewHolder<WatchData>
    {

        private ImageView image;
        private TextView title;
        private TextView group;
        private TextView episode;

        @Override
        public int getLayoutResource()
        {
            return R.layout.list_item_gallery_compact;
        }

        @Override
        public GenericAdapter.ViewHolder<WatchData> getNewInstance()
        {
            return new GalleryItemHolder();
        }

        @Override
        public GenericAdapter.ViewHolder<WatchData> setupViewSource(View inflatedConvertView)
        {
            image = (ImageView) inflatedConvertView.findViewById(R.id.item_image);
            title = (TextView) inflatedConvertView.findViewById(R.id.item_title);
            group = (TextView) inflatedConvertView.findViewById(R.id.item_group);
            episode = (TextView) inflatedConvertView.findViewById(R.id.item_episode);
            return this;
        }

        @Override
        public void applySource(Context context, WatchData source)
        {
            if (image != null)
            {
                Glide.with(context).load(source.getAnimeObject().imageUrl).into(image);
            }
            if (title != null)
                title.setText(source.getAnimeObject().title);
            if (group != null)
                group.setText(source.getNyaaEntry().fansub);

            if (episode != null)
                episode.setText(source.getNyaaEntry().episodeString);

        }
    }
}
