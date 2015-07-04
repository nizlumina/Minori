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

package com.nizlumina.minori.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.SeasonIndexController;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.service.global.Director;
import com.nizlumina.minori.service.global.DirectorTask;
import com.nizlumina.minori.ui.fragment.SeasonFragment;
import com.nizlumina.syncmaru.model.Season;

import java.util.ArrayList;
import java.util.List;

public class SeasonBrowserActivity extends BaseDrawerActivity
{
    private static final String FRAGMENT_TITLE = "Season Browser";
    private static final String CLASSTAG = SeasonBrowserActivity.class.getSimpleName();
    private static final String REQID_GETINDEX = CLASSTAG + "$id_getindex";
    private static final String ARG_ALREADYLOADED = CLASSTAG + "$loaded";
    private static final String PALKEY_SAVEDSEASONS = CLASSTAG + "$savedseasons";
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

    private boolean mAlreadyLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
        {
            mAlreadyLoaded = savedInstanceState.getBoolean(ARG_ALREADYLOADED);
        }

        setupViews(savedInstanceState);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mIndexTask.detach();
    }

    @Override
    public int getDrawerItemId()
    {
        return R.id.mm_nav_seasonbrowser;
    }


    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PALKEY_SAVEDSEASONS, mSeasons);
        outState.putBoolean(ARG_ALREADYLOADED, mAlreadyLoaded);
    }

    public void setupViews(@Nullable Bundle savedInstanceState)
    {
        View view = inflateContent(R.layout.layout_tabparent);
        mViewPager = (ViewPager) view.findViewById(R.id.ltp_viewpager);
        mTabLayout = (TabLayout) view.findViewById(R.id.ltp_tablayout);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.ltp_toolbar);

        setDrawerNavigationButton(toolbar);
        toolbar.setTitle(FRAGMENT_TITLE);

        mIndexTask.attach(mListener);
        //Only init first time
        if (savedInstanceState == null && !mAlreadyLoaded)
        {
            mAlreadyLoaded = true;
            mIndexTask.enqueue();
        }
        else
        {
            if (savedInstanceState != null)
            {
                final ArrayList<Season> savedSeasons = savedInstanceState.getParcelableArrayList(PALKEY_SAVEDSEASONS);
                if (savedSeasons != null)
                {
                    if (savedSeasons.size() != 0)
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
        final SeasonPagerAdapter adapter = new SeasonPagerAdapter(getSupportFragmentManager(), mSeasons);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(adapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.post(new Runnable()
        {
            @Override
            public void run()
            {
                int currentItem = mSeasons.size() - 1;
                mViewPager.setCurrentItem(currentItem);
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
