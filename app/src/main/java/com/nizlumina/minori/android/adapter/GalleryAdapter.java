package com.nizlumina.minori.android.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nizlumina.minori.android.data.WatchDataPresenter;

import java.util.List;

public class GalleryAdapter extends BaseAdapter
{
    private Context mContext;
    private int mResource;
    private List<WatchDataPresenter> mWatchDatas;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public GalleryAdapter(Context context, int resource, List<WatchDataPresenter> objects)
    {
        mContext = context;
        mResource = resource;
        mWatchDatas = objects;
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount()
    {
        if (mWatchDatas != null) return mWatchDatas.size();
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
    public WatchDataPresenter getItem(int position)
    {
        if (mWatchDatas != null)
            return mWatchDatas.get(position);
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

        galleryItemHolder.applySource(mWatchDatas.get(position));

        return convertView;
    }

    public void add(WatchDataPresenter item)
    {
        this.mWatchDatas.add(item);
    }

    public void add(List<WatchDataPresenter> items)
    {
        this.mWatchDatas.addAll(items);
    }

}
