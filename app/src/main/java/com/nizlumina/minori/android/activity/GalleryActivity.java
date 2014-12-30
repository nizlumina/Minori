package com.nizlumina.minori.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.utility.Util;

/**
 * Main start activity
 */
public class GalleryActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupFabs();
        setupGridView();
    }

    private void setupFabs()
    {
        getFabMain().setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
        Util.tintImageButton(getFabMain(), getColorPrimaryDarkComplement(), getColorAccent());

        getFabMain().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getBaseContext(), SearchActivity.class));
            }
        });

        getFabMini().setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh_black_24dp));
        Util.tintImageButton(getFabMini(), getColorPrimaryDarkComplement(), getColorAccent());
    }

    private void setupGridView()
    {
        GridView gridView = (GridView) LayoutInflater.from(this).inflate(R.layout.fragment_gridview, getContentContainer(), false);
        getContentContainer().addView(gridView);


    }
}
