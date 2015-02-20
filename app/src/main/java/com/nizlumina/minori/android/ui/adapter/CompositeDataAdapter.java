/*
 * Where applicable, this Minori project follows the standard MIT license as below:
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.android.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nizlumina.minori.R;
import com.nizlumina.syncmaru.model.CompositeData;

import java.util.List;

public class CompositeDataAdapter extends RecyclerView.Adapter<CompositeDataAdapter.CompositeDataHolder>
{
    private final List<CompositeData> mCompositeDatas;

    public CompositeDataAdapter(final List<CompositeData> compositeDatas)
    {
        mCompositeDatas = compositeDatas;

    }

    @Override
    public CompositeDataHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        return new CompositeDataHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_gallery_compact, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(CompositeDataHolder compositeDataHolder, int i)
    {
        compositeDataHolder.bindCompositeData(mCompositeDatas.get(i));
    }

    @Override
    public int getItemCount()
    {
        return mCompositeDatas.size();
    }

    static class CompositeDataHolder extends RecyclerView.ViewHolder
    {
        private TextView mTitleView;
        private ImageView mImageView;

        public CompositeDataHolder(View itemView)
        {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.item_title);
            mImageView = (ImageView) itemView.findViewById(R.id.item_image);
        }

        public void bindCompositeData(CompositeData compositeData)
        {
            mTitleView.setText(compositeData.getLiveChartObject().getTitle());
        }
    }
}
