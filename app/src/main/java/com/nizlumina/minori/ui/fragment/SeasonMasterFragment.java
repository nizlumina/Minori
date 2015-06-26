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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.SeasonIndexController;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.service.global.GlobalService;
import com.nizlumina.minori.service.global.ServiceTask;
import com.nizlumina.minori.ui.activity.DrawerActivity;
import com.nizlumina.syncmaru.model.Season;

import java.util.ArrayList;
import java.util.List;

/**
 * A host fragment for SeasonFragment child tabs. Upon load, it acquires index from Firebase (or check its relevant cache) and populate child tabs as needed.
 */
public class SeasonMasterFragment extends DrawerActivity.DrawerFragment
{
    private static final String FRAGMENT_TITLE = "Season Browser";
    private static final String REQID_GETINDEX = SeasonMasterFragment.class.getName() + "$id_getindex";
    private static String PALKEY_SAVEDSEASONS = SeasonMasterFragment.class.getSimpleName() + "$savedseasons";
    private final ArrayList<Season> mSeasons = new ArrayList<>();
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private Toolbar mToolbar;

    private GlobalService.ServiceBroadcastReceiver broadcastReceiver = new GlobalService.ServiceBroadcastReceiver()
    {
        @Override
        public void onProgress(String requestId, int progress)
        {

        }

        @Override
        public void onFinish(String requestId)
        {
            if (requestId.equals(REQID_GETINDEX))
            {
                List<Season> results = GlobalService.takeResult(getActivity(), requestId);
                if (results != null)
                {
                    if (mSeasons.size() > 0)
                        mSeasons.clear();
                    mSeasons.addAll(results);
                }
                //make sure we post this
                mViewPager.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        setupViewPager();
                    }
                });
                //     Log.v(getFragmentTag(), "OF - " + results);
            }
        }
    };

    public static SeasonMasterFragment newInstance()
    {
        return new SeasonMasterFragment();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        LocalBroadcastManager.getInstance(activity).registerReceiver(broadcastReceiver, GlobalService.ServiceBroadcastReceiver.getIntentFilter());
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PALKEY_SAVEDSEASONS, mSeasons);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        //Log.v(getFragmentTag(), "OC - " + savedInstanceState);
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null)
        {

        }
        else
        {
            //if this isn't null, result hence was already retrieved
            ArrayList<Season> savedSeasons = savedInstanceState.getParcelableArrayList(PALKEY_SAVEDSEASONS);

            if (savedSeasons != null && mSeasons.size() != savedSeasons.size())
            {
                mSeasons.clear();
                mSeasons.addAll(savedSeasons);
            }
            else
            {
                //if above is false and this is true, this means result was already there but we missed the broadcast due to rotation
                if (GlobalService.isRequestCompleted(REQID_GETINDEX))
                {
                    GlobalService.rebroadcastCompletion(getActivity(), REQID_GETINDEX);
                }
                else if (GlobalService.isRequestEnqueued(REQID_GETINDEX)) //if above is not true, this then means the request is still being processed but not completed
                {
                    //do nothing since receiver already registered so we'll just wait
                }

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_seasonmaster, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.fsm_viewpager);
        mTabLayout = (TabLayout) view.findViewById(R.id.fsm_tablayout);
        mToolbar = (Toolbar) view.findViewById(R.id.fsm_toolbar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        setDrawerNavigationButton(mToolbar);
        mToolbar.setTitle(FRAGMENT_TITLE);

        //Only init first time
        if (savedInstanceState == null)
        {
            final ServiceTask getIndexTask = new ServiceTask(REQID_GETINDEX, ServiceTask.RequestThread.NETWORK)
            {
                @Override
                public void run()
                {
                    SeasonIndexController controller = new SeasonIndexController();
                    controller.getIndex(false, new OnFinishListener<List<Season>>()
                    {
                        @Override
                        public void onFinish(List<Season> result)
                        {
                            if (result != null)
                            {
                                setResult(result);
                            }
                        }
                    });
                }
            };
            GlobalService.startRequest(getActivity(), getIndexTask);
        }
        else
        {
            setupViewPager();
        }
    }

    private void setupViewPager()
    {
        SeasonPagerAdapter adapter = new SeasonPagerAdapter(getChildFragmentManager(), mSeasons);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(2);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(mSeasons.size() - 1);
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
