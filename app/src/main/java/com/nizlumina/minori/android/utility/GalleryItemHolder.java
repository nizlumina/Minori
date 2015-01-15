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

package com.nizlumina.minori.android.utility;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.presenter.GalleryPresenter;
import com.squareup.picasso.Picasso;

public class GalleryItemHolder
{
    private static boolean DEBUG_MODE = true;
    public TextView title, group, episode;
    public ImageView imageContainer;

    public GalleryItemHolder(View convertView)
    {
        episode = (TextView) convertView.findViewById(R.id.item_episode);
        group = (TextView) convertView.findViewById(R.id.item_group);
        title = (TextView) convertView.findViewById(R.id.item_title);
        imageContainer = (ImageView) convertView.findViewById(R.id.item_image);
    }

    public void applySource(Context context, GalleryPresenter galleryPresenter)
    {
        if (episode != null && galleryPresenter.getEpisode() != null)
            episode.setText(galleryPresenter.getEpisode());
        if (group != null && galleryPresenter.getGroup() != null)
            group.setText(galleryPresenter.getGroup());
        if (title != null && galleryPresenter.getTitle() != null)
            title.setText(galleryPresenter.getTitle());

        //Try using Universal Image Loader first. Later onwards, will compare with a normal AsyncTask fork

        //TODO: Set options
        if (imageContainer != null && DEBUG_MODE)
        {
            String localURI = galleryPresenter.getLocalImageURI();
            String onlineURI = galleryPresenter.getOnlineImageURI();
            if (onlineURI != null)
            {
                Picasso.with(context).load(onlineURI).fit().into(imageContainer);


//                if (localURI == null)
//                {
//                    ImageViewAware imageViewAware = new ImageViewAware(imageContainer);
//                    ImageLoader imageLoader = ImageLoader.getInstance();
//                    imageLoader.displayImage(onlineURI, imageViewAware);
//
//                    String newLocalURI = imageLoader.getDiskCache().get(onlineURI).getAbsolutePath();
//                    String newLoadingURI = imageLoader.getLoadingUriForView(imageViewAware);
//
//                    Log.v(getClass().getSimpleName(), String.format("\nLocal[%s]\nLoading[%s]\n", newLocalURI, newLoadingURI));
//                }
            }
        }

    }
}
