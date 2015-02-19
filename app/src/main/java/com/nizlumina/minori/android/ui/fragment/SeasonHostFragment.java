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

package com.nizlumina.minori.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.nizlumina.minori.android.common.SlidingTabLayout;
import com.nizlumina.minori.android.controller.SeasonDataIndexController;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.syncmaru.model.Season;

import java.util.Collections;
import java.util.List;

/**
 * A host fragment for SeasonFragment child tabs. Upon load, it acquires index from Firebase (or check its relevant cache) and populate child tabs as needed.
 */
public class SeasonHostFragment extends TabbedFragment
{

    private static final String FRAGMENT_TAG = "season_host_fragment";
    private final SeasonDataIndexController mIndexController = new SeasonDataIndexController();
    //private List<Fragment> mFragmentList = new ArrayList<>();

    public static String getFragmentTag()
    {
        return FRAGMENT_TAG;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null)
        {
            Log.v(getClass().getSimpleName() + " OnViewCreated", "Saved instance state is null");
            log("Loading index.");
            onLoad(getTabLayout(), getContentViewPager());
        }
    }

//    @Override
//    void setup(SlidingTabLayout tabLayout, ViewPager viewPager)
//    {
//
//    }

    private void onLoad(@NonNull final SlidingTabLayout tabLayout, @NonNull final ViewPager viewPager)
    {
        mIndexController.loadIndex(new OnFinishListener<Void>()
        {
            @Override
            public void onFinish(Void result)
            {
                log("Index loaded");
                //if (mFragmentList.size() > 0) mFragmentList.clear();
                final List<Season> mSeasons = mIndexController.getSeasonList();
                Collections.reverse(mSeasons);
//                for (Season season : mSeasons)
//                {
//                    mFragmentList.add(SeasonFragment.newInstance(season));
//                }
//                log("FragmentSize: " + mFragmentList.size());

                if (SeasonHostFragment.this.isVisible())
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (getLoadingPlaceholder().getVisibility() != View.GONE)
                                getLoadingPlaceholder().setVisibility(View.GONE);
                            //SimplePagerAdapter pagerAdapter = new SimplePagerAdapter(getChildFragmentManager(), mFragmentList);
                            FragmentStatePagerAdapter pagerAdapter = new FragmentStatePagerAdapter(getChildFragmentManager())
                            {
                                @Override
                                public Fragment getItem(int position)
                                {
                                    return SeasonFragment.newInstance(mSeasons.get(position));
                                }

                                @Override
                                public int getCount()
                                {
                                    return mSeasons.size();
                                }

                                @Override
                                public CharSequence getPageTitle(int position)
                                {
                                    return mSeasons.get(position).getIndexKey();
                                }
                            };
                            viewPager.setAdapter(pagerAdapter);
                            tabLayout.setViewPager(viewPager);
                            log("Pager Adapter init - Size: " + pagerAdapter.getCount());
                            viewPager.setVisibility(View.VISIBLE);
                            viewPager.setCurrentItem(mSeasons.indexOf(mIndexController.getCurrentSeason()));
                        }
                    });
                }
            }
        }, false);

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    private void log(String input)
    {
        Log.v(getClass().getSimpleName(), input);
    }

    private void test()
    {

    }
}
