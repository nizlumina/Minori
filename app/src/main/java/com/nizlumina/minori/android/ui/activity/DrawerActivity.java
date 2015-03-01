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

package com.nizlumina.minori.android.ui.activity;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.ui.fragment.DetailFragment;
import com.nizlumina.minori.android.ui.fragment.GalleryFragment;
import com.nizlumina.minori.android.ui.fragment.SeasonFragment;
import com.nizlumina.minori.android.ui.fragment.SeasonHostFragment;
import com.nizlumina.minori.android.utility.Util;

public class DrawerActivity extends ActionBarActivity implements SeasonFragment.Listener, SeasonHostFragment.Listener, DetailFragment.Listener
{
    private Toolbar mToolbar;
    private ViewGroup mTopContainer;
    private LinearLayout mFabContainer;
    private ImageButton mFabMain;
    private ImageButton mFabMini;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private float mDensity;
    private Point mDisplaySize;
    private float mFabContainerX = -10000;
    private boolean mContainerVisibility;

    public ImageButton getFabMini()
    {
        return mFabMini;
    }

    public ImageButton getFabMain()
    {
        return mFabMain;
    }

    public Toolbar getToolbar()
    {
        return mToolbar;
    }


    public ViewGroup getToolbarContainer()
    {
        return (ViewGroup) mToolbar.getParent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_drawer_base);
        setupViews();
        setupMetrics();
        if (getSupportFragmentManager().getFragments() == null || getSupportFragmentManager().getFragments().size() == 0)
            getSupportFragmentManager().beginTransaction().add(R.id.base_contentfragment, new GalleryFragment(), GalleryFragment.getFragmentTag()).commit();

    }


    private void setupMetrics()
    {
        mDensity = getResources().getDisplayMetrics().density;
    }

    private void setupViews()
    {
        //Set basics
        mToolbar = (Toolbar) findViewById(R.id.base_toolbar);
        mTopContainer = (ViewGroup) findViewById(R.id.base_topcontainer);
        mFabMain = (ImageButton) findViewById(R.id.base_fabmain);
        //setupFab(mFabMain, R.color.accent_color_shade, R.color.accent_color);

        mFabMini = (ImageButton) findViewById(R.id.base_fabmini);
        //setupFab(mFabMini, R.color.accent_color_shade, R.color.accent_color);

        mFabContainer = (LinearLayout) findViewById(R.id.base_fabgroup);

        //Set drawers
        mDrawerLayout = (DrawerLayout) findViewById(R.id.base_drawerlayout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.accessibility_drawer_open, R.string.accessibility_drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void showFab()
    {
        if (mFabContainer != null)
        {
            if (mFabContainerX != -10000)
                ObjectAnimator.ofFloat(mFabContainer, "x", mFabContainerX).start();
        }
    }

    private void hideFab()
    {
        if (mFabContainer != null)
        {
            //lazy init
            if (mDisplaySize == null)
            {
                mDisplaySize = new Point();
                getWindowManager().getDefaultDisplay().getSize(mDisplaySize);
            }

            //another lazy init
            if (mFabContainerX == -10000)
            {
                mFabContainerX = mFabContainer.getX();
            }
            ObjectAnimator.ofFloat(mFabContainer, "x", mDisplaySize.x).start();
        }
    }

    //Utilize XML bindings
    public void onDrawerInteraction(View view)
    {
        switch (view.getId())
        {
            case R.id.drawer_watchlist:
                getSupportFragmentManager().beginTransaction().replace(R.id.base_contentfragment, new GalleryFragment()).commit();
                showFab();
                break;
            case R.id.drawer_season:
                getSupportFragmentManager().beginTransaction().replace(R.id.base_contentfragment, SeasonHostFragment.newInstance(this)).commit();
                hideFab();
                break;
            case R.id.drawer_explore:
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

    /**
     * Set fab with default accent color and shade
     *
     * @param fab             The ImageButton or FAB
     * @param drawableID      The drawable ID used as foreground
     * @param foregroundColor Color resource ID to tint the foreground icon
     * @param backgroundColor Color resource ID to tint the background
     */
    public void setupFab(ImageButton fab, @DrawableRes int drawableID, @ColorRes int foregroundColor, @ColorRes int backgroundColor)
    {
        Resources resources = getResources();
        fab.setImageDrawable(resources.getDrawable(drawableID));
        Util.tintImageButton(fab, resources.getColor(foregroundColor), resources.getColor(backgroundColor));
    }

    /**
     * Set fab with default accent color and shade
     *
     * @param fab        The ImageButton or FAB
     * @param drawableID The drawable ID used as foreground
     */
    public void setupFab(ImageButton fab, @DrawableRes int drawableID)
    {
        setupFab(fab, drawableID, R.color.accent_color_shade, R.color.accent_color);
    }

    public boolean getContainerVisibility()
    {
        return mContainerVisibility;
    }

    /**
     * Hides the ViewGroup containing toolbar
     */
    public void hideTopContainer()
    {
        if (mTopContainer != null)
        {
            ObjectAnimator.ofFloat(mTopContainer, "y", mTopContainer.getHeight()).start();
            mContainerVisibility = false;
        }
    }

    public void showTopContainer()
    {
        if (mTopContainer != null)
        {
            ObjectAnimator.ofFloat(mTopContainer, "y", -mTopContainer.getHeight()).start();
            mContainerVisibility = true;
        }
    }

    @Override
    public void addTabLayout(View tabLayout)
    {
        getToolbarContainer().addView(tabLayout);
    }

    @Override
    public void removeTabLayout(View tabLayout)
    {
        getToolbarContainer().removeView(tabLayout);
    }

    @Override
    public void displayDetailFragment(Bundle args)
    {
        DetailFragment detailFragment = DetailFragment.newInstance(this);
        detailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.base_contentfragment, detailFragment).addToBackStack(null).commit();

    }

    @Override
    public void setToolbarTitle(String title)
    {
        getToolbar().setTitle(title);
    }
}
