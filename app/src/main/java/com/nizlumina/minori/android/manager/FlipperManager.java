package com.nizlumina.minori.android.manager;

import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ViewFlipper;

import java.util.ArrayList;

/**
 * Wrapper class for ViewFlipper
 */
public class FlipperManager
{
    private ViewFlipper mainFlipper;
    private ArrayList<View> flipperChilds;

    public FlipperManager(LayoutInflater inflater, ViewFlipper flipper, @LayoutRes int... childs)
    {
        mainFlipper = flipper;
        ArrayList<View> childViews = new ArrayList<View>();
        for (int child : childs)
        {
            View mChild = inflater.inflate(child, flipper, false);
            if (mChild != null)
            {
                mainFlipper.addView(mChild);
                childViews.add(mChild);
            }
        }

        flipperChilds = childViews;
    }

    public ViewFlipper getMainFlipper()
    {
        return mainFlipper;
    }

    public ArrayList<View> getFlipperChilds()
    {
        return flipperChilds;
    }

    public View getFlipperChild(@IdRes int id)
    {
        for (View child : flipperChilds)
        {
            if (child.getId() == id) return child;
        }
        return null;
    }
}
