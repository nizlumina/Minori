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

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.ui.ToolbarContract;
import com.nizlumina.minori.ui.fragment.DrawerFragmentListener;
import com.nizlumina.minori.ui.fragment.GalleryFragment;
import com.nizlumina.minori.ui.fragment.SeasonMasterFragment;

import java.lang.ref.SoftReference;

public class DrawerActivity extends AppCompatActivity
{
    private final int FRAGMENT_CONTAINER = R.id.base_content_container;
    private final DrawerFragmentListener fragmentListener = new DrawerFragmentListener()
    {
        @Override
        public void invokeFragmentChange(Fragment target)
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(target.getClass().getSimpleName())
                    .replace(FRAGMENT_CONTAINER, target)
                    .commit();
        }
    };
    private DrawerLayout mDrawerLayout;
    private FrameLayout mContentViewContainer, mToolbarSiblingViewContainer;
    private Toolbar mToolbar;
    private LinearLayout mToolbarContainer;
    private boolean mToolbarVisible = true;
    protected AbsListView.OnScrollListener toolbarOnScrollListener = new AbsListView.OnScrollListener()
    {
        private int mLastFirstVisibleItem;
        private SoftReference<ToolbarContract> toolbarContractSoftRef = new SoftReference<>(mToolbarContract);

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
                    final ToolbarContract contract = toolbarContractSoftRef.get();
                    if (contract != null)
                        contract.hideToolbar();
                }
            }

            if (firstVisibleItem < mLastFirstVisibleItem)
            {
                //scroll up
                if (!mToolbarVisible)
                {
                    final ToolbarContract contract = toolbarContractSoftRef.get();
                    if (contract != null)
                        contract.showToolbar();
                }
            }

            mLastFirstVisibleItem = firstVisibleItem;
        }


    };
    private View mToolbarChildView, mContentView, mToolbarSiblingView;
    private final ToolbarContract mToolbarContract = new ToolbarContract()
    {
        @Override
        public View getToolbarChildView()
        {
            return mToolbarChildView;
        }

        @Override
        public ViewGroup getToolbarSiblingViewContainer()
        {
            return mToolbarSiblingViewContainer;
        }

        @Override
        public boolean isToolbarVisible()
        {
            return mToolbarVisible;
        }

        @Override
        public View setToolbarSiblingView(LayoutInflater inflater, @LayoutRes int layoutResID)
        {
            mToolbarSiblingView = inflater.inflate(layoutResID, getToolbarSiblingViewContainer(), false);
            mToolbarSiblingViewContainer.addView(mToolbarSiblingView);
            return mToolbarSiblingView;
        }

        @Override
        public View setToolbarChild(LayoutInflater inflater, @LayoutRes int layoutResID)
        {
            mToolbarChildView = inflater.inflate(layoutResID, getToolbar(), false);
            getToolbar().addView(mToolbarChildView);
            return mToolbarChildView;
        }

        @Override
        public ViewGroup getToolbarContainer()
        {
            return mToolbarContainer;
        }

        @Override
        public View getToolbarSiblingView()
        {
            return mToolbarSiblingView;
        }

        @Override
        public Toolbar getToolbar()
        {
            return mToolbar;
        }

        @Override
        public void removeToolbarChild()
        {
            getToolbar().removeView(mToolbarChildView);
        }

        @Override
        public void removeToolbarSiblingView()
        {
            getToolbarSiblingViewContainer().removeView(mToolbarSiblingView);
        }

        @Override
        public void hideToolbar()
        {
            mToolbarVisible = false;
            slideToolbar(-mToolbarContainer.getHeight()).start();
        }

        @Override
        public void showToolbar()
        {
            mToolbarVisible = true;
            slideToolbar(0).start();
        }

        @Override
        public void resetToolbar()
        {
            if (mToolbarChildView != null) removeToolbarChild();
            if (mToolbarSiblingView != null) removeToolbarSiblingView();
            if (!mToolbarContract.isToolbarVisible()) mToolbarContract.showToolbar();
        }

        @Override
        public AbsListView.OnScrollListener getAutoDisplayToolbarListener()
        {
            return toolbarOnScrollListener;
        }
    };

    public ToolbarContract getToolbarContract()
    {
        return mToolbarContract;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        setupViews();
        if (getSupportFragmentManager().getFragments() == null || getSupportFragmentManager().getFragments().size() == 0)
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(FRAGMENT_CONTAINER, GalleryFragment.newInstance())
                    .commit();

    }

    private void setupViews()
    {
        //Set drawers
        mDrawerLayout = (DrawerLayout) findViewById(R.id.base_drawerlayout);

        final View view = mDrawerLayout; //easy refactoring later on
        mToolbar = (Toolbar) view.findViewById(R.id.base_toolbar);
        mContentViewContainer = (FrameLayout) view.findViewById(R.id.base_content_container);
        mToolbarContainer = (LinearLayout) view.findViewById(R.id.base_toolbar_container);
        mToolbarSiblingViewContainer = (FrameLayout) mToolbarContainer.findViewById(R.id.base_toolbar_sibling_content);

        if (mToolbar != null)
        {
            final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(DrawerActivity.this, mDrawerLayout, mToolbar, R.string.accessibility_drawer_open, R.string.accessibility_drawer_close);
            mDrawerLayout.setDrawerListener(drawerToggle);
            drawerToggle.syncState();
        }

        final LayoutTransition layoutTransition = mToolbarContainer.getLayoutTransition();
        if (layoutTransition != null)
        {
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        }
    }

    //Utilize XML bindings
    public void onDrawerInteraction(View view)
    {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        switch (view.getId())
        {
            case R.id.drawer_watchlist:
                fragmentManager
                        .beginTransaction()
                        .replace(FRAGMENT_CONTAINER, GalleryFragment.newInstance())
                        .commit();
                break;
            case R.id.drawer_season:
                fragmentManager
                        .beginTransaction()
                        .replace(FRAGMENT_CONTAINER, SeasonMasterFragment.newInstance(), SeasonMasterFragment.class.getSimpleName())
                        .commit();
                break;
            case R.id.drawer_search:

                break;
            case R.id.drawer_downloadqueue:
                break;
            case R.id.drawer_settings:
                break;
            case R.id.drawer_about:
                break;
        }

        mDrawerLayout.closeDrawers();
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


}
