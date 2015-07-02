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

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.service.global.Director;

public abstract class BaseDrawerActivity extends AppCompatActivity
{
    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout;
    private SmoothDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS | Window.FEATURE_ACTIVITY_TRANSITIONS);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        setupViews(savedInstanceState);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Director.getInstance().shutdown();
    }

    public FrameLayout getContentContainer()
    {
        return mFrameLayout;
    }

    private void setupViews(final Bundle savedInstanceState)
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.ad_basedrawerlayout);
        mFrameLayout = (FrameLayout) findViewById(R.id.ad_fragmentcontainer);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.ad_drawer);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                int itemId = menuItem.getItemId();
                if (itemId != getDrawerItemId())
                {
                    final Intent activityIntent = new Intent();
                    switch (itemId)
                    {
                        case R.id.mm_nav_watchlist:
                        {
                            activityIntent.setClass(BaseDrawerActivity.this, GalleryActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        }
                        break;
                        case R.id.mm_nav_seasonbrowser:
                        {
                            activityIntent.setClass(BaseDrawerActivity.this, SeasonBrowserActivity.class);
                        }
                        break;
                    }
                    startActivity(activityIntent);
                }
                mDrawerLayout.closeDrawers();
                return false;
            }
        });
    }

    public abstract int getDrawerItemId();

    public DrawerLayout getDrawerLayout()
    {
        return mDrawerLayout;
    }

    public void setDrawerNavigationButton(@NonNull Toolbar toolbar)
    {
        if (mDrawerLayout != null)
        {
            mDrawerToggle = new SmoothDrawerToggle(BaseDrawerActivity.this, mDrawerLayout, toolbar, R.string.accessibility_drawer_open, R.string.accessibility_drawer_close);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }
    }

    private static class SmoothDrawerToggle extends ActionBarDrawerToggle
    {

        private Runnable mRunnable;

        public SmoothDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes)
        {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        public SmoothDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes)
        {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

//        @Override
//        public void onDrawerStateChanged(int newState)
//        {
//            super.onDrawerStateChanged(newState);
//            if (newState == DrawerLayout.STATE_IDLE)
//            {
//            }
//        }

        public void enqueueRunnableOnIdle(Runnable runnable)
        {
            this.mRunnable = runnable;
        }

        @Override
        public void onDrawerClosed(View drawerView)
        {
            super.onDrawerClosed(drawerView);

            if (mRunnable != null)
            {
                mRunnable.run();
                mRunnable = null;
            }
        }
    }

    /**
     * A Fragment who optionally depends on DrawerActivity for communicating calls.
     */
    public abstract static class DrawerFragment extends Fragment
    {

        private ActionBarDrawerToggle drawerToggle;

        public void setDrawerNavigationButton(@NonNull Toolbar toolbar)
        {
            final FragmentActivity activity = getActivity();
            if (activity != null && activity instanceof BaseDrawerActivity)
            {
                ((BaseDrawerActivity) activity).setDrawerNavigationButton(toolbar);

            }
        }

        public String getFragmentTag()
        {
            return getClass().getSimpleName();
        }
    }
}
