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

package com.nizlumina.minori.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;

import com.nizlumina.minori.ui.ToolbarContract;
import com.nizlumina.minori.ui.activity.DrawerActivity;


/**
 * Base fragment class with a hideable header toolbar.
 * Subclass must make sure that super is called in {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
 */
public class DrawerContentFragment extends Fragment
{
    private boolean mContentWithoutPadding = false;
    private ToolbarContract mToolbarContract;

    public ToolbarContract getToolbarContract()
    {
        return mToolbarContract;
    }

    /**
     * Call this before calling super.onViewCreated();
     */
    public void disableExtraTopPadding()
    {
        mContentWithoutPadding = true;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (activity instanceof DrawerActivity)
        {
            mToolbarContract = ((DrawerActivity) activity).getToolbarContract();
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (!mContentWithoutPadding)
        {
            if (view != null)
            {
                view.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        addExtraPaddingToParent();
                    }
                });
            }
        }
    }

    private void addExtraPaddingToParent()
    {
        final ViewGroup viewGroup = (ViewGroup) getView();
        if (viewGroup != null)
        {
            int top = viewGroup.getPaddingTop() + mToolbarContract.getToolbarContainer().getMeasuredHeight();
            viewGroup.setPadding(viewGroup.getPaddingLeft(), top, viewGroup.getPaddingRight(), viewGroup.getPaddingBottom());
            viewGroup.setClipToPadding(false);
        }
    }

    @Override
    public void onStop()
    {
        super.onStop();
        final FragmentActivity activity = getActivity();
        if (activity instanceof DrawerActivity)
        {
            mToolbarContract.resetToolbar();
        }
    }
}
