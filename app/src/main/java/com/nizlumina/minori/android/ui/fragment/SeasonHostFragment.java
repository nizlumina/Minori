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
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.SeasonDataIndexController;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.model.SeasonType;
import com.nizlumina.minori.android.ui.common.SlidingTabLayout;
import com.nizlumina.minori.android.utility.Util;
import com.nizlumina.syncmaru.model.Season;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A host fragment for SeasonFragment child tabs. Upon load, it acquires index from Firebase (or check its relevant cache) and populate child tabs as needed.
 */
public class SeasonHostFragment extends TabbedFragment
{
    private static final String FRAGMENT_TAG = "season_host_fragment";
    private final SeasonDataIndexController mIndexController = new SeasonDataIndexController();

    private WeakReference<Listener> mFragmentListenerRef;
    private ViewGroup mTabContainer;
    private SlidingTabLayout mTabLayout;

    public static String getFragmentTag()
    {
        return FRAGMENT_TAG;
    }

    public static SeasonHostFragment newInstance(Listener fragmentListener)
    {
        SeasonHostFragment seasonHostFragment = new SeasonHostFragment();
        seasonHostFragment.mFragmentListenerRef = new WeakReference<Listener>(fragmentListener);
        return seasonHostFragment;
    }

    public SlidingTabLayout getChildTabLayout()
    {
        return mTabLayout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Listener fragmentListener = mFragmentListenerRef.get();
        if (fragmentListener != null)
        {
            mTabContainer = fragmentListener.getContainerForTabs();

            SlidingTabLayout tabLayout = new SlidingTabLayout(getActivity());
            tabLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Util.dpToPx(getActivity(), 48)));
            tabLayout.setBackgroundColor(getResources().getColor(R.color.primary_color));
            tabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.primary_color_dark));
            mTabContainer.addView(tabLayout);

            mTabLayout = tabLayout;
        }

        //injectTabsToParent(view);

        if (savedInstanceState == null)
        {
            Log.w(getClass().getSimpleName() + " OnViewCreated", "Saved instance state is null");
            log("Loading index.");
            onLoad(getChildTabLayout(), getContentViewPager());
        }
    }

//    private void removeParentTab(View view)
//    {
//        if (view instanceof ViewGroup)
//        {
//            ViewGroup parent = (ViewGroup) getTabLayout().getParent();
//
//            parent.removeView(getTabLayout());
//        }
//    }

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
        }, false);

    }

    @Override
    public void onDestroyView()
    {
//        Listener fragmentListener = mFragmentListenerRef.get();
//        if (fragmentListener != null) fragmentListener.removeTabLayout(getTabLayout());

        super.onDestroyView();
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

    private void buildViews(final List<Season> mSeasons, ViewPager viewPager, SlidingTabLayout tabLayout)
    {
        viewPager.setOffscreenPageLimit(1);
        getLoadingPlaceholder().setVisibility(View.GONE);
        FragmentStatePagerAdapter pagerAdapter = new FragmentStatePagerAdapter(getFragmentManager())
        {
            @Override
            public Fragment getItem(int position)
            {
                return SeasonFragment.newInstance(mSeasons.get(position), (SeasonFragment.Listener) getActivity());
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
        log("Pager Adapter init - Size: " + pagerAdapter.getCount());
        viewPager.setVisibility(View.VISIBLE);
        viewPager.setCurrentItem(mSeasons.indexOf(mIndexController.getCurrentSeason()));
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

    public void addTabLayout(final View tabLayout)
    {
        mTabContainer.addView(tabLayout);
//        if (mTopContainerBaseHeight < 0)
//            mTopContainerBaseHeight = getToolbarContainer().getHeight();
//
//        tabLayout.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);
//
//        final int endHeight = mTopContainerBaseHeight + tabLayout.getHeight();
//
//        Log.v(getClass().getSimpleName(),"Container Height: " + mTopContainerBaseHeight + "\nTarget height: " + endHeight + "\nTabHeight: " +  tabLayout.getMeasuredHeight() + "|" + tabLayout.getHeight());
//
//        getToolbarContainer().addView(tabLayout);
//        final ValueAnimator expandAnimator = layoutHeightAnimator(getToolbarContainer(), endHeight);
//        expandAnimator.addListener(new Animator.AnimatorListener()
//        {
//            @Override
//            public void onAnimationStart(Animator animation)
//            {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation)
//            {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation)
//            {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation)
//            {
//
//            }
//        });
//
//        expandAnimator.start();


    }

    public void removeTabLayout(final View tabLayout)
    {
        mTabContainer.removeView(tabLayout);
//        final ValueAnimator expandAnimator = layoutHeightAnimator(getToolbarContainer(), mTopContainerBaseHeight);
//        expandAnimator.addListener(new Animator.AnimatorListener()
//        {
//            @Override
//            public void onAnimationStart(Animator animation)
//            {
//                getToolbarContainer().removeView(tabLayout);
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation)
//            {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation)
//            {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation)
//            {
//
//            }
//        });
//
//        expandAnimator.start();

    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy()
    {
        removeTabLayout(mTabLayout);
        super.onDestroy();
    }

    public static interface Listener
    {
        ViewGroup getContainerForTabs();
    }
}
