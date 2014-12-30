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

package com.nizlumina.minori.android.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.data.WatchDataPresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

class GalleryItemHolder
{
    public TextView title, group, episode;
    public ImageView imageContainer;

    public GalleryItemHolder(View convertView)
    {
        episode = (TextView) convertView.findViewById(R.id.item_episode);
        group = (TextView) convertView.findViewById(R.id.item_group);
        title = (TextView) convertView.findViewById(R.id.item_title);
        imageContainer = (ImageView) convertView.findViewById(R.id.item_image);
    }

    public void applySource(WatchDataPresenter watchDataPresenter)
    {
        episode.setText(watchDataPresenter.getEpisode());
        group.setText(watchDataPresenter.getGroup());
        title.setText(watchDataPresenter.getTitle());

        //Try using Universal Image Loader first. Later onwards, will compare with a normal AsyncTask fork

        //TODO: Set options
        ImageLoader.getInstance().displayImage(watchDataPresenter.getCoverImageURI(), imageContainer);

    }
}
