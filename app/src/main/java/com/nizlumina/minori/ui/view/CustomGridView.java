package com.nizlumina.minori.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class CustomGridView extends GridView
{

    public CustomGridView(Context context)
    {
        super(context);
    }

    public CustomGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public CustomGridView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    public int getVerticalScrollOffset()
    {
        return computeVerticalScrollOffset();
    }

    @Override
    protected int computeVerticalScrollOffset()
    {
        return super.computeVerticalScrollOffset();
    }
}