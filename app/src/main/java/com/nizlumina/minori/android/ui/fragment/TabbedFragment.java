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
import android.widget.FrameLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.ui.common.SlidingTabLayout;

public abstract class TabbedFragment extends Fragment
{
    private static final int FRAGMENT_TAB_LAYOUT = R.layout.fragment_tabbed;
    private static final int TABS_LAYOUT = R.id.fragment_tabs_layout;
    private static final int CONTENT_VIEWPAGER = R.id.fragment_tabs_content_viewpager;
    private static final int LOADING_PLACEHOLDER = R.id.fragment_tabs_loading_placeholder;
    private SlidingTabLayout mTabLayout;
    private ViewPager mContentViewPager;
    private FrameLayout mLoadingPlaceHolder;

    public SlidingTabLayout getTabLayout()
    {
        return mTabLayout;
    }

    private void setTabLayout(SlidingTabLayout mTabLayout)
    {
        this.mTabLayout = mTabLayout;
    }

    public ViewPager getContentViewPager()
    {
        return mContentViewPager;
    }

    public FrameLayout getLoadingPlaceholder()
    {
        return mLoadingPlaceHolder;
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
        if (mTabLayout == null)
        {

            mTabLayout = (SlidingTabLayout) view.findViewById(TABS_LAYOUT);
            if (mTabLayout != null)
            {
                mTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.primary_color_dark));
            }
        }
        if (mContentViewPager == null)
            mContentViewPager = (ViewPager) view.findViewById(CONTENT_VIEWPAGER);
        if (mLoadingPlaceHolder == null)
            mLoadingPlaceHolder = (FrameLayout) view.findViewById(LOADING_PLACEHOLDER);
    }
}
