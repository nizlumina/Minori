package com.nizlumina.minori.android.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.utility.Util;

/**
 * Search activity for both network + Cached seasons
 */
public class SearchActivity extends BaseActivity
{
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
//        getFabMain().setImageDrawable(getResources().getDrawable(R.drawable.abc_ic_search_api_mtrl_alpha));
//        Util.tintImageButton(getFabMain(), Color.WHITE, getColorAccent());
//        getFabMain().setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//
//            }
//        });


    }
}
