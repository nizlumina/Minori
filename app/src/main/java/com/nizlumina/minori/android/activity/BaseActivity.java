package com.nizlumina.minori.android.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.nizlumina.minori.R;

public class BaseActivity extends ActionBarActivity
{
    private FrameLayout topContainer;
    private FrameLayout contentContainer;
    private Toolbar toolbarMain;
    private ImageButton fabMain;
    private ImageButton fabMini;
    private int colorAccent;
    private int colorAccentShade;
    private int colorPrimary;
    private int colorPrimaryDark;
    private int colorPrimaryComplement;
    private int colorPrimaryDarkComplement;

    public Toolbar getToolbarMain()
    {
        return toolbarMain;
    }

    public ImageButton getFabMain()
    {
        return fabMain;
    }

    public ImageButton getFabMini()
    {
        return fabMini;
    }

    public int getColorAccent()
    {
        return colorAccent;
    }

    public int getColorAccentShade()
    {
        return colorAccentShade;
    }

    public int getColorPrimary()
    {
        return colorPrimary;
    }

    public int getColorPrimaryDark()
    {
        return colorPrimaryDark;
    }

    public int getColorPrimaryComplement()
    {
        return colorPrimaryComplement;
    }

    public int getColorPrimaryDarkComplement()
    {
        return colorPrimaryDarkComplement;
    }

    public FrameLayout getContentContainer()
    {
        return contentContainer;
    }

    public FrameLayout getTopContainer()
    {
        return topContainer;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        setupColors();
        setupBaseViews();
    }

    private void setupColors()
    {
        colorAccent = getResources().getColor(R.color.accent_color);
        colorAccentShade = getResources().getColor(R.color.accent_color_shade);

        colorPrimary = getResources().getColor(R.color.primary_color);
        colorPrimaryDark = getResources().getColor(R.color.primary_color_dark);

        colorPrimaryComplement = getResources().getColor(R.color.primary_color_complement);
        colorPrimaryDarkComplement = getResources().getColor(R.color.primary_color_dark_complement);
    }

    private void setupBaseViews()
    {

        toolbarMain = (Toolbar) findViewById(R.id.ma_toolbar);
        if (toolbarMain != null)
        {
            toolbarMain.setTitle("M-Mi-Minori");
        }

        fabMain = (ImageButton) findViewById(R.id.ma_fab_main);
//        if (fabMain != null)
//        {
//            //fabMain.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black_24dp));
//            //fabMain.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
//            //Util.tintImageButton(fabMain, Color.GREEN, Color.BLUE);
//        }

        fabMini = (ImageButton) findViewById(R.id.ma_fab_mini);
//        if (fabMini != null)
//        {
//            //fabMini.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh_black_24dp));
//            //Util.tintImageButton(fabMini, Color.WHITE, colorPrimaryComplement);
//        }

        contentContainer = (FrameLayout) findViewById(R.id.ma_contentview);
        topContainer = (FrameLayout) findViewById(R.id.ma_topcontainer);
    }

}
