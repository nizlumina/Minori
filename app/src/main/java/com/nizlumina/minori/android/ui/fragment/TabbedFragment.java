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
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.common.SlidingTabLayout;
import com.nizlumina.minori.android.ui.adapter.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabbedFragment extends Fragment
{
    private static final int FRAGMENT_TAB_LAYOUT = R.layout.fragment_tabbed;
    private static final int TABS_CONTAINER = R.id.fragment_tabs_layout;
    private static final int CONTENT_VIEWPAGER = R.id.fragment_tabs_content_viewpager;
    private List<Fragment> mFragmentList = new ArrayList<Fragment>();
    private SlidingTabLayout mTabLayout;
    private ViewPager mContentViewPager;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private boolean initialized = false;

    public static TabbedFragment newInstance(Fragment... tabFragment)
    {
        TabbedFragment fragment = new TabbedFragment();
        if (tabFragment != null && tabFragment.length > 0)
        {
            List<Fragment> fragmentsList = Arrays.asList(tabFragment);
            fragment.getFragmentList().addAll(fragmentsList);
        }
        return fragment;
    }

    protected List<Fragment> getFragmentList()
    {
        return mFragmentList;
    }

    protected void clearFragments()
    {
        this.mFragmentList.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(FRAGMENT_TAB_LAYOUT, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    private void initViews(View view)
    {
        if (!initialized || mTabLayout == null || mContentViewPager == null || mFragmentPagerAdapter == null)
        {
            mTabLayout = (SlidingTabLayout) view.findViewById(TABS_CONTAINER);
            mContentViewPager = (ViewPager) view.findViewById(CONTENT_VIEWPAGER);
            mFragmentPagerAdapter = new FragmentPagerAdapter(getFragmentManager(), mFragmentList);

            mContentViewPager.setAdapter(mFragmentPagerAdapter);
            mTabLayout.setViewPager(mContentViewPager);

            initialized = true;
        }
    }
}
