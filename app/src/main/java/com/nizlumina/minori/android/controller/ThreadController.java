package com.nizlumina.minori.android.controller;

import android.os.Handler;

import java.util.concurrent.Callable;

/**
 * Simple Thread controller class.
 * Will be converted into a full handler class if needed
 */
public class ThreadController
{
    public static synchronized void postman(final Callable doInBackground, final Callable callOnOriginalThread)
    {
        final Handler originalThreadHandler = new Handler();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    doInBackground.call();
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
                            callOnOriginalThread.call();
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
}
