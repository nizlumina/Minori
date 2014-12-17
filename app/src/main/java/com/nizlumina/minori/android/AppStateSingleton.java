package com.nizlumina.minori.android;


import java.util.ArrayList;

/**
 * Simple state singleton
 */
public class AppStateSingleton
{
    private static volatile AppStateSingleton INSTANCE = null;
    private static volatile ArrayList<GlobalStateListener> listeners;

    private AppStateSingleton() {}

    public static AppStateSingleton getInstance()
    {
        if (INSTANCE == null)
        {
            synchronized (AppStateSingleton.class)
            {
                if (INSTANCE == null)
                {
                    INSTANCE = new AppStateSingleton();
                }
            }
        }
        return INSTANCE;
    }

    public synchronized boolean listenersAvailable()
    {
        return listeners != null && listeners.size() > 0;
    }

    public synchronized void registerListener(GlobalStateListener listener)
    {
        if (listeners != null)
        {
            listeners.add(listener);
        }
        else
            listeners = new ArrayList<GlobalStateListener>();
    }

    public synchronized void unregisterListener(GlobalStateListener listener)
    {
        if (listeners != null)
        {
            listeners.remove(listener);
        }
    }

    public interface GlobalStateListener {}

    public static class Preference
    {
        public static final String PACKAGE_KEY = "com.nizlumina.minori";
        public static final String PREF_FILE = "shared_prefs";
        public static final String PREF_KEY_PREFFERED_CONN = "preferred_conn";
        public static final String PREF_KEY_DOWNLOAD_LOC = "download_loc";
    }

}
