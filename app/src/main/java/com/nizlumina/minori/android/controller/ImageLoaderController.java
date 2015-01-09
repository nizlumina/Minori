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

package com.nizlumina.minori.android.controller;

import android.content.Context;

import com.nizlumina.minori.android.adapter.GalleryAdapter;
import com.nizlumina.minori.android.presenter.GalleryPresenter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * This controller class bridges the communication between HummingbirdNetworkController and the ImageLoader public library.
 * <p/>
 * Since HummingbirdNetworkController controls cache access to the main AnimeObject datapoint, this class then supplements additional setters to the cache <i>before</i> the cache is saved.
 * <p/>
 * <p>This separation is needed since services do not need any access to ImageLoaderController methods as well as for future extendability.</p>.
 */
public class ImageLoaderController<K extends GalleryPresenter>
{
    HummingbirdNetworkController mHummingbirdNetworkController;
    ImageLoader mImageLoader;

    public ImageLoaderController(HummingbirdNetworkController hummingbirdNetworkController, ImageLoader imageLoader)
    {
        this.mHummingbirdNetworkController = hummingbirdNetworkController;
        this.mImageLoader = imageLoader;
    }


    public void loadAdapter(final Context context, final GalleryAdapter<? extends GalleryPresenter> adapter, List<K> presenters)
    {

        List<K> markers = new ArrayList<>();
        //Cached URI is set here as well
        for (K presenter : presenters)
        {
            if (presenter.getOnlineImageURI() != null)
            {

                String localURI = presenter.getLocalImageURI();
                String onlineURI = presenter.getOnlineImageURI();
                if (localURI == null)
                {
                    //mImageLoader.displ
                }
            }
        }
    }

    private void updateCachedURI()
    {

    }

}
