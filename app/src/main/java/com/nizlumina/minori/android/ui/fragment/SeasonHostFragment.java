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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

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
    private SlidingTabLayout mTabLayout;
    private int mTabContainerOriginalHeight;

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

        //injectTabsToParent(view);

        if (savedInstanceState == null)
        {
            Listener fragmentListener = mFragmentListenerRef.get();
            if (fragmentListener != null)
            {
                addTabLayout(fragmentListener.getContainerForTabs());
            }

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
        Listener fragmentListener = mFragmentListenerRef.get();
        if (fragmentListener != null)
        {
            removeTabLayout(fragmentListener.getContainerForTabs());


        }

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
        if (getLoadingPlaceholder().getVisibility() != View.GONE)
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
        int position = mSeasons.indexOf(mIndexController.getCurrentSeason());
        viewPager.setCurrentItem(position);
        mTabLayout.explicitScrollTo(position);
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

    public void addTabLayout(final ViewGroup tabContainer)
    {
        log("Tab added");
        mTabLayout = (SlidingTabLayout) View.inflate(getActivity(), R.layout.view_slidingtab, null);
        Resources resourcesInstance = getResources();
        mTabLayout.setSelectedIndicatorColors(resourcesInstance.getColor(R.color.primary_color_dark_complement));
        mTabLayout.setTextColor(resourcesInstance.getColorStateList(R.color.selector_tab_text));

        mTabLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        mTabContainerOriginalHeight = tabContainer.getHeight();

        Util.makeLayoutHeightAnimator(tabContainer, mTabLayout.getMeasuredHeight() + mTabContainerOriginalHeight).start();
        tabContainer.addView(mTabLayout);
    }

    public void removeTabLayout(final ViewGroup tabContainer)
    {
        ValueAnimator animator = Util.makeLayoutHeightAnimator(tabContainer, mTabContainerOriginalHeight);
        animator.addListener(new Animator.AnimatorListener()
        {
            @Override
            public void onAnimationStart(Animator animation)
            {
                tabContainer.removeView(mTabLayout);
            }

            @Override
            public void onAnimationEnd(Animator animation)
            {

            }

            @Override
            public void onAnimationCancel(Animator animation)
            {

            }

            @Override
            public void onAnimationRepeat(Animator animation)
            {

            }
        });

        animator.start();

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
