/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Unless specified, permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.service.global;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Global service that enqueus runnable task. Think of it like a rocket powered executor w.r.t. Android lifecycles. It mostly utilize {@link LocalBroadcastManager} for IPC. As for usages:
 * <br/>
 * <li>First, add the service to manifest and use the static method {@link #startRequest(Context, String, Runnable, ServiceTask.RequestThread)} from the UI thread to dispatch request. Do not use the usual Context.startService().</li>
 * <li>If you need the task result/progress publishing, use the second static method {@link #startRequest(Context, ServiceTask)} and call {@link ServiceTask#publishProgress(int)} or {@link ServiceTask#setResult(Object)} in the run block. You can obtain the result (once completed) via {@link GlobalService#getResult(String)} or {@link #takeResult(Context, String)}</li>
 * <li>Handle the result in a concrete implementation of {@link ServiceBroadcastReceiver}</li>
 * <li>Register this receiver with {@link LocalBroadcastManager} in a fragment onAttach() / activity onCreate() and unregister in onDetach / activity onStop() or wherever appropriate. The the IntentFilter for this receiver can be retrieved statically from {@link ServiceBroadcastReceiver#getIntentFilter()}.</li>
 * <br/><br/>
 * Important: The service is only automatically killed if all request are taken out/removed from the internal map. Otherwise, use the normal {@link Context#stopService(Intent)} to stop it.
 * <br/>
 */
public class GlobalService extends Service
{
    private static volatile ConcurrentHashMap<String, ServiceTask> sTaskMap = new ConcurrentHashMap<>();
    private final ExecutorService networkExecutorService = Executors.newCachedThreadPool();
    private final ExecutorService diskExecutorService = Executors.newFixedThreadPool(1);
    private final ExecutorService generalExecutorService = Executors.newCachedThreadPool();

    public GlobalService()
    {
    }

    public static <T> T getResult(String requestId)
    {
        ServiceTask serviceTask = sTaskMap.get(requestId);
        if (serviceTask != null)
        {
            //noinspection unchecked
            return serviceTask.getResult();
        }
        return null;
    }

    /**
     * Get the result associated with the request and also removes it from the internal map. Result is nullable.
     * Result can be set via {@link ServiceTask#setResult(Object)}.
     *
     * @param context   Any available context
     * @param requestId RequestId for the request/task
     * @return The result, or null.
     */
    public static <T> T takeResult(Context context, String requestId)
    {
        ServiceTask serviceTask = sTaskMap.get(requestId);
        if (serviceTask != null)
        {
            removeTask(context, requestId);
            //noinspection unchecked
            return serviceTask.getResult();
        }
        return null;
    }

    /**
     * Check request completion.
     *
     * @return True if request completed. False if it can't be found or still being processed. Mostly used with {@link #isRequestEnqueued(String)}
     */
    public static boolean isRequestCompleted(String requestId)
    {
        final ServiceTask serviceTask = sTaskMap.get(requestId);
        return serviceTask != null && serviceTask.isCompleted();
    }

    /**
     * Check if request already enqueued in the task map.
     *
     * @return True if task already enqueued. False if the task cannot be found.
     */
    public static boolean isRequestEnqueued(String requestId)
    {
        return sTaskMap.containsKey(requestId);
    }

    public static void startRequest(Context context, String requestId, final Runnable runnable, ServiceTask.RequestThread requestThread)
    {
        final ServiceTask task = new ServiceTask(requestId, requestThread)
        {
            @Override
            public void run()
            {
                runnable.run();
            }
        };

        sTaskMap.put(requestId, task);

        final Intent outIntent = new Intent(context, GlobalService.class)
                .putExtra(ServiceBroadcastReceiver.IKEY_REQID, requestId);
        WakefulBroadcastReceiver.startWakefulService(context, outIntent);
    }


    /**
     * Use this to rebroadcast task completion for the request. While the service task automatically emits completion broadcast, in case of receivers being unregistered during the broadcast (hence missing the them altogether), this method can be then used to rebroadcast the same intent.
     */
    public static void rebroadcastCompletion(Context context, String requestId)
    {
        LocalBroadcastManager.getInstance(context).sendBroadcast(buildCompletionIntent(context, requestId));
    }

    /**
     * Start the service (or enqueue additional task if already started) with the {@link ServiceTask} instance.
     * You do not actually need to implement a concrete ServiceTask class since a simple new ServiceTask instance already creates all the necessary requirements for it to be run.
     */
    public static void startRequest(Context context, ServiceTask serviceTask)
    {
        final Intent outIntent = new Intent(context, GlobalService.class)
                .putExtra(ServiceBroadcastReceiver.IKEY_REQID, serviceTask.getId());
        sTaskMap.put(serviceTask.getId(), serviceTask);
        WakefulBroadcastReceiver.startWakefulService(context, outIntent);
    }

    public static void startRequest(Context context, @NonNull ServiceTask... serviceTasks)
    {
        for (ServiceTask serviceTask : serviceTasks)
        {
            startRequest(context, serviceTask);
        }
    }

    /**
     * Remove the task from the internal map. Also automatically stops the service if all task are removed.
     *
     * @param requestId The requestId set with the task.
     */
    public static void removeTask(Context context, String requestId)
    {
        sTaskMap.remove(requestId);
        if (sTaskMap.size() == 0)
        {
            stopService(context);
        }
    }

    /**
     * Clear the internal task map and stop the service.
     */
    public static void stopService(Context context)
    {
        sTaskMap.clear();
        Intent intent = new Intent(context, GlobalService.class);
        context.stopService(intent);
    }

    private static Intent buildProgressUpdateIntent(Context context, String requestId)
    {
        return new Intent(context, ServiceBroadcastReceiver.class)
                .setAction(ServiceBroadcastReceiver.ACTION_PROGRESSUPDATE)
                .putExtra(ServiceBroadcastReceiver.IKEY_REQID, requestId);
    }

    private static Intent buildCompletionIntent(Context context, String requestId)
    {
        return new Intent(context, ServiceBroadcastReceiver.class)
                .setAction(ServiceBroadcastReceiver.ACTION_TASKCOMPLETE)
                .putExtra(ServiceBroadcastReceiver.IKEY_REQID, requestId);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null; //we don't provide binding
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null)
        {
            onHandleIntent(intent);
        }
        return START_NOT_STICKY;
    }

    //The meat of the service
    private void onHandleIntent(@NonNull final Intent intent)
    {
        final String requestId = intent.getStringExtra(ServiceBroadcastReceiver.IKEY_REQID);
        if (requestId != null)
        {
            final ServiceTask serviceTask = sTaskMap.get(requestId);
            if (serviceTask != null)
            {
                final Runnable wrapperRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //we need separate wakelock
                        final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, GlobalService.class.getName() + requestId);
                        wakeLock.acquire();
                        {
                            final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                            final Intent progressIntent = buildProgressUpdateIntent(getApplicationContext(), requestId);

                            serviceTask.setProgressListener(new ServiceTask.onProgressListener()
                            {

                                @Override
                                public void onProgress(int progress)
                                {
                                    progressIntent.putExtra(ServiceBroadcastReceiver.IKEY_PROGRESS_INT, progress);
                                    broadcastManager.sendBroadcast(progressIntent);
                                }
                            });

                            serviceTask.run();
                            serviceTask.setCompleted(true);

                            Intent finalIntent = buildCompletionIntent(getApplicationContext(), requestId);
                            broadcastManager.sendBroadcast(finalIntent);
                        }
                        wakeLock.release();
                    }
                };

                switch (serviceTask.getRequestThread())
                {
                    case DISK:
                        diskExecutorService.submit(wrapperRunnable);
                        break;
                    case NETWORK:
                        networkExecutorService.submit(wrapperRunnable);
                        break;
                    case GENERAL:
                        generalExecutorService.submit(wrapperRunnable);
                        break;
                    default:
                        generalExecutorService.submit(wrapperRunnable);
                        break;
                }
            }
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    @Override
    public void onDestroy()
    {
        sTaskMap.clear();
        generalExecutorService.shutdown();
        diskExecutorService.shutdown();
        networkExecutorService.shutdown();
        super.onDestroy();
    }

    /**
     * A BroadcastReceiver to handles broadcast messages <b>from</b> the {@link GlobalService} and hands off as necessarily.
     */
    public static abstract class ServiceBroadcastReceiver extends WakefulBroadcastReceiver
    {
        private static final String CLASSNAME = ServiceBroadcastReceiver.class.getName();
        private static final String ACTION_TASKCOMPLETE = CLASSNAME + "$task_complete";
        private static final String ACTION_PROGRESSUPDATE = CLASSNAME + "$progress_update";
        private static final String IKEY_PROGRESS_INT = CLASSNAME + "$ikey_progressint";
        private static final String IKEY_REQID = CLASSNAME + "$ikey_requestId";


        public static IntentFilter getIntentFilter()
        {
            IntentFilter intentFilter = new IntentFilter(ACTION_TASKCOMPLETE);
            intentFilter.addAction(ACTION_PROGRESSUPDATE);
            return intentFilter;
        }

        /**
         * If you override this class onReceive(), make sure to call super.onReceive(Context, Intent).
         *
         * @param context {@inheritDoc}
         * @param intent  {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            String requestId = intent.getStringExtra(IKEY_REQID);
            if (action != null && requestId != null)
            {
                if (action.equals(ACTION_TASKCOMPLETE))
                {
                    onFinish(requestId);
                }
                else if (action.equals(ACTION_PROGRESSUPDATE))
                {
                    int progress = intent.getIntExtra(IKEY_PROGRESS_INT, -1);
                    onProgress(requestId, progress);
                }
            }
        }

        /**
         * Called if the task runnable publish progress to the main thread.
         */
        public abstract void onProgress(String requestId, int progress);

        /**
         * Broadcasted when the request finished.
         */
        public abstract void onFinish(String requestId);
    }

}


