package com.nizlumina.minori.android.activity;

import android.os.Bundle;
import android.view.View;

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
        setupViews();
    }

    private void setupViews()
    {
        getFabMini().setVisibility(View.GONE);

        getFabMain().setImageDrawable(getResources().getDrawable(R.drawable.abc_ic_search_api_mtrl_alpha));
        Util.tintImageButton(getFabMain(), getColorPrimaryDarkComplement(), getColorAccent());

        getFabMain().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }
}
