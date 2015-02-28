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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.SeasonController;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.ui.adapter.GenericAdapter;
import com.nizlumina.minori.android.ui.gallery.GalleryItemHolder;
import com.nizlumina.minori.android.ui.gallery.GalleryPresenter;
import com.nizlumina.minori.android.ui.view.CustomGridView;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.LiveChartObject;
import com.nizlumina.syncmaru.model.Season;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SeasonFragment extends Fragment
{
    boolean mInitialized = false;
    private CustomGridView mGridView;
    private SeasonController mSeasonController;
    private List<CompositeData> mCompositeDatasForDisplay = new ArrayList<>();
    private GenericAdapter<CompositeData> mCompositeDataAdapter;
    private Season mSeason;

    public static SeasonFragment newInstance(Season season)
    {
        SeasonFragment seasonFragment = new SeasonFragment();
        seasonFragment.mSeason = season;
        seasonFragment.mSeasonController = new SeasonController(season);
        return seasonFragment;
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
        mGridView = (CustomGridView) inflater.inflate(R.layout.layout_gridview, container, false);
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
        if (!mInitialized)
        {
            mInitialized = true;
            GalleryPresenter<CompositeData> compositeDataPresenter = new GalleryPresenter<CompositeData>()
            {
                @Override
                public void loadInto(final ImageView imageView, CompositeData source)
                {
                    Glide.with(SeasonFragment.this).load(source.getMalObject().getImage()).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
                }

                @Override
                public String getTitle(CompositeData source)
                {
                    return source.getLiveChartObject().getTitle();
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


                @Override
                public String getSourceText(CompositeData source)
                {
                    return source.getLiveChartObject().getSource();
                }

                private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

                @Override
                public String getScore(CompositeData source)
                {
                    return decimalFormat.format(source.getMalObject().getScore());
                }
            };

            GalleryItemHolder<CompositeData> compositeDataHolder = new GalleryItemHolder<CompositeData>(compositeDataPresenter)
            {
                @Override
                protected void modifyViewProperties(TextView titleView, ImageView imageView, TextView groupView, TextView episodeView)
                {
                    groupView.setVisibility(View.GONE);
                    episodeView.setVisibility(View.GONE);
                }
            };

            mCompositeDataAdapter = new GenericAdapter<>(getActivity(), mCompositeDatasForDisplay, compositeDataHolder);
            mCompositeDataAdapter.setNotifyOnChange(true);
            mGridView.setAdapter(mCompositeDataAdapter);

            mGridView.setOnScrollListener(new AbsListView.OnScrollListener()
            {
                private int mInitialScroll = 0;

                boolean scrolling = false;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState)
                {
//                    if (scrollState == SCROLL_STATE_IDLE)
//                        scrolling = false;
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
                {
//                    int scrollOffset = mGridView.getVerticalScrollOffset();
//                    boolean scrollUp = scrollOffset > mInitialScroll;
//                    mInitialScroll = scrollOffset;
//
//                    if (!scrolling)
//                    {
////                        log("Hide container");
////                        if(getActivity() instanceof DrawerActivity)
////                            ((DrawerActivity) getActivity()).hideTopContainer();
////                        if (!scrollUp)
////                        {
////
////
////                        }
////                        scrolling = true;
//                    }
////                    else if(scrollUp & !containerShown)
////                    {
////                        ((DrawerActivity) getActivity()).showTopContainer();
////                        containerShown = true;
////
////                        log("Show container");
////                    }
                }
            });

            buildGridItems(false);
        }
    }

    private void buildGridItems(final boolean forceRefresh)
    {
        log("Composite data get initiated");
        mSeasonController.getCompositeDatas(new OnFinishListener<List<CompositeData>>()
        {
            @Override
            public void onFinish(final List<CompositeData> result)
            {
                log("Composite datas loaded: " + result.size());

                //Even though the result is complete, we only display series for now (since that's what people will use it for anyway).
                //Sectioning parts of the UI for complete results will be decided later in the future
                List<CompositeData> filteredList = new ArrayList<CompositeData>();
                for (CompositeData compositeData : result)
                {
                    if (compositeData.getLiveChartObject().getCategory() == LiveChartObject.Category.TV)
                        filteredList.add(compositeData);
                }

                Collections.sort(filteredList, new Comparator<CompositeData>()
                {
                    @Override
                    public int compare(CompositeData lhs, CompositeData rhs)
                    {
                        //return lhs.getLiveChartObject().getTitle().compareTo(rhs.getLiveChartObject().getTitle());
                        //try by rating
                        return Float.compare(rhs.getMalObject().getScore(), lhs.getMalObject().getScore());
                    }
                });

                if (mCompositeDataAdapter.getCount() > 0)
                    mCompositeDataAdapter.clear();
                mCompositeDataAdapter.addAll(filteredList);
                log("Adapter set with new composite datas");

            }
        }, forceRefresh);
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

    private void log(String input)
    {
        Log.v(getClass().getSimpleName() + " - " + mSeason.getIndexKey(), input);
    }

}
