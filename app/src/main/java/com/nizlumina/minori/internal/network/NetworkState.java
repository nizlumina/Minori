package com.nizlumina.minori.internal.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * State class to check the network validity
 */
public class NetworkState
{
    public static boolean networkOK(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
        {
//            switch (Global.CONN_OPTION)
//            {
//                case WIFI_ONLY:
//                    return activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
//                case MOBILE_DATA_ONLY:
//                    return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
//                case ANY_CONNECTION:
//                    return true;
//            }
            return true;
        }
        return false;
    }

}
