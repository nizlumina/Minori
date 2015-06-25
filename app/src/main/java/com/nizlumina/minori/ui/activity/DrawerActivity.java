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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.ui.fragment.DetailFragment;
import com.nizlumina.minori.ui.fragment.GalleryFragment;
import com.nizlumina.minori.ui.fragment.SeasonFragment;
import com.nizlumina.minori.ui.fragment.SeasonMasterFragment;
import com.nizlumina.syncmaru.model.CompositeData;

public class DrawerActivity extends BaseActivity
{

    private DrawerLayout mDrawerLayout;
    private FrameLayout mFrameLayout;
    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(SeasonFragment.ACTION_REQUEST_DETAIL))
            {
                CompositeData dataFromRequest = SeasonFragment.getDataFromRequest(intent);
                if (dataFromRequest != null)
                    switchFragment(DetailFragment.newInstance(dataFromRequest), DetailFragment.class.getSimpleName(), true, DetailFragment.class.getName());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        setupViews();
        if (savedInstanceState == null)
        {
            //switchFragment(new GalleryFragment(), GalleryFragment.class.getSimpleName(), false, null);
            getSupportFragmentManager().beginTransaction().replace(R.id.ad_fragmentcontainer, new GalleryFragment(), "GalleryFragment").commit();
        }
        setupBroadcastReceivers();
    }

    @Override
    protected void onDestroy()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void setupBroadcastReceivers()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SeasonFragment.ACTION_REQUEST_DETAIL);

        LocalBroadcastManager.getInstance(DrawerActivity.this).registerReceiver(receiver, intentFilter);
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
                    {
                        getSupportFragmentManager().beginTransaction().replace(R.id.ad_fragmentcontainer, new GalleryFragment(), "GalleryFragment").commit();

//                        String tag = GalleryFragment.class.getSimpleName();
//                        GalleryFragment backStackFragment = (GalleryFragment) getSupportFragmentManager().findFragmentByTag(tag);
//                        if (backStackFragment == null)
//                        {
//                            switchFragment(new GalleryFragment(), tag, false, tag);
//                        }
//                        else
//                            switchFragment(backStackFragment, tag, false, tag);
                    }
                    break;
                    case R.id.mm_nav_seasonbrowser:
                    {
//                        String tag = SeasonMasterFragment.class.getSimpleName();
//                        switchFragment(SeasonMasterFragment.newInstance(), tag, true, tag);
                        getSupportFragmentManager().beginTransaction().replace(R.id.ad_fragmentcontainer, SeasonMasterFragment.newInstance(), "SeasonMasterFragment").commit();
                    }
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
            if (activity != null && activity instanceof DrawerActivity)
            {
                final DrawerLayout drawerLayout = ((DrawerActivity) activity).getDrawerLayout();
                if (drawerLayout != null)
                {
                    drawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.accessibility_drawer_open, R.string.accessibility_drawer_close);
                    drawerLayout.setDrawerListener(drawerToggle);
                    drawerToggle.syncState();
                }
            }
        }

        public String getFragmentTag()
        {
            return getClass().getSimpleName();
        }
    }
}
