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

package com.nizlumina.minori.ui.view;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nizlumina.minori.R;

/**
 * A shoe-horned wrapper class for Badges. An all out specialized View will be decided later.
 */
public final class BadgeView
{
    private static int DEFAULT_CATEGORY_TEXT_COLOR = Color.WHITE;
    private final TextView mCategory;
    private final TextView mContent;
    private final ViewGroup mContainer;

    /**
     * Obtain BadgeView instance to wrap the view that was found by findViewById
     */
    public BadgeView(ViewGroup badgeView)
    {
        mContainer = badgeView;
        mContent = (TextView) badgeView.getChildAt(0);
        mCategory = (TextView) badgeView.getChildAt(1);
    }

    public static void setDefaultCategoryTextColor(@ColorRes int color)
    {
        BadgeView.DEFAULT_CATEGORY_TEXT_COLOR = color;
    }

    public static BadgeView quickBuild(View parent, @IdRes int badgeViewContainerID, String category, String contentText)
    {
        final ViewGroup parentView = (ViewGroup) parent.findViewById(badgeViewContainerID);
        BadgeView badgeView = null;
        if (parentView != null)
        {
            badgeView = new BadgeView(parentView);
            badgeView.getContentTextView().setText(contentText);
            badgeView.getCategoryTextView().setText(category);
            badgeView.getCategoryTextView().setTextColor(DEFAULT_CATEGORY_TEXT_COLOR);
        }
        return badgeView;
    }

    public ViewGroup getContainer()
    {
        return mContainer;
    }

    public TextView getContentTextView()
    {
        return mContent;
    }

    public TextView getCategoryTextView()
    {
        return mCategory;
    }

    public void quickTint(@ColorRes int backgroundColorRes)
    {
        int color = getContainer().getResources().getColor(backgroundColorRes);
        Drawable contentBackground = this.getContentTextView().getBackground();
        if (contentBackground != null)
            contentBackground.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        this.getCategoryTextView().getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public void quickColorText(@ColorRes int contentTextColorRes)
    {
        int color = getContainer().getResources().getColor(contentTextColorRes);
        this.getContentTextView().setTextColor(color);
    }

    public void quickColorCategoryText(@ColorRes int categoryTextColorRes)
    {
        int color = getContainer().getResources().getColor(categoryTextColorRes);
        this.getCategoryTextView().setTextColor(color);
    }


    //For future
    private enum Type
    {
        NUMBER(R.layout.badgeview_category_number),
        TEXT(R.layout.badgeview_category_text),
        TEXT_NOBACKGROUND(R.layout.badgeview_category_text_nobg);

        private int id;

        Type(@LayoutRes int resID)
        {
            this.id = resID;
        }
    }
}
