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

package com.nizlumina.minori.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.HummingbirdNetworkController;
import com.nizlumina.minori.android.listener.NetworkListener;
import com.nizlumina.minori.android.ui.activity.DrawerActivity;
import com.nizlumina.minori.android.ui.adapter.GenericAdapter;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.util.ArrayList;

public class UpcomingFragment extends Fragment
{
    GridView mGridView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mGridView = (GridView) inflater.inflate(R.layout.layout_gridview, container, false);
        return mGridView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        DrawerActivity drawerActivity = ((DrawerActivity) getActivity());
        Toolbar toolbar = drawerActivity.getToolbar();
        toolbar.setTitle("Upcoming");

        setupGridView(mGridView);
    }

    private void setupGridView(GridView gridView)
    {
        final HummingbirdNetworkController controller = new HummingbirdNetworkController();
        final GenericAdapter<AnimeObject> watchDataAdapter = new GenericAdapter<>(getActivity(), new ArrayList<AnimeObject>(), new GalleryItemHolder());

        watchDataAdapter.setNotifyOnChange(true);
        gridView.setAdapter(watchDataAdapter);

        controller.getUpcomingAnimeObjectAsync(new NetworkListener<AnimeObject>()
        {

            int count = 0;

            @Override
            public void onEachSuccessfulResponses(final AnimeObject animeObject)
            {
                count++;
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        watchDataAdapter.add(animeObject);
                    }
                });
            }

            @Override
            public void onFinish()
            {
                Log.v(UpcomingFragment.class.getSimpleName(), "Results count: " + count);
            }
        }, false);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    private static class GalleryItemHolder implements GenericAdapter.ViewHolder<AnimeObject>
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
        public GenericAdapter.ViewHolder<AnimeObject> getNewInstance()
        {
            return new GalleryItemHolder();
        }

        @Override
        public GenericAdapter.ViewHolder<AnimeObject> setupViewSource(View inflatedConvertView)
        {
            image = (ImageView) inflatedConvertView.findViewById(R.id.item_image);
            title = (TextView) inflatedConvertView.findViewById(R.id.item_title);
            group = (TextView) inflatedConvertView.findViewById(R.id.item_group);
            episode = (TextView) inflatedConvertView.findViewById(R.id.item_episode);
            return this;
        }

        @Override
        public void applySource(Context context, AnimeObject source)
        {
            if (image != null)
            {
                Glide.with(context).load(source.getImageUrl()).into(image);
            }
            if (title != null)
                title.setText(source.getTitle());
            if (group != null)
                group.setText("");

            if (episode != null)
                episode.setVisibility(View.GONE);
            //episode.setText(String.valueOf(source.episodeCount));

        }
    }
}
