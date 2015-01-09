package com.nizlumina.minori.android.internal;


import java.util.ArrayList;

/**
 * Simple state singleton
 */
public class MinoriSingleton
{
    private static volatile MinoriSingleton INSTANCE = null;
    private static volatile ArrayList<ActivityListener> activityListeners = new ArrayList<ActivityListener>();

    private MinoriSingleton() {}

    public static MinoriSingleton getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (MinoriSingleton.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = new MinoriSingleton();
                }
            }
        }
        return INSTANCE;
    }

    public synchronized boolean listenersAvailable()
    {
        return activityListeners != null && activityListeners.size() > 0;
    }

    public synchronized void registerListener(ActivityListener listener)
    {
        if (!activityListeners.contains(listener))
            activityListeners.add(listener);
    }

    public synchronized void unregisterListener(ActivityListener listener)
    {
        if (activityListeners.size() > 0)
            activityListeners.remove(listener);
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
