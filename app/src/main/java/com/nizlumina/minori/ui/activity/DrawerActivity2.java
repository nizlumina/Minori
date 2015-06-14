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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.ui.fragment.GalleryFragment;
import com.nizlumina.minori.ui.fragment.SeasonMasterFragment;

public class DrawerActivity2 extends BaseActivity
{

    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer2);
        setupViews();
        switchFragment(GalleryFragment.newInstance(), GalleryFragment.class.getName(), false, null);
    }

    private void setupViews()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.ad_basedrawerlayout);
        mFrameLayout = (FrameLayout) findViewById(R.id.ad_fragmentcontainer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.ad_drawer);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.mm_nav_watchlist:
                        switchFragment(GalleryFragment.newInstance(), GalleryFragment.class.getName(), false, null);
                        break;
                    case R.id.mm_nav_seasonbrowser:
                        switchFragment(SeasonMasterFragment.newInstance(), SeasonMasterFragment.class.getName(), false, null);
                        break;
                }
                mDrawerLayout.closeDrawers();
                return false;
            }
        });


    }

    public DrawerLayout getDrawerLayout()
    {
        return mDrawerLayout;
    }

    private FrameLayout getFragmentContainer()
    {
        return mFrameLayout;
    }

    private void switchFragment(@NonNull Fragment fragment, @Nullable String fragmentTag, boolean addToBackStack, @Nullable String backStackTag)
    {
        @SuppressLint("CommitTransaction")
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction().replace(getFragmentContainer().getId(), fragment, fragmentTag);
        if (addToBackStack)
            fragmentTransaction.addToBackStack(backStackTag).commit();
        else
            fragmentTransaction.commit();
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
            if (activity != null && activity instanceof DrawerActivity2)
            {
                final DrawerLayout drawerLayout = ((DrawerActivity2) activity).getDrawerLayout();
                if (drawerLayout != null)
                {
                    drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.accessibility_drawer_open, R.string.accessibility_drawer_close);
                    drawerLayout.setDrawerListener(drawerToggle);
                    drawerToggle.syncState();
                }
            }
        }
    }
}
