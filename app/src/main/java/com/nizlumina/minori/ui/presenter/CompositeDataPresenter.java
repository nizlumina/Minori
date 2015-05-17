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

package com.nizlumina.minori.ui.presenter;

import com.nizlumina.syncmaru.model.CompositeData;

import java.text.DecimalFormat;

/**
 * A presenter class that expose formatted values for a wrapped {@link com.nizlumina.syncmaru.model.CompositeData}
 */
public class CompositeDataPresenter
{
    private final CompositeData mCompositeData;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public CompositeDataPresenter(CompositeData compositeData)
    {
        this.mCompositeData = compositeData;
    }

    public String getTitle()
    {
        return getCompositeData().getLiveChartObject().getTitle();
    }

    public String getStudio()
    {
        return getCompositeData().getLiveChartObject().getStudio();
    }

    public String getScore()
    {
        return decimalFormat.format(getCompositeData().getMalObject().getScore());
    }

    public String getSource()
    {
        return getCompositeData().getLiveChartObject().getSource();
    }

    public String getEpisodesCount()
    {
        return String.valueOf(getCompositeData().getMalObject().getEpisodes());
    }

    public String getMediaType()
    {
        return getCompositeData().getLiveChartObject().getType();
    }

    public String getWebsite()
    {
        return getCompositeData().getLiveChartObject().getWebsite();
    }

    public String getBigImageURL() {return getCompositeData().getSmallAnimeObject().getPosterImage();}

    public String getImageURL()
    {
        return getCompositeData().getMalObject().getImage();
    }

    public String getSynopsis()
    {
        return getCompositeData().getMalObject().getSynopsis().replace('[', '<').replace(']', '>');
    }

    /**
     * We do not expose presenter backing ref to the original object to remain true to the class purpose.
     */
    private CompositeData getCompositeData()
    {
        return mCompositeData;
    }
}
