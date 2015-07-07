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

package com.nizlumina.minori.ui.common;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Handy dandy class for setting equal margin for equal sized items.
 */
public class MarginItemDecoration extends RecyclerView.ItemDecoration
{
    private final int marginPixel;
    private int gridSpanCount = 1;
    private int adjustedSideMargin;

    /**
     * Use this constructor for vertical linear RecyclerViews
     *
     * @param marginPixel Item margin in pixels
     */
    public MarginItemDecoration(int marginPixel)
    {
        this.marginPixel = marginPixel;
        this.adjustedSideMargin = marginPixel;
    }

    /**
     * Use this constructor for vertical grid based RecyclerViews with set columns.
     *
     * @param marginPixel   Item margin in pixels
     * @param gridSpanCount Column count for the grid
     */
    public MarginItemDecoration(int marginPixel, int gridSpanCount)
    {
        this.marginPixel = marginPixel;
        this.gridSpanCount = gridSpanCount;
        this.adjustedSideMargin = marginPixel;
    }

    /**
     * Use this constructor for vertical grid based RecyclerViews with an extra horizontal margin
     *
     * @param marginPixel     Item margin in pixels
     * @param sideMarginPixel Extra margin at each side. 8px margin in here will result 8px margin from the left edge and 8px margin from the right edge.
     * @param gridSpanCount   Column count for the grid
     */
    public MarginItemDecoration(int marginPixel, int sideMarginPixel, int gridSpanCount)
    {
        this.marginPixel = marginPixel;
        this.gridSpanCount = gridSpanCount;
        this.adjustedSideMargin = marginPixel + sideMarginPixel;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
    {
        super.getItemOffsets(outRect, view, parent, state);

        final int childAdapterPosition = parent.getChildAdapterPosition(view);

        if (gridSpanCount > 1)
        {
            if (childAdapterPosition < gridSpanCount) //get only the top items
                outRect.top = marginPixel;

            if (childAdapterPosition % gridSpanCount == 0) //get only the leftmost item
            {
                outRect.left = adjustedSideMargin;
            }

            if ((childAdapterPosition + 1) % gridSpanCount == 0) //get only the rightmost item
            {
                outRect.right = adjustedSideMargin;
            }
            else
                outRect.right = marginPixel;
        }
        else
        {
            outRect.right = marginPixel;
            outRect.left = marginPixel;

            if (childAdapterPosition == 0)
            {
                outRect.top = marginPixel;
            }
        }

        outRect.bottom = marginPixel;
    }
}
