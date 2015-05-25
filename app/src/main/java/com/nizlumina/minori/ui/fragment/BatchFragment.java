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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.ui.adapter.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class BatchFragment extends Fragment
{
    private static final String ARRAYLIST_TITLES = "batchfragment_titles";
    private List<BatchItem> batchItemList = new ArrayList<>();

    public static BatchFragment newInstance(ArrayList<String> titles)
    {
        final BatchFragment batchFragment = new BatchFragment();
        final Bundle bundle = new Bundle();
        bundle.putStringArrayList(ARRAYLIST_TITLES, titles);
        batchFragment.setArguments(bundle);
        return batchFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        final ListView listView = (ListView) getView();
        if (listView != null)
        {
            final GenericAdapter<BatchItem> adapter = new GenericAdapter<>(getActivity(), batchItemList, new BatchItemViewHolder());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    private class BatchItem
    {

    }


    private class BatchItemViewHolder implements GenericAdapter.ViewHolder<BatchItem>
    {

        @Override
        public int getLayoutResource()
        {
            return R.layout.list_item_batchmode;
        }

        @Override
        public GenericAdapter.ViewHolder<BatchItem> getNewInstance()
        {
            return new BatchItemViewHolder();
        }

        @Override
        public GenericAdapter.ViewHolder<BatchItem> setupViewSource(View inflatedConvertView)
        {
            return null;
        }

        @Override
        public void applySource(Context context, BatchItem source)
        {

        }
    }
}

