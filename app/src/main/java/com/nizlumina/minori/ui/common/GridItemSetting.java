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
 * Handy dandy class for determining the best grid item size provided it has a target width and container width. Width, height, and margin properties are in pixels.
 * Get the item width via {@link #getItemWidthPixels()}. Extraneous rightmost and leftmost margin can be get via {@link #getExtraSideMarginPx()}.
 */
public class GridItemSetting
{
    private int mExtraSideMarginPx;
    private int columnCount;
    private int itemTargetWidthPx;
    private int convertedHeightPx;
    private int marginPx;
    private float displayMetricsDensity;
    private int containerWidthPx;
    private float widthToHeightRatio;

    public GridItemSetting(final float displayMetricsDensity, final int parentContainerWidthPx, final int targetItemWidthDp, final float widthToHeightRatio, final int itemMarginDp)
    {
        this.displayMetricsDensity = displayMetricsDensity;
        this.setMargin(itemMarginDp);
        this.columnCount = (int) (parentContainerWidthPx / (targetItemWidthDp * displayMetricsDensity)); //floored. We throw out the few non-absolute pixels for now.
        this.containerWidthPx = parentContainerWidthPx;

        float yLeftovers = 0;

        float grossTotalMarginPixel = this.columnCount * itemMarginDp * displayMetricsDensity; //Also floored with the same reason as above.
        int totalMarginPixel = (int) grossTotalMarginPixel;
        yLeftovers += grossTotalMarginPixel % 1;

        float grossItemTargetWidth = (parentContainerWidthPx - totalMarginPixel) / this.columnCount;
        this.itemTargetWidthPx = (int) grossItemTargetWidth;

        yLeftovers += grossItemTargetWidth % 1;

        if (yLeftovers >= 2)
            mExtraSideMarginPx = (int) (yLeftovers / 2);
        else mExtraSideMarginPx = 0;

        this.widthToHeightRatio = widthToHeightRatio;
        this.applyWidthToHeightRatio(this.widthToHeightRatio);
    }

    public int getExtraSideMarginPx()
    {
        return mExtraSideMarginPx;
    }

    public int getItemHeightPixels()
    {
        return convertedHeightPx;
    }

    public int getItemWidthPixels()
    {
        return itemTargetWidthPx;
    }

    public void applyWidthToHeightRatio(float ratio)
    {
        this.convertedHeightPx = (int) (this.itemTargetWidthPx / ratio);
    }

    public int getMarginPixels()
    {
        return marginPx;
    }

    public void setMargin(int marginDP)
    {
        this.marginPx = (int) (marginDP * displayMetricsDensity);
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
        final int itemGrossWidth = containerWidthPx / columnCount;
        this.itemTargetWidthPx = itemGrossWidth - marginPx;
        this.applyWidthToHeightRatio(widthToHeightRatio);
    }
}
