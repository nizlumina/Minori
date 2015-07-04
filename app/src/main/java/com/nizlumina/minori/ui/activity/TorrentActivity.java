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

package com.nizlumina.minori.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.nizlumina.common.torrent.TorrentEngine;
import com.nizlumina.minori.R;
import com.nizlumina.minori.service.TorrentService;

public class TorrentActivity extends BaseDrawerActivity
{

    private TorrentEngine mTorrentEngine;
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mTorrentEngine = ((TorrentService.TorrentServiceBinder) service).getTorrentEngine();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mTorrentEngine = null;
        }
    };

    @Override
    public int getDrawerItemId()
    {
        return R.id.mm_nav_torrent;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Intent intent = new Intent(TorrentActivity.this, TorrentService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setupViews(final Bundle savedInstanceState)
    {
        View view = inflateContent(R.layout.activity_torrent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupViews(savedInstanceState);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unbindService(mServiceConnection);
    }

    private static class TorrentViewholder extends RecyclerView.ViewHolder
    {

        public TorrentViewholder(View itemView)
        {
            super(itemView);
        }
    }
}
