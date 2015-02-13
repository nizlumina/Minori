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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.SeasonDataController;
import com.nizlumina.minori.android.ui.adapter.GenericAdapter;
import com.nizlumina.minori.android.ui.gallery.GalleryItemHolder;
import com.nizlumina.minori.android.ui.gallery.GalleryPresenter;
import com.nizlumina.syncmaru.model.CompositeData;

import java.util.ArrayList;
import java.util.List;

public class SeasonFragment extends Fragment
{
    Toolbar mToolbar;
    private GridView mGridView;
    private SeasonDataController mSeasonDataController;
    private List<CompositeData> mCompositeDatas = new ArrayList<>();

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mSeasonDataController = new SeasonDataController();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //return super.onCreateView(inflater, container, savedInstanceState);
        mGridView = (GridView) inflater.inflate(R.layout.fragment_gridview, container, false);
        //load stuffs
        return mGridView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        if (mGridView != null) setupGridView();
    }

    private void setupGridView()
    {
        GalleryItemHolder<CompositeData> compositeDataHolder = new GalleryItemHolder<>(new GalleryPresenter<CompositeData>()
        {
            @Override
            public String getImageURI(CompositeData source)
            {
                return null;
            }

            @Override
            public String getTitle(CompositeData source)
            {
                return null;
            }

            @Override
            public String getGroup(CompositeData source)
            {
                return null;
            }

            @Override
            public String getEpisode(CompositeData source)
            {
                return null;
            }
        });
        GenericAdapter<CompositeData> compositeDataAdapter = new GenericAdapter<>(getActivity(), mCompositeDatas, compositeDataHolder);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        if (mToolbar != null) mToolbar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }
}
