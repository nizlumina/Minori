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

package com.nizlumina.syncmaru.model;


/**
 * Composite data that wraps each subclasses
 */
public class CompositeData
{
    private int id;
    private MALObject malObject;
    private SmallAnimeObject smallAnimeObject;
    private LiveChartObject liveChartObject;

    public CompositeData(int id, MALObject malObject, SmallAnimeObject smallAnimeObject, LiveChartObject liveChartObject)
    {
        this.id = id;
        this.malObject = malObject;
        this.smallAnimeObject = smallAnimeObject;
        this.liveChartObject = liveChartObject;
    }

    public CompositeData()
    {
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public MALObject getMalObject()
    {
        return malObject;
    }

    public void setMalObject(MALObject malObject)
    {
        this.malObject = malObject;
    }

    public SmallAnimeObject getSmallAnimeObject()
    {
        return smallAnimeObject;
    }

    public void setSmallAnimeObject(SmallAnimeObject smallAnimeObject)
    {
        this.smallAnimeObject = smallAnimeObject;
    }

    public LiveChartObject getLiveChartObject()
    {
        return liveChartObject;
    }

    public void setLiveChartObject(LiveChartObject liveChartObject)
    {
        this.liveChartObject = liveChartObject;
    }
}
