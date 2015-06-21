package com.nizlumina.minori;


import android.app.Application;
import android.content.Context;

import java.util.ArrayList;

/**
 * Simple application instance
 */
public class MinoriApplication extends Application
{
    private static volatile ArrayList<ActivityListener> activityListeners = new ArrayList<>();


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
        return Singleton.getContext();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Singleton.getInstance().setContext(MinoriApplication.this);
    }

    public interface ActivityListener {}

    public static class Preference
    {
        public static final String PACKAGE_KEY = "com.nizlumina.minori";
        public static final String PREF_FILE = "shared_prefs";
        public static final String PREF_KEY_PREFFERED_CONN = "preferred_conn";
        public static final String PREF_KEY_DOWNLOAD_LOC = "download_loc";
    }


    //A small private singleton for delegateing context access
    private static class Singleton
    {
        private static volatile Context mContext;
        private static volatile Singleton instance;

        private Singleton() {}

        public static Context getContext()
        {
            return mContext;
        }

        public void setContext(Context context)
        {
            mContext = context;
        }

        private static Singleton getInstance()
        {
            if (instance == null)
            {
                synchronized (Singleton.class)
                {
                    if (instance == null)
                    {
                        instance = new Singleton();
                    }
                }
            }
            return instance;
        }
    }
}
