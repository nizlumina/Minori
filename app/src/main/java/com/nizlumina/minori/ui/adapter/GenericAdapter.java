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

package com.nizlumina.minori.ui.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A Generic Adapter that partly implements ArrayAdapter methods (except the filtering) and also uses a concrete implementation of an inner static ViewHolder interface
 *
 * @param <T> The object backed by the Adapter
 */
public class GenericAdapter<T> extends BaseAdapter
{
    private final Object mLock = new Object();
    private final Context mContext;
    private final int mListItemResource;
    private final ViewHolder<T> mViewHolder;
    private boolean mNotifyOnChange;
    private List<T> mObjects;

    /**
     * @param context    Context where this adapter is used
     * @param list       The list that backs the T object. Passing an empty list and then populate the adapter later also works.
     * @param viewHolder Pass a concrete {@link com.nizlumina.minori.ui.adapter.GenericAdapter.ViewHolder} interface implementation. Anonymous interface do not work due to {@linkplain com.nizlumina.minori.ui.adapter.GenericAdapter.ViewHolder#getNewInstance()} required for instantiation.
     */
    public GenericAdapter(Context context, @NonNull List<T> list, ViewHolder<T> viewHolder)
    {
        mContext = context;
        mListItemResource = viewHolder.getLayoutResource();
        mObjects = list;
        mViewHolder = viewHolder;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder<T> viewholder;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(mListItemResource, parent, false);
            viewholder = mViewHolder.getNewInstance();
            viewholder.setupViewSource(convertView);
            convertView.setTag(viewholder);
        }
        else
        {
            //noinspection unchecked
            viewholder = (ViewHolder<T>) convertView.getTag();
        }

        //noinspection unchecked
        viewholder.applySource(mContext, getItem(position));

        return convertView;
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object)
    {
        synchronized (mLock)
        {
            mObjects.add(object);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection)
    {
        synchronized (mLock)
        {
            mObjects.addAll(collection);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T... items)
    {
        synchronized (mLock)
        {
            Collections.addAll(mObjects, items);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }


    // SECTION DAVY JONES - The below follows ArrayAdapter implementation with slight changes (no filter)

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insert(T object, int index)
    {
        synchronized (mLock)
        {
            mObjects.add(index, object);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object)
    {
        synchronized (mLock)
        {
            mObjects.remove(object);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear()
    {
        synchronized (mLock)
        {
            mObjects.clear();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     *                   in this adapter.
     */
    public void sort(Comparator<? super T> comparator)
    {
        synchronized (mLock)
        {
            Collections.sort(mObjects, comparator);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged()
    {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}.  If set to false, caller must
     * manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     * <p/>
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     *                       automatically call {@link
     *                       #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange)
    {
        mNotifyOnChange = notifyOnChange;
    }

    public Context getContext()
    {
        return mContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount()
    {
        return mObjects.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getItem(int position)
    {
        return mObjects.get(position);
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(T item)
    {
        return mObjects.indexOf(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    //END SECTION DAVY JONES

    /**
     * A generic interface to be used for the Generic adapter. Anonymous instantiation do not work due to {@link #getNewInstance()} being compulsory (plus avoiding heavy reflections). As per how to use it, create a local inner class implementing this interface, declare local View fields and set them in {@link #setupViewSource(android.view.View)}. After that, apply the source in {@link #applySource(android.content.Context, Object)} to the previous View fields.
     *
     * @param <T> Object served as the main source
     */
    public interface ViewHolder<T>
    {
        /**
         * Returns the layout resource item to be used here
         *
         * @return The layout resource id
         */
        @LayoutRes
        int getLayoutResource();

        /**
         * Simply return a new instance of the class that implement this interface.<br />e.g.<br />
         * {@code public class CustomerViewHolder implements ViewHolder<Customer>}
         * <br /><br />
         * then writing:<br /><br />
         * {@code ViewHolder<Customer> getNewInstance(){ return new CustomerViewHolder(); } }
         * <br /><br />
         * is enough.
         *
         * @return An instance class of the implementation.
         */
        ViewHolder<T> getNewInstance();

        /**
         * Setup the viewholder with the inflated view. This is where you find/attach any view to the viewholder local fields.
         *
         * @param inflatedConvertView The inflated view
         * @return A ViewHolder to be used in tagging. This must not return null unless it is intended. Just "return this" is mostly enough.
         */
        ViewHolder<T> setupViewSource(final View inflatedConvertView);

        /**
         * Here's where you apply data source to the ViewHolder local fields (which generally are {@link android.view.View} objects)
         *
         * @param context Context if needed. This is the same context being used in the {@link GenericAdapter} constructor
         * @param source  The object source
         */
        void applySource(final Context context, final T source);
    }

}
