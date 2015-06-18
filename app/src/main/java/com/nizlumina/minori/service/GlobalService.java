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

package com.nizlumina.minori.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global service that enqueus Callable. Think of it like a rocket powered executor w.r.t. Android lifecycles. As for usage:
 * <br/>
 * <li>Add {@link ServiceDaemonReceiver} as receiver in the manifest (preferably enabled & not exported)</li>
 * <li>Use the static method {@link #startRequest(Context, int, ServiceCallable, RequestThread, boolean)} from the UI thread to dispatch request. Do not use the usual Context.startService().</li>
 * <li>Handles the result in a concrete implementation of {@link ServiceBroadcastReceiver}</li>
 * Register this receiver in fragment onAttach() / activity onCreate() and unregister in onDetach / activity onStop() or wherever appropriate. If you use {@link android.support.v4.content.LocalBroadcastManager} to register, make sure to set useLocalBroadcastManager to true in startRequest().
 * <li>Once receiving the {@link ServiceBroadcastReceiver#onFinish(int)} broadcast from the service, you can retrieve the resulting object via the static method {@link #getResult(int)} and cast it to the original T parameter of the ServiceCallable you supplied in startRequest()</li>
 */
public class GlobalService extends Service
{
    private static final String CLASSNAME = GlobalService.class.getSimpleName();
    private static final String IKEY_REQ_ID = CLASSNAME + "$id";
    private static final String ACTION_GLOBALSERVICE = CLASSNAME + "$action_start";

    public GlobalService()
    {
    }

    public static <Result> void startRequest(Context context, int requestId, ServiceCallable<Result> callable, RequestThread requestThread, boolean useLocalBroadcastManager)
    {
        final Context applicationContext = context.getApplicationContext();
        final Intent outIntent = new Intent(applicationContext, ServiceDaemonReceiver.class)
                .setAction(ACTION_GLOBALSERVICE)
                .putExtra(IKEY_REQ_ID, requestId)
                .putExtra(RequestThread.IKEY_REQTHREAD, requestThread.ordinal());

        ServiceDirector.getInstance().addTask(requestId, callable);
        if (useLocalBroadcastManager)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(outIntent);
        else
            applicationContext.sendBroadcast(outIntent);
    }

    public static IntentFilter getIntentFilter()
    {
        return new IntentFilter(ACTION_GLOBALSERVICE);
    }

    /**
     * Check the status of the request
     *
     * @param requestId Request ID set in the original request
     * @return Returns the state of the request as stated under {@link com.nizlumina.minori.service.GlobalService.RequestState}.
     * Returns null if the ID can't be found.
     */
    public static RequestState checkRequest(int requestId)
    {
        return ServiceDirector.getInstance().checkTask(requestId);
    }

    /**
     * On successful return (i.e not null), the task will be atomically removed from the task queue, hence subsequent calls will return null unless a new task is generated.
     *
     * @param requestId Id used for the request.
     * @return The resulting object (Don't forget to cast it to its original type).
     */
    public static Object getResult(int requestId)
    {
        return ServiceDirector.getInstance().getResult(requestId);
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
            onHandleIntent(intent);
        return START_NOT_STICKY;
    }

    //The meat of the service
    private void onHandleIntent(@NonNull Intent intent)
    {

    }


    public enum RequestState
    {
        PROCESSING, FINISHED
    }

    public enum RequestThread
    {
        DISK, NETWORK;
        private static final String IKEY_REQTHREAD = RequestThread.class.getName();
    }

    public interface ServiceCallable<T> extends Callable<T>
    {
        void publishProgress(int requestId, int progress);
    }

    /**
     * A BroadcastReceiver to handles broadcast messages <b>from</b> the {@link GlobalService} and hands off as necessarily.
     */
    public static abstract class ServiceBroadcastReceiver extends BroadcastReceiver
    {
        /**
         * If you override this class onReceive(), make sure to call super.onReceive(Context, Intent).
         *
         * @param context {@inheritDoc}
         * @param intent  {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent)
        {

        }

        abstract void onProgress(int requestId, int progress);

        /**
         * Broadcasted when the service finished.
         * Retrieve the resulting object via the static method {@link #getResult(int)} and cast it to the original T parameter of the ServiceCallable you supplied in startRequest(...)
         */
        abstract void onFinish(int requestId);
    }


    /**
     * A singleton director class that manages the object set, access, and retrieval pipeline via a {@link com.nizlumina.minori.service.GlobalService.ServiceDirector.Task}.
     */
    private static class ServiceDirector
    {
        // A = A task to be done. Not that Task class. A real task!
        // B : the callable
        // So to begin...
        // A{B}
        // => A . . [B to list]
        // => A broadcast . . [B in list]
        // => C received A broadcast
        // => [B in list] taken out and processed by C with ID {A}
        // => C broadcast the result to where A resides

        private static volatile ServiceDirector INSTANCE;
        private final ConcurrentHashMap<Integer, Task> taskMap = new ConcurrentHashMap<>();

        private ServiceDirector() {}

        /**
         * Double checked locking implementation of the singleton.
         *
         * @return The sole instance for this class.
         */
        public static ServiceDirector getInstance()
        {
            if (INSTANCE == null)
            {
                synchronized (ServiceDirector.class)
                {
                    if (INSTANCE == null)
                    {
                        INSTANCE = new ServiceDirector();
                    }
                }
            }
            return INSTANCE;
        }

        public synchronized void addTask(final int requestId, final ServiceCallable<?> callable)
        {
            //noinspection unchecked
            taskMap.put(requestId, new Task(requestId, callable));
        }

        public RequestState checkTask(final int requestId)
        {
            final AtomicReference<Task> task = new AtomicReference<>(taskMap.get(requestId));
            if (task.get() != null)
            {
                return task.get().state;
            }
            return null;
        }

        public Object getResult(int requestId)
        {
            AtomicReference<Task> task = new AtomicReference<>(taskMap.get(requestId));
            if (task.get() != null)
            {
                return task.get().result;
            }
            return null;
        }

        public synchronized void removeTask(final int requestID)
        {
            taskMap.remove(requestID);
        }

        private static final class Task<T>
        {
            private final int id;
            private final ServiceCallable<T> callable;
            private volatile RequestState state;
            private T result;

            public Task(int id, ServiceCallable<T> callable)
            {
                this.id = id;
                this.callable = callable;
                this.state = RequestState.PROCESSING;
            }
        }
    }

    public static final class ServiceDaemonReceiver extends WakefulBroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent != null && intent.getAction().equals(ACTION_GLOBALSERVICE))
            {
                startWakefulService(context, intent.setClass(context, GlobalService.class));
            }
        }
    }
}
