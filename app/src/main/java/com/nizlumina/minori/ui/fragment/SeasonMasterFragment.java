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
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.SeasonIndexController;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.service.global.Director;
import com.nizlumina.minori.service.global.DirectorTask;
import com.nizlumina.minori.ui.activity.BaseDrawerActivity;
import com.nizlumina.minori.utility.Util;
import com.nizlumina.syncmaru.model.Season;

import java.util.ArrayList;
import java.util.List;

/**
 * A host fragment for SeasonFragment child tabs. Upon load, it acquires index from Firebase (or check its relevant cache) and populate child tabs as needed.
 */
public class SeasonMasterFragment extends BaseDrawerActivity.DrawerFragment
{
    private static final String FRAGMENT_TITLE = "Season Browser";
    private static final String REQID_GETINDEX = SeasonMasterFragment.class.getName() + "$id_getindex";
    private static final String ARG_ALREADYLOADED = "LOADED";
    private static String PALKEY_SAVEDSEASONS = SeasonMasterFragment.class.getSimpleName() + "$savedseasons";
    private final ArrayList<Season> mSeasons = new ArrayList<>();
    private final DirectorTask<List<Season>> mIndexTask = new DirectorTask<List<Season>>(REQID_GETINDEX, DirectorTask.RequestThread.NETWORK)
    {
        @Override
        public void run()
        {
            final SeasonIndexController controller = new SeasonIndexController();
            controller.getIndex(false, new OnFinishListener<List<Season>>()
            {
                @Override
                public void onFinish(List<Season> result)
                {
                    if (result != null)
                    {
                        publishResult(result);
                    }
                }
            });
        }
    };
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private final DirectorTask.Listener<List<Season>> mListener = new DirectorTask.Listener<List<Season>>()
    {
        @Override
        public void onProgress(int progress)
        {

        }

        @Override
        public void onFinish(List<Season> result)
        {
            if (result != null)
            {
                if (mSeasons.size() > 0)
                    mSeasons.clear();
                mSeasons.addAll(result);
            }

            setupViewPager();

            Director.getInstance().removeTask(REQID_GETINDEX);
        }
    };
    private Toolbar mToolbar;
    private boolean alreadyLoaded = false;

    public static SeasonMasterFragment newInstance()
    {
        return new SeasonMasterFragment();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mIndexTask.detach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PALKEY_SAVEDSEASONS, mSeasons);
        outState.putBoolean(ARG_ALREADYLOADED, alreadyLoaded);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.layout_tabparent, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.ltp_viewpager);
        mTabLayout = (TabLayout) view.findViewById(R.id.ltp_tablayout);
        mToolbar = (Toolbar) view.findViewById(R.id.ltp_toolbar);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            alreadyLoaded = savedInstanceState.getBoolean(ARG_ALREADYLOADED);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        setDrawerNavigationButton(mToolbar);
        mToolbar.setTitle(FRAGMENT_TITLE);

        mIndexTask.attach(mListener);
        //Only init first time
        if (savedInstanceState == null && !alreadyLoaded)
        {
            alreadyLoaded = true;
            mIndexTask.enqueue();
        }
        else
        {
            if (savedInstanceState != null)
            {
                final ArrayList<Season> savedSeasons = savedInstanceState.getParcelableArrayList(PALKEY_SAVEDSEASONS);
                if (savedSeasons != null)
                {
                    if (savedSeasons.size() == 0)
                    {
                        //only returns true due to double rotation bug (go to detail, rotate twice, then return to the season browser)
                        //the bundle is there but the value is lost. I know right?
                        alreadyLoaded = true;
                        mIndexTask.enqueue();
                    }
                    else
                    {
                        mSeasons.clear();
                        mSeasons.addAll(savedSeasons);
                        setupViewPager(); // Don't ever View.post() this. Parent of a nested fragment is very fickle. Such is life.
                    }
                }
            }
            else
            {
                setupViewPager();
            }
        }
    }

    private void setupViewPager()
    {
        Util.postOnPreDraw(mViewPager, new Runnable()
        {
            @Override
            public void run()
            {
                final SeasonPagerAdapter adapter = new SeasonPagerAdapter(getChildFragmentManager(), mSeasons);
                mViewPager.setAdapter(adapter);
                mTabLayout.setupWithViewPager(mViewPager);
                mViewPager.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        int currentItem = mSeasons.size() - 1;
                        mViewPager.setCurrentItem(currentItem);
                        mTabLayout.getTabAt(currentItem).select();
                    }
                });
            }
        });
    }

    private static class SeasonPagerAdapter extends FragmentStatePagerAdapter
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
