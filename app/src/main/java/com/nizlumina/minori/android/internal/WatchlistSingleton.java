package com.nizlumina.minori.android.internal;

import com.nizlumina.minori.android.data.WatchData;

import java.util.ArrayList;

/**
 * A singleton that holds resource for the main watchlist data. This is required since the scanning service may access the main dataset and update it as necessary while the app is being used.
 */
public class WatchlistSingleton
{
    private static volatile WatchlistSingleton INSTANCE = null;
    private static ArrayList<WatchData> dataList;

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

    public synchronized ArrayList<WatchData> getDataList()
    {
        if (dataList == null) dataList = new ArrayList<WatchData>();
        return dataList;
    }

    public synchronized int getAvailableID()
    {
        return dataList.size(); //this only work since only addition/removal will call getAvailableID().
    }
}
