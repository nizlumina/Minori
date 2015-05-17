package com.nizlumina.minori.internal;

import com.nizlumina.minori.model.WatchData;

import java.util.ArrayList;
import java.util.List;

/**
 * A singleton that holds resource for the main watchlist data. This is required since the scanning service may access the main dataset and update it as necessary while the app is being used.
 */
public class WatchlistSingleton
{
    private static volatile WatchlistSingleton INSTANCE = null;
    private static List<WatchData> mDataList;

    private WatchlistSingleton() {}

    public static WatchlistSingleton getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (WatchlistSingleton.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = new WatchlistSingleton();
                }
            }
        }
        return INSTANCE;
    }

    public List<WatchData> getDataList()
    {
        if (mDataList == null) mDataList = new ArrayList<WatchData>();
        return mDataList;
    }

    public void setDataList(List<WatchData> newWatchDatas)
    {
        mDataList = newWatchDatas;
    }

    public int getAvailableID()
    {
        return mDataList.size(); //this only work since only addition/removal will call getAvailableID().
    }
}
