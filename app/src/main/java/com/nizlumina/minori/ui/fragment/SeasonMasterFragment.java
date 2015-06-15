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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.SeasonDataIndexController;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.model.SeasonType;
import com.nizlumina.minori.ui.activity.DrawerActivity2;
import com.nizlumina.syncmaru.model.Season;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A host fragment for SeasonFragment child tabs. Upon load, it acquires index from Firebase (or check its relevant cache) and populate child tabs as needed.
 */
public class SeasonMasterFragment extends DrawerActivity2.DrawerFragment
{
    private final static String FRAGMENT_TITLE = "Season Browser";
    private final SeasonDataIndexController mIndexController = new SeasonDataIndexController();
    //for sorting
    private final Comparator<Season> seasonComparator = new Comparator<Season>()
    {
        @Override
        public int compare(Season lhs, Season rhs)
        {
            if (lhs.getYear() == rhs.getYear())
            {
                final SeasonType a = SeasonType.fromString(lhs.getSeason());
                final SeasonType b = SeasonType.fromString(rhs.getSeason());
                if (a != null && b != null)
                {
                    return a.compareTo(b);
                }
            }
            return lhs.getYear() - rhs.getYear();
        }
    };

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    public static SeasonMasterFragment newInstance()
    {
        return new SeasonMasterFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_seasonmaster, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.fsm_viewpager);
        mTabLayout = (TabLayout) view.findViewById(R.id.fsm_tablayout);
        //AppBarLayout appBar = (AppBarLayout) view.findViewById(R.id.fsm_appbarlayout);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        onLoad(mTabLayout, mViewPager);
    }

    private void onLoad(@NonNull final TabLayout tabLayout, @NonNull final ViewPager viewPager)
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
        Collections.sort(mSeasons, seasonComparator);
        return mSeasons;
    }

    private void buildViews(final List<Season> mSeasons, final ViewPager viewPager, final TabLayout tabLayout)
    {
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new SeasonPagerAdapter(getChildFragmentManager(), mSeasons));
        tabLayout.setupWithViewPager(viewPager);

        int position = mSeasons.indexOf(mIndexController.getCurrentSeason());
        viewPager.setCurrentItem(position);

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


    private static class SeasonPagerAdapter extends FragmentPagerAdapter
    {
        private final List<Season> seasons;

        public SeasonPagerAdapter(FragmentManager fm, List<Season> seasons)
        {
            super(fm);
            this.seasons = seasons;
        }

        @Override
        public Fragment getItem(int position)
        {
            return SeasonFragment.newInstance(seasons.get(position));
        }

        @Override
        public int getCount()
        {
            return seasons.size();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return seasons.get(position).getDisplayString();
        }
    }
}
