package com.nizlumina.minori.android.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.adapter.GalleryAdapter;
import com.nizlumina.minori.android.controller.HummingbirdNetworkController;
import com.nizlumina.minori.android.presenter.AnimeObjectPresenter;
import com.nizlumina.minori.android.utility.Util;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Search activity for both network + Cached seasons
 */
public class SearchActivity extends BaseActivity
{

    GridView gridView;
    GalleryAdapter<AnimeObjectPresenter> adapter;
    HummingbirdNetworkController hummingbirdNetworkController;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupContent();
    }

    private void setupContent()
    {
        RelativeLayout mainContent = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.fragment_search_content, getContentContainer(), false);

        getContentContainer().addView(mainContent);

        getFabMini().setVisibility(View.GONE);
        getFabMain().setVisibility(View.GONE);

        ImageButton searchFab = (ImageButton) findViewById(R.id.sa_fab_search);
        searchFab.setImageDrawable(getResources().getDrawable(R.drawable.abc_ic_search_api_mtrl_alpha));
        Util.tintImageButton(searchFab, Color.WHITE, getColorAccent());

        List<AnimeObjectPresenter> animeObjectPresenters = new ArrayList<>();
        gridView = (GridView) findViewById(R.id.gridview);
        adapter = new GalleryAdapter<>(this, R.layout.list_item_compact, animeObjectPresenters);

        gridView.setAdapter(adapter);

        hummingbirdNetworkController = new HummingbirdNetworkController();
        hummingbirdNetworkController.populateLinkAsync(new HummingbirdNetworkController.NetworkListener<AnimeObject>()
        {
            @Override
            public void onEachSuccessfulResponses(final AnimeObject animeObject)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        adapter.add(new AnimeObjectPresenter(animeObject));
                        adapter.notifyDataSetChanged();
                    }
                });
            }

            public void onFinish()
            {

            }
        }, false);

        searchFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.v(getClass().getSimpleName(), "Search Button clicked!");

            }
        });
    }
}
