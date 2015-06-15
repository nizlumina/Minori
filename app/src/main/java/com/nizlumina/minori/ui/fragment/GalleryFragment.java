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

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.common.nyaa.NyaaFansubGroup;
import com.nizlumina.minori.R;
import com.nizlumina.minori.Watchlist;
import com.nizlumina.minori.model.MinoriModel;
import com.nizlumina.minori.ui.activity.DrawerActivity;
import com.nizlumina.minori.ui.common.MarginItemDecoration;
import com.nizlumina.minori.utility.Util;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.SmallAnimeObject;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends DrawerActivity.DrawerFragment
{
    private static final String PARCELKEY_MINORIMODELS = "minori_models";
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<MinoriModel> models = new ArrayList<>();

    public static GalleryFragment newInstance()
    {
        return new GalleryFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PARCELKEY_MINORIMODELS, models);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        if (mToolbar != null)
        {

        }
        else Log.e("ERROR", "mToolbar is null");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fg_recylerview);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        boolean loaded = false;
        if (savedInstanceState != null)
        {
            ArrayList<MinoriModel> savedList = savedInstanceState.getParcelableArrayList(PARCELKEY_MINORIMODELS);
            if (savedList != null)
            {
                models.addAll(savedList);
                loaded = true;
            }
        }

        final FragmentActivity context = getActivity();
        final GalleryRecyclerAdapter adapter = new GalleryRecyclerAdapter(context, models);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new MarginItemDecoration(Util.dpToPx(context, 8)));
        mToolbar.setTitle(R.string.app_name);
        setDrawerNavigationButton(mToolbar);

        if (!loaded)
        {
            Log.v(getClass().getSimpleName(), "Not loaded");
            Watchlist.getInstance().getWatchDatasAsync(new Watchlist.OnFinishListener<ArrayList<MinoriModel>>()
            {
                @Override
                public void onFinish(final ArrayList<MinoriModel> result)
                {
                    Log.v(getClass().getSimpleName(), "Result received" + result.size());
                    if (mRecyclerView != null)
                    {
                        for (int i = 0; i < 50; i++)
                        {
                            models.add(getDemo());
                        }
                        adapter.notifyItemRangeInserted(0, 50);
                    }
                }
            }, new Handler(Looper.getMainLooper()));
        }
    }

    private MinoriModel getDemo()
    {

        CompositeData data = new CompositeData();
        final SmallAnimeObject smallAnimeObject = new SmallAnimeObject();
        smallAnimeObject.setPosterImage("http://cdn.myanimelist.net/images/anime/3/74387.jpg");
        data.setSmallAnimeObject(smallAnimeObject);
        NyaaFansubGroup group = new NyaaFansubGroup("UGU-SENPAI");
//        group.setSeriesTitle("Watashi Wakaranai Desu Dekinai ga Warau Don Don Sasquatch Oboeteru Gigavelo Machi");
        group.setSeriesTitle("Kekkai Yolo");
        group.setLatestEpisode(2);
        group.setModes(2);
        group.setId(3535);
        group.setTrustCategory(NyaaEntry.Trust.TRUSTED);
        group.getResolutions().add(NyaaEntry.Resolution.R720);


        return new MinoriModel(data, group);
    }

    @Override
    public String getFragmentTag()
    {
        return getClass().getSimpleName();
    }

    private static class GalleryRecyclerAdapter extends RecyclerView.Adapter<GalleryViewHolder>
    {
        private static final String nextEp = "Next Ep: ";
        private final LayoutInflater layoutInflater;
        private final MultiSelector multiSelector = new MultiSelector();
        private final List<MinoriModel> models;
        private final String[] modes;

        public GalleryRecyclerAdapter(Context context, @NonNull List<MinoriModel> models)
        {
            this.layoutInflater = LayoutInflater.from(context);
            this.modes = layoutInflater.getContext().getResources().getStringArray(R.array.array_modes);
            this.models = models;
        }

        @Override
        public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new GalleryViewHolder(layoutInflater.inflate(GalleryViewHolder.LAYOUT_RESOURCE, parent, false), multiSelector);
        }

        @Override
        public void onBindViewHolder(GalleryViewHolder holder, int position)
        {
            final MinoriModel minoriModel = models.get(position);
            final ImageView imageView = holder.getPosterImage();
            if (imageView != null)
            {
                Glide.with(layoutInflater.getContext())
                        .load(minoriModel.getCompositeData().getSmallAnimeObject().getPosterImage())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(imageView);
            }

            holder.getTitle().setText(minoriModel.getNyaaFansubGroup().getSeriesTitle());
            holder.getGroup().setText(minoriModel.getNyaaFansubGroup().getGroupName());
            holder.getTrust().setText(NyaaEntry.Trust.getTrustString(minoriModel.getNyaaFansubGroup().getTrustCategory()));

            holder.getResolution().setText(NyaaEntry.Resolution.getResolutionDisplayStringLinear(minoriModel.getNyaaFansubGroup().getResolutions()));

            holder.getEpisode().setText(nextEp + String.valueOf(minoriModel.getNyaaFansubGroup().getLatestEpisode() + 1));
            holder.getMode().setText(modes[minoriModel.getNyaaFansubGroup().getModes()]);
        }

        @Override
        public int getItemCount()
        {
            return models.size();
        }
    }

    private static class GalleryViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public static final int LAYOUT_RESOURCE = R.layout.listitem_gallery_small;
        private final MultiSelector mMultiSelectorRef;

        private final ImageView ivPosterImage;
        private final TextView tvTitle;
        private final TextView tvGroup;
        private final TextView tvTrust;
        private final TextView tvResolution;
        private final TextView tvEpisode;
        private final TextView tvMode;

        public GalleryViewHolder(View itemView, MultiSelector multiSelector)
        {
            super(itemView, multiSelector);
            ivPosterImage = (ImageView) itemView.findViewById(R.id.item_image);

            tvTitle = (TextView) itemView.findViewById(R.id.item_title);
            tvGroup = (TextView) itemView.findViewById(R.id.item_group);
            tvTrust = (TextView) itemView.findViewById(R.id.item_trust);
            tvResolution = (TextView) itemView.findViewById(R.id.item_resolution);
            tvEpisode = (TextView) itemView.findViewById(R.id.item_episode);
            tvMode = (TextView) itemView.findViewById(R.id.item_mode);

            mMultiSelectorRef = multiSelector;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public ImageView getPosterImage()
        {
            return ivPosterImage;
        }

        public TextView getTitle()
        {
            return tvTitle;
        }

        public TextView getGroup()
        {
            return tvGroup;
        }

        public TextView getTrust()
        {
            return tvTrust;
        }

        public TextView getResolution()
        {
            return tvResolution;
        }

        public TextView getEpisode()
        {
            return tvEpisode;
        }

        public TextView getMode()
        {
            return tvMode;
        }

        @Override
        public void onClick(View v)
        {
            if (mMultiSelectorRef.tapSelection(this))
            {

            }
            else
            {

            }
        }

        @Override
        public boolean onLongClick(View v)
        {
            mMultiSelectorRef.setSelectable(true);
            mMultiSelectorRef.setSelected(this, true);
            return false;
        }
    }

}
