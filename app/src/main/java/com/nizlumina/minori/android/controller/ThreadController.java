package com.nizlumina.minori.android.controller;

import android.os.Handler;

/**
 * Simple Thread controller class.
 * Will be converted into a full handler class if needed
 */
public class ThreadController
{
    public static synchronized void post(final java.util.concurrent.Callable doInBackground, final java.util.concurrent.Callable callOnOriginalThread)
    {
        final Handler originalThreadHandler = new Handler();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (doInBackground != null) doInBackground.call();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                originalThreadHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            if (callOnOriginalThread != null) callOnOriginalThread.call();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();
    }

    public static class Carrier<K>
    {
        public synchronized Thread post(final Callable<K> doInBackground, final Callable<K> callOnOriginalThread, final K resultCarrier)
        {
            final Handler originalThreadHandler = new Handler();
            final Thread taskThread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        doInBackground.call(resultCarrier);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    originalThreadHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                callOnOriginalThread.call(resultCarrier);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            taskThread.start();
            return taskThread;
        }

        public interface Callable<K>
        {
            void call(K carrierObject) throws Exception;
        }
    }
}
