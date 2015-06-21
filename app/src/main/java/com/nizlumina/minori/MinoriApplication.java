package com.nizlumina.minori;


import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

/**
 * Simple application instance
 */
public class MinoriApplication extends Application
{
    public static volatile Context mContext;
    private static volatile ArrayList<ActivityListener> activityListeners = new ArrayList<>();

    private static void setContext(Context context)
    {
        mContext = context;
    }

    public synchronized static boolean listenersAvailable()
    {
        return activityListeners != null && activityListeners.size() > 0;
    }

    public synchronized static void registerListener(ActivityListener listener)
    {
        if (!activityListeners.contains(listener))
            activityListeners.add(listener);
    }

    public synchronized static void unregisterListener(ActivityListener listener)
    {
        if (activityListeners.size() > 0)
            activityListeners.remove(listener);
    }

    public static Context getAppContext()
    {
        return mContext;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        MinoriApplication.setContext(mContext);
    }

    public interface ActivityListener {}

    public static class Preference
    {
        public static final String PACKAGE_KEY = "com.nizlumina.minori";
        public static final String PREF_FILE = "shared_prefs";
        public static final String PREF_KEY_PREFFERED_CONN = "preferred_conn";
        public static final String PREF_KEY_DOWNLOAD_LOC = "download_loc";
    }
}
