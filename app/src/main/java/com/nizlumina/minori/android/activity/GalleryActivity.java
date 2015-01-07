package com.nizlumina.minori.android.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.adapter.GalleryAdapter;
import com.nizlumina.minori.android.controller.WatchlistController;
import com.nizlumina.minori.android.data.WatchDataPresenter;
import com.nizlumina.minori.android.utility.Util;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Main start activity
 */
public class GalleryActivity extends BaseActivity
{

    //Any libs should be loaded here since this is the main start Activity.
    private void initImageLoaderLibrary()
    {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initImageLoaderLibrary();

        setupFabs();
        setupContent();

        WatchlistController controller = new WatchlistController();
        controller.forceLoadData(this); //will change to async later on
    }

    private void setupFabs()
    {
        getFabMain().setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
        Util.tintImageButton(getFabMain(), Color.WHITE, getColorAccent());

        getFabMini().setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh_black_24dp));
        Util.tintImageButton(getFabMini(), Color.WHITE, getColorAccent());

        getFabMain().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(GalleryActivity.this, SearchActivity.class));
            }
        });
    }

    private void setupContent()
    {
        GridView gridView = (GridView) LayoutInflater.from(this).inflate(R.layout.fragment_gridview, getContentContainer(), false);
        getContentContainer().addView(gridView);

        WatchlistController controller = new WatchlistController();
        GalleryAdapter galleryAdapter = new GalleryAdapter(this, R.layout.list_item_singleview, WatchDataPresenter.listFrom(controller.getWatchDataArray()));
        gridView.setAdapter(galleryAdapter);
    }
}
