/*
 * Where applicable, this Minori project follows the standard MIT license as below:
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.SeasonDataIndexController;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.model.SeasonType;
import com.nizlumina.minori.ui.ToolbarContract;
import com.nizlumina.minori.ui.common.SlidingTabLayout;
import com.nizlumina.syncmaru.model.Season;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A host fragment for SeasonFragment child tabs. Upon load, it acquires index from Firebase (or check its relevant cache) and populate child tabs as needed.
 */
public class SeasonMasterFragment extends DrawerContentFragment
{
    private final SeasonDataIndexController mIndexController = new SeasonDataIndexController();
    private final String fragmentTitle = "Season Browser";

    private SoftReference<DrawerFragmentListener> mFragmentListenerRef;
    private SlidingTabLayout mTabLayout;
    private ViewPager mViewPager;

    public static SeasonMasterFragment newInstance(DrawerFragmentListener fragmentListener)
    {
        SeasonMasterFragment seasonTabHostFragment = new SeasonMasterFragment();
        seasonTabHostFragment.mFragmentListenerRef = new SoftReference<>(fragmentListener);
        return seasonTabHostFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return mViewPager = (ViewPager) inflater.inflate(R.layout.view_viewpager, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        disableExtraTopPadding();
        super.onViewCreated(view, savedInstanceState);

        final LayoutInflater inflater = LayoutInflater.from(getActivity());

        final ToolbarContract toolbarContract = getToolbarContract();
        toolbarContract.getToolbar().setTitle(fragmentTitle);
        mTabLayout = (SlidingTabLayout) toolbarContract.setToolbarSiblingView(inflater, R.layout.view_slidingtab);
        onLoad(mTabLayout, mViewPager);
    }

    private void onLoad(@NonNull final SlidingTabLayout tabLayout, @NonNull final ViewPager viewPager)
    {
        mIndexController.loadIndex(new OnFinishListener<Void>()
        {
            @Override
            public void onFinish(Void result)
            {
                log("Index loaded");
                final List<Season> mSeasons = getSeasonsListFromController();

                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            buildViews(mSeasons, viewPager, tabLayout);
                        }
                    });
            }
        }, false); //todo:reevaluate index force refresh
    }

    private List<Season> getSeasonsListFromController()
    {
        final List<Season> mSeasons = mIndexController.getSeasonList();
        Collections.sort(mSeasons, new Comparator<Season>()
        {
            @Override
            public int compare(Season lhs, Season rhs)
            {
                if (lhs.getYear() == rhs.getYear())
                    return SeasonType.fromString(lhs.getSeason()).compareTo(SeasonType.fromString(rhs.getSeason()));
                return lhs.getYear() - rhs.getYear();
            }
        });
        return mSeasons;
    }

    private void buildViews(final List<Season> mSeasons, final ViewPager viewPager, final SlidingTabLayout tabLayout)
    {
        viewPager.setOffscreenPageLimit(1);
        final ToolbarContract toolbarContract = getToolbarContract();

        final FragmentStatePagerAdapter pagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager())
        {
            @Override
            public Fragment getItem(int position)
            {

                SeasonFragment seasonFragment = SeasonFragment.newInstance(mSeasons.get(position), mFragmentListenerRef.get(), toolbarContract.getAutoDisplayToolbarListener());
                Bundle args = new Bundle();
                args.putInt(SeasonFragment.ARGS_GRIDVIEW_PADDING_TOP, toolbarContract.getToolbarContainer().getMeasuredHeight());
                seasonFragment.setArguments(args);
                return seasonFragment;
            }

            @Override
            public int getCount()
            {
                return mSeasons.size();
            }

            @Override
            public CharSequence getPageTitle(int position)
            {
                Season season = mSeasons.get(position);
                String year = String.valueOf(season.getYear());
                return season.getSeason() + " " + year.substring(year.length() - 2, year.length());
            }
        };

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setViewPager(viewPager);
        tabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                if (toolbarContract.isToolbarVisible())
                    toolbarContract.showToolbar();
            }

            @Override
            public void onPageSelected(int position)
            {

            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        log("Pager Adapter init - Size: " + pagerAdapter.getCount());
        View view = getView();

        if (view != null)
        {
            view.post(new Runnable()
            {
                @Override
                public void run()
                {
                    int position = mSeasons.indexOf(mIndexController.getCurrentSeason());
                    viewPager.setCurrentItem(position);
                }
            });
        }
    }

    private void log(String input)
    {
        Log.v(getClass().getSimpleName(), input);
    }
}
