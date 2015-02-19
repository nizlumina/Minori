/*
 * Where applicable, this Minori project follows the standard MIT license as below:
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.android.ui.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

abstract class SectionPresenter<T> extends RecyclerView.ViewHolder
{
    public SectionPresenter(View itemView)
    {
        super(itemView);
    }

    abstract SectionPresenter<T> makeViewHolder(LayoutInflater inflater, ViewGroup container, T source);

    abstract SectionPresenter<T> makeSectionHeaderViewHolder(LayoutInflater inflater, ViewGroup container, T nextSource);

    abstract void bindViewHolder(SectionPresenter<T> viewHolder, T source);

    abstract void bindSectionHeader(SectionPresenter<T> sectionHeaderHolder, T source);

    abstract int getSectionsCount();

    abstract boolean isNextSection(T previousObject, T currentObject);
}

/**
 * A RecycleView Adapter where a sorted dataset (and its final arrangement based on section) is separated by headers (which can be a custom view).
 * Implement a concrete {@link com.nizlumina.minori.android.ui.recycler.SectionPresenter} and pass it as the parameter in the constructor.
 * <p/>
 * The sections are embedded into the dataset, hence any positional calls will be transformed internally to correct the expected behaviour.
 * The original dataset order is not touched hence positional calls to this dataset (especially from {@link com.nizlumina.minori.android.ui.recycler.SectionPresenter}) works normally.
 *
 * @param <T> The Type backed by this adapter.
 */
public class SectionedAdapter<T> extends RecyclerView.Adapter<SectionPresenter<T>>
{
    private int[] mSections;
    private List<T> mDataSet;
    private SectionPresenter<T> mSectionPresenter;

    public SectionedAdapter(List<T> dataSet, SectionPresenter<T> sectionPresenter)
    {
        mDataSet = new ArrayList<>(dataSet);
        mSectionPresenter = sectionPresenter;
        mSections = new int[sectionPresenter.getSectionsCount()];

        if (sectionPresenter.getSectionsCount() > 1)
            buildSectionIndex(dataSet);
        else mSections[0] = 0;
    }

    private void buildSectionIndex(List<T> dataSet)
    {
        int sectionIndex = 0;
        for (int i = 0; i < dataSet.size(); ++i)
        {
            //Set first section
            if (i == 0)
            {
                mSections[0] = 0;
                sectionIndex++;
                continue;
            }

            //Make sure next object can be retrieved. Hence, last object is determined inclusively on the n-1 iteration.
            if (i + 1 < dataSet.size())
            {
                if (mSectionPresenter.isNextSection(dataSet.get(i), dataSet.get(i + 1)))
                {
                    mSections[sectionIndex] = i + sectionIndex;
                    sectionIndex++;
                }
            }
        }
    }

    @Override
    public SectionPresenter<T> onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        if (isHeader(i))
            return mSectionPresenter.makeSectionHeaderViewHolder(LayoutInflater.from(viewGroup.getContext()), viewGroup, mDataSet.get(i));
        return mSectionPresenter.makeViewHolder(LayoutInflater.from(viewGroup.getContext()), viewGroup, mDataSet.get(i));
    }

    @Override
    public void onBindViewHolder(SectionPresenter<T> sectionPresenter, int i)
    {
        if (isHeader(i))
            mSectionPresenter.bindSectionHeader(sectionPresenter, mDataSet.get(i));
        else
            mSectionPresenter.bindViewHolder(sectionPresenter, mDataSet.get(i));

    }

    @Override
    public int getItemCount()
    {
        return mDataSet.size();
    }


    /**
     * Returns the real position of the original object in the internal (adjusted) list that includes section positions.
     * This is only used if the position requested hits a header position.
     *
     * @param position Position of the item in original list before being set in the adapter.
     */
    private int getAdjustedPosition(int position)
    {
        if (isHeader(position))
        {
            if (mSections.length > 1 && position > 0)
            {
                for (int i = 0; i < mSections.length; ++i)
                {
                    if (i + 1 < mSections.length)
                    {
                        if (mSections[i] < position && position < mSections[i + 1])
                            return position + i + 1;
                    }
                    else
                        return position + i;
                }
            }
            //this returns correct position if the original list position is 0 or there's only 1 section
            return position + 1;
        }
        else return position;

    }

    public boolean isHeader(int position)
    {
        for (int section : mSections)
        {
            if (position == section)
                return true;
        }
        return false;
    }
}
