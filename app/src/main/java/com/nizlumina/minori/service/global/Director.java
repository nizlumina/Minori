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
/*
After hotly debating about it, there's no easy and maintainable way for enqueing task which survives configuration changes and reports to its UI listener afterwards. This is what I concluded after trying to implement multiple variations of Service + Broadcast Receiver, Service + Binders, etc etc. All of them are pointing to the same thing: Such service is simply being utilized as a singleton to run tasks. Hence here's a solution to skip all the overhead and simply do it in pure Java.

That said, Android configuration changes suck monkey balls. To be bug-free, it really forces you to use "dirty" patterns whether you like it or not. So yes, you are forced to use Singleton if you have not-too-short of a task that needs to report to the UI. AsyncTask is just a bandaid. Not to mention utilizing retained fragment is simply making a singleton that is just under a different name.

 On another note, this rant doesn't apply to you if you scope down your tasks carefully and recreates the mutiple IntentService and its variation to handle them. Plus it's good if you code all those to be that specific (with all your lovely complex factories) but for the lazy programmers out there, you want to avoid introducing a boilerplate only you yourself can understand. Or worse, seeing you becoming the boilerplate itself.

 Finally, if you've yet to drown in your factories and ideals, avoid using this. Otherwise, a simple class, portable and reusable across projects, and easy enough to be understood and applied to the future maintainers, become my decision that finally broke the camel's back. */

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//This can actually be moved to a service with a binder but the only good from that is just the benefit from knowing when to shutdown. Cons? Bulky overhead and spaghetti code fest ensues. RxJava can only cut down so much.

public class Director
{
    //Shamelessly stolen from AsyncTask
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<>(128);

    private static final ThreadFactory sThreadFactory = new ThreadFactory()
    {
        private AtomicInteger mCount = new AtomicInteger(0);

        @Override
        public Thread newThread(@NonNull final Runnable r)
        {
            return new Thread(r, getTag());
        }

        private String getTag()
        {
            return Director.class.getSimpleName() + "-thread-" + mCount.incrementAndGet();
        }
    };

    private static final ExecutorService sDiskIOservice = Executors.newFixedThreadPool(1);
    private static final ThreadPoolExecutor sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
    private static final Director ourInstance = new Director();
    private static final ConcurrentHashMap<String, DirectorTask> mHashMap = new ConcurrentHashMap<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean isShuttingDown = false;

//    public <T> DirectorTask<T> takeTask(String id)
//    {
//        //noinspection unchecked
//        DirectorTask<T> directorTask = mHashMap.get(id);
//        mHashMap.remove(id);
//        return directorTask;
//    }

    public static Director getInstance()
    {
        return ourInstance;
    }

    public <T> void enqueue(DirectorTask<T> directorTask)
    {
        mHashMap.put(directorTask.getId(), directorTask);
        if (directorTask.getRequestThread().equals(DirectorTask.RequestThread.DISK))
        {
            sDiskIOservice.submit(directorTask);
        }
        else
        {
            sThreadPoolExecutor.submit(directorTask);
        }
    }

    public <T> DirectorTask<T> getTask(String id)
    {
        //noinspection unchecked
        return mHashMap.get(id);
    }

    public void removeTask(String id)
    {
        mHashMap.remove(id);
    }

    public void cancelShutdown()
    {
        if (isShuttingDown)
        {
            isShuttingDown = false;
        }
    }

    /**
     * Enqueue a timed shutdown. Use this for onDestroy. Any reattachment within the time limit will cancel this shutdown (hence no need to worry about it for configuration changes).
     */
    public void shutdown()
    {
//        isShuttingDown = true;
//
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run()
//            {
//                if(isShuttingDown) //if still true, proceed
//                    shutdownNow();
//            }
//        }, 15000);

    }

    public void shutdownNow()
    {
        mHashMap.clear();
        sThreadPoolExecutor.shutdown();
        sDiskIOservice.shutdown();
    }
}
