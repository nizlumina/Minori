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

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.nizlumina.minori.R;


/**
 * Base fragment class with a hideable header toolbar.
 * Subclass must make sure that super is called in {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
 */
public class ToolbarFragment extends Fragment
{
    private FrameLayout mContentViewContainer, mToolbarSiblingViewContainer;
    private Toolbar mToolbar;
    private LinearLayout mToolbarContainer;
    private boolean mToolbarVisible = true, mContentNeedPadding = false;
    private View mToolbarChildView, mContentView, mToolbarSiblingView;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.base_fragment_toolbar, container, false);
        mToolbar = (Toolbar) view.findViewById(R.id.fragment_toolbar);
        mContentViewContainer = (FrameLayout) view.findViewById(R.id.fragment_content);
        mToolbarContainer = (LinearLayout) view.findViewById(R.id.fragment_toolbar_container);
        mToolbarSiblingViewContainer = (FrameLayout) mToolbarContainer.findViewById(R.id.fragment_toolbar_content);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (mContentNeedPadding)
        {
            View view = getView();
            if (view != null)
            {
                view.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        addPaddingForScrollableParent();
                    }
                });
            }
        }
    }

    public View getToolbarChildView()
    {
        return mToolbarChildView;
    }

    public FrameLayout getToolbarSiblingViewContainer()
    {
        return mToolbarSiblingViewContainer;
    }

    public boolean isToolbarVisible()
    {
        return mToolbarVisible;
    }

    /**
     * Call this in {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} if the layout in {@link #setContentView(android.view.LayoutInflater, int)} is scrollable so padding can be added internally
     */
    public void addContentHeaderPadding()
    {
        mContentNeedPadding = true;
    }

    public View setContentView(LayoutInflater inflater, @LayoutRes int layoutResID)
    {
        mContentView = inflater.inflate(layoutResID, mContentViewContainer, false);
        mContentViewContainer.addView(mContentView);
        return mContentView;
    }

    public View setToolbarSiblingView(LayoutInflater inflater, @LayoutRes int layoutResID)
    {
        mToolbarSiblingView = inflater.inflate(layoutResID, mToolbarSiblingViewContainer, false);
        mToolbarSiblingViewContainer.addView(mToolbarSiblingView);
        return mToolbarSiblingView;
    }

    public View setToolbarChild(LayoutInflater inflater, @LayoutRes int layoutResID)
    {
        mToolbarChildView = inflater.inflate(layoutResID, getToolbar(), false);
        getToolbar().addView(mToolbarChildView);
        return mToolbarChildView;
    }

    public View getContentView()
    {
        return mContentView;
    }

    public LinearLayout getToolbarContainer()
    {
        return mToolbarContainer;
    }

    public View getToolbarSiblingView()
    {
        return mToolbarSiblingView;
    }

    public Toolbar getToolbar()
    {
        return mToolbar;
    }

    public void removeToolbarChild()
    {
        getToolbar().removeView(mToolbarChildView);
    }

    public void hideToolbar()
    {
        mToolbarVisible = false;
        slideToolbar(-mToolbarContainer.getHeight()).start();
    }

    public void showToolbar()
    {
        mToolbarVisible = true;
        slideToolbar(0).start();
    }


    private void addPaddingForScrollableParent()
    {
        final ViewGroup viewGroup = (ViewGroup) mContentView;
        int top = viewGroup.getPaddingTop() + mToolbarContainer.getMeasuredHeight();
        viewGroup.setPadding(viewGroup.getPaddingLeft(), top, viewGroup.getPaddingRight(), viewGroup.getPaddingBottom());
        viewGroup.setClipToPadding(false);
    }

    final ValueAnimator slideToolbar(final int translationY)
    {
        final ValueAnimator animator = ValueAnimator.ofFloat(mToolbarContainer.getTranslationY(), translationY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                final float animatedValue = (float) animation.getAnimatedValue();
                mToolbarContainer.setTranslationY(animatedValue);
            }
        });
        return animator;
    }

    protected AbsListView.OnScrollListener makeToolbarOnScrollListener()
    {
        return new AbsListView.OnScrollListener()
        {
            private int mLastFirstVisibleItem;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState)
            {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
            {
                if (firstVisibleItem > mLastFirstVisibleItem)
                {
                    //scroll down
                    if (mToolbarVisible)
                    {
                        hideToolbar();
                    }
                }
                if (firstVisibleItem < mLastFirstVisibleItem)
                {
                    //scroll up
                    if (!mToolbarVisible)
                    {
                        showToolbar();
                    }
                }

                mLastFirstVisibleItem = firstVisibleItem;
            }
        };
    }
}
