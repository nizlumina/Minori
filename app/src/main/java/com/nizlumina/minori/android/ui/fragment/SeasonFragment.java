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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.SeasonDataController;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.ui.adapter.FragmentPagerAdapter;
import com.nizlumina.minori.android.ui.adapter.GenericAdapter;
import com.nizlumina.minori.android.ui.gallery.GalleryItemHolder;
import com.nizlumina.minori.android.ui.gallery.GalleryPresenter;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.Season;

import java.util.ArrayList;
import java.util.List;

public class SeasonFragment extends Fragment implements FragmentPagerAdapter.TitledFragment
{
    private GridView mGridView;
    private SeasonDataController mSeasonDataController = new SeasonDataController();
    private List<CompositeData> mCompositeDatas = new ArrayList<>();
    private GenericAdapter<CompositeData> mCompositeDataAdapter;
    private Season mSeason;

    public static SeasonFragment newInstance(Season season)
    {
        SeasonFragment seasonFragment = new SeasonFragment();
        seasonFragment.mSeason = season;
        return seasonFragment;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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

        if (mGridView != null) setupGridView();
    }

    private void setupGridView()
    {
        GalleryPresenter<CompositeData> compositeDataPresenter = new GalleryPresenter<CompositeData>()
        {
            @Override
            public void loadInto(final ImageView imageView, CompositeData source)
            {
                Glide.with(SeasonFragment.this).load(source.getSmallAnimeObject().getPosterImage()).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
            }

            @Override
            public String getTitle(CompositeData source)
            {
                return source.getMalObject().getTitle();
            }

            @Override
            public String getGroup(CompositeData source)
            {
                return null;
            }

            @Override
            public String getEpisode(CompositeData source)
            {
                return null;
            }
        };

        GalleryItemHolder<CompositeData> compositeDataHolder = new GalleryItemHolder<CompositeData>(compositeDataPresenter);
        compositeDataHolder.setVisibility(View.VISIBLE, View.VISIBLE, View.GONE, View.GONE);

        mCompositeDataAdapter = new GenericAdapter<>(getActivity(), mCompositeDatas, compositeDataHolder);
        mCompositeDataAdapter.setNotifyOnChange(true);

        mGridView.setAdapter(mCompositeDataAdapter);

        mSeasonDataController.getCompositeDatas(new OnFinishListener<List<CompositeData>>()
        {
            @Override
            public void onFinish(final List<CompositeData> result)
            {
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (mCompositeDataAdapter.getCount() > 0) mCompositeDataAdapter.clear();
                        mCompositeDataAdapter.addAll(result);
                        Log.v(SeasonFragment.class.getSimpleName(), "Result size: " + result.size());
                    }
                });
            }
        }, false);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        //if (mToolbar != null) mToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public CharSequence getTitle()
    {
        return mSeason.getSeason() + String.valueOf(mSeason.getYear()).substring(2);
    }
}
