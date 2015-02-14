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

package com.nizlumina.minori.android.ui.gallery;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.ui.adapter.GenericAdapter;

public class GalleryItemHolder<T> implements GenericAdapter.ViewHolder<T>
{
    private ImageView mImageView;
    private TextView mTitleView;
    private TextView mGroupView;
    private TextView mEpisodeView;
    private GalleryPresenter<T> mPresenter;

    public GalleryItemHolder(GalleryPresenter<T> galleryPresenter)
    {
        this.mPresenter = galleryPresenter;
    }

    private GalleryItemHolder() {}

    @Override
    public int getLayoutResource()
    {
        return R.layout.list_item_gallery_compact;
    }

    @Override
    public GenericAdapter.ViewHolder<T> getNewInstance()
    {
        return new GalleryItemHolder<T>(mPresenter);
    }

    @Override
    public GenericAdapter.ViewHolder<T> setupViewSource(final View inflatedConvertView)
    {
        mImageView = (ImageView) inflatedConvertView.findViewById(R.id.item_image);
        mTitleView = (TextView) inflatedConvertView.findViewById(R.id.item_title);
        mGroupView = (TextView) inflatedConvertView.findViewById(R.id.item_group);
        mEpisodeView = (TextView) inflatedConvertView.findViewById(R.id.item_episode);
        return this;
    }

    @Override
    public void applySource(final Context context, final T source)
    {
        if (mImageView != null)
            mPresenter.loadInto(mImageView, source);

        setTextView(mTitleView, mPresenter.getTitle(source));

        setTextView(mGroupView, mPresenter.getGroup(source));

        setTextView(mEpisodeView, mPresenter.getEpisode(source));
    }

    private void setTextView(TextView view, String inputText)
    {
        if (view != null && view.getVisibility() == View.VISIBLE)
        {
            if (inputText != null)
            {
                view.setText(inputText);
            }
        }
    }

    public void setVisibility(int imageVisibility, int titleVisibility, int groupVisibility, int episodeVisibility)
    {
        setVisibility(mImageView, imageVisibility);

        setVisibility(mTitleView, titleVisibility);

        setVisibility(mGroupView, groupVisibility);

        setVisibility(mEpisodeView, episodeVisibility);
    }

    private void setVisibility(View view, int visibility)
    {
        if (view != null) view.setVisibility(visibility);
    }
}