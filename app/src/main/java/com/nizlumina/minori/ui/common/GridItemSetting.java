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

/**
 * Handy dandy class for determining the best grid item size provided it has a minimum width. Width, height, and margin properties are in pixels.
 */
public class GridItemSetting
{
    private int columnCount;
    private int width;
    private int height;
    private int margin;
    private float displayMetricsDensity;
    private int containerWidthPixel;
    private float widthToHeightRatio;

    public GridItemSetting(float displayMetricsDensity, final int parentContainerWidthPixel, final int minItemWidthDp, final float widthToHeightRatio, final int minItemSingleHorizontalMarginDP)
    {
//        Log.e(getClass().getSimpleName(), String.format(" DPI %f Parent Container %d MinWidth %d", displayMetricsDensity, parentContainerWidthPixel, minItemWidthDp));
        this.displayMetricsDensity = displayMetricsDensity;
        this.columnCount = (int) (parentContainerWidthPixel / (minItemWidthDp * displayMetricsDensity));
        this.containerWidthPixel = parentContainerWidthPixel;
        final int freeHorizontalMargin = this.containerWidthPixel % minItemWidthDp / this.columnCount;

        if (freeHorizontalMargin >= minItemSingleHorizontalMarginDP)
        {
            int freeWidth = freeHorizontalMargin - minItemSingleHorizontalMarginDP;
            this.setWidth(minItemWidthDp + freeWidth);
            this.setMargin(minItemSingleHorizontalMarginDP);
        }
        else
        {
            this.setWidth(minItemWidthDp);
            this.setMargin(minItemSingleHorizontalMarginDP);
        }

        this.widthToHeightRatio = widthToHeightRatio;
        this.applyWidthToHeightRatio(this.widthToHeightRatio);
    }


    public int getHeightPixels()
    {
        return height;
    }

    public int getWidthPixels()
    {
        return width;
    }

    public void setWidth(int widthDP)
    {
        this.width = (int) (widthDP * displayMetricsDensity);
    }

    public void applyWidthToHeightRatio(float ratio)
    {
        this.height = (int) (this.width / ratio);
    }

    public int getMarginPixels()
    {
        return margin;
    }

    public void setMargin(int marginDP)
    {
        this.margin = (int) (marginDP * displayMetricsDensity);
    }

    public int getColumnCount()
    {
        return columnCount;
    }

    /**
     * Only call for setting item size based on column count.
     * Setting this will destroy the class values intended for fitting a minimum item width (hence you'll need to construct another {@link GridItemSetting}).
     * Once set, the only usable values onwards (for the grid item) are marginPixels, widthPixels and heightPixels.
     *
     * @param columnCount Preferred column count.
     */
    public void setColumn(int columnCount)
    {
        this.columnCount = columnCount;
        final int itemGrossWidth = containerWidthPixel / columnCount;
        this.width = itemGrossWidth - margin;
        this.applyWidthToHeightRatio(widthToHeightRatio);
    }
}
