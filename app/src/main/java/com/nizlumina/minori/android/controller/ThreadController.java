package com.nizlumina.minori.android.controller;

import android.os.Handler;

import java.util.concurrent.Callable;

/**
 * Simple Thread controller class.
 * Will be converted into a full handler class if needed
 */
public class ThreadController
{
    public static synchronized void post(final Callable doInBackground, final Callable callOnOriginalThread)
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


    /**
     * Something I forgot why I put it here in the first place.
     *
     * @param <K> Generic return type to be used when Callables returns.
     */
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
