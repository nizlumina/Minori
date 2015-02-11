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

package com.nizlumina.minori.android.controller;

import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.android.internal.ThreadMaster;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class SearchController
{

    private Future<List<NyaaEntry>> searchFuture;

    public void searchNyaa(final String terms, final OnFinishListener onFinishListener)
    {
        this.stopSearch(); //try to stop any search that is running
        Callable<List<NyaaEntry>> backgroundTask = new Callable<List<NyaaEntry>>()
        {
            @Override
            public List<NyaaEntry> call() throws Exception
            {
                final List<NyaaEntry> output = new ArrayList<>();
                CoreNetworkFactory.getNyaaEntries(terms, output);
                return output;
            }
        };

        searchFuture = ThreadMaster.getInstance().enqueue(backgroundTask, new OnFinishListener<List<NyaaEntry>>()
        {
            @Override
            public void onFinish(List<NyaaEntry> result)
            {
                onFinishListener.onFinish(result);
            }
        });
    }

    public Future<List<NyaaEntry>> getSearchFuture()
    {
        return searchFuture;
    }

    public boolean stopSearch()
    {
        return searchFuture == null || searchFuture.cancel(true);
    }
}
