package com.nizlumina.minori.android.adapter;


import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nizlumina.minori.android.presenter.GalleryPresenter;
import com.nizlumina.minori.android.utility.GalleryItemHolder;

import java.util.List;

public class GalleryAdapter<T extends GalleryPresenter> extends BaseAdapter
{
    private Context mContext;
    private int mResource;
    private List<T> mGalleryItems;

    /**
     * Constructor
     *
     * @param context            The current context.
     * @param listItemResourceID The listItemResourceID ID for a layout file containing a TextView to use when
     *                           instantiating views.
     * @param objects            The objects to represent in the ListView.
     */
    public GalleryAdapter(Context context, @LayoutRes int listItemResourceID, List<T> objects)
    {
        mContext = context;
        mResource = listItemResourceID;
        mGalleryItems = objects;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount()
    {
        if (mGalleryItems != null) return mGalleryItems.size();
        return 0;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public T getItem(int position)
    {
        if (mGalleryItems != null)
            return mGalleryItems.get(position);
        return null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @param convertView
     * @param parent
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        //return super.getView(position, convertView, parent);

        GalleryItemHolder galleryItemHolder;

        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
            galleryItemHolder = new GalleryItemHolder(convertView);
            convertView.setTag(galleryItemHolder);
        }
        else galleryItemHolder = (GalleryItemHolder) convertView.getTag();

        galleryItemHolder.applySource(mContext, this.mGalleryItems.get(position));

        return convertView;
    }

    public void add(T item)
    {
        this.mGalleryItems.add(item);
    }

    public void add(List<T> items)
    {
        this.mGalleryItems.addAll(items);
    }

}
