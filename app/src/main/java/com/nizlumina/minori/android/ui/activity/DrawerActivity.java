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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.ui.fragment.DrawerFragmentListener;
import com.nizlumina.minori.android.ui.fragment.GalleryFragment;
import com.nizlumina.minori.android.ui.fragment.SeasonTabHostFragment;

public class DrawerActivity extends ActionBarActivity
{
    private final int FRAGMENT_CONTAINER = R.id.base_contentfragment;
    private DrawerLayout mDrawerLayout;
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

        @Override
        public void setDrawerToggle(Toolbar toolbar)
        {
            if (toolbar != null)
            {
                ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(DrawerActivity.this, mDrawerLayout, toolbar, R.string.accessibility_drawer_open, R.string.accessibility_drawer_close);
                mDrawerLayout.setDrawerListener(drawerToggle);
                drawerToggle.syncState();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_activity_drawer);
        setupViews();
        if (getSupportFragmentManager().getFragments() == null || getSupportFragmentManager().getFragments().size() == 0)
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(FRAGMENT_CONTAINER, GalleryFragment.newInstance(fragmentListener), GalleryFragment.getFragmentTag())
                    .commit();

    }

    private void setupViews()
    {
        //Set drawers
        mDrawerLayout = (DrawerLayout) findViewById(R.id.base_drawerlayout);
    }

    //Utilize XML bindings
    public void onDrawerInteraction(View view)
    {
        switch (view.getId())
        {
            case R.id.drawer_watchlist:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(FRAGMENT_CONTAINER, GalleryFragment.newInstance(fragmentListener))
                        .commit();
                break;
            case R.id.drawer_season:
                SeasonTabHostFragment seasonTabHostFragment = (SeasonTabHostFragment) getSupportFragmentManager().findFragmentByTag(SeasonTabHostFragment.class.getSimpleName());
                if (seasonTabHostFragment == null)
                {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(SeasonTabHostFragment.class.getSimpleName())
                            .replace(FRAGMENT_CONTAINER, SeasonTabHostFragment.newInstance(fragmentListener), SeasonTabHostFragment.class.getSimpleName())
                            .commit();
                }
                else
                {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(SeasonTabHostFragment.class.getSimpleName())
                            .replace(FRAGMENT_CONTAINER, seasonTabHostFragment, SeasonTabHostFragment.class.getSimpleName())
                            .commit();
                }

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

}
