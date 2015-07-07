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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.nizlumina.common.torrent.TorrentEngine;
import com.nizlumina.common.torrent.TorrentObject;
import com.nizlumina.minori.R;
import com.nizlumina.minori.service.TorrentService;
import com.nizlumina.minori.ui.common.MarginItemDecoration;
import com.nizlumina.minori.ui.common.RecyclerViewAdapterFactory;
import com.nizlumina.minori.utility.Util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TorrentActivity extends BaseDrawerActivity
{
    private boolean serviceBinded = false;
    private TorrentEngine mTorrentEngine;
    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            serviceBinded = true;
            mTorrentEngine = ((TorrentService.TorrentServiceBinder) service).getTorrentEngine();
            setupViews();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            serviceBinded = false;
            mTorrentEngine = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setupViews(savedInstanceState);
        Intent intent = new Intent(TorrentActivity.this, TorrentService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (serviceBinded)
        {
            unbindService(mServiceConnection);
            serviceBinded = false;
        }
    }

    @Override
    public int getDrawerItemId()
    {
        return R.id.mm_nav_torrent;
    }

    public void setupViews()
    {
        final View view = inflateContent(R.layout.activity_torrent);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.ta_recyclerview);
        final Toolbar mainToolbar = (Toolbar) view.findViewById(R.id.main_toolbar);
        mainToolbar.setTitle("Torrent Engine");
        setDrawerNavigationButton(mainToolbar);
        float density = getResources().getDisplayMetrics().density;
        final MarginItemDecoration decoration = new MarginItemDecoration((int) (8 * density));
        final TorrentAdapter torrentAdapter = new TorrentAdapter(TorrentActivity.this, mTorrentEngine);

        torrentAdapter.setOnItemClickListener(new RecyclerViewAdapterFactory.OnItemClickListener()
        {
            @Override
            public void onListItemClick(int position, View view)
            {
                //start torrent detail activity
            }

            @Override
            public boolean onListItemLongClick(int position, View view)
            {
                //selection mode
                return false;
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(TorrentActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(torrentAdapter);

    }

    private static class TorrentViewholder extends RecyclerView.ViewHolder
    {
        private static final int LAYOUT_RES = R.layout.listitem_torrent;
        private TextView title, details;
        private FloatingActionButton fab;
        private ViewSwitcher switcher;
        private ProgressBar progressBar;
        private CheckBox checkBox;

        public TorrentViewholder(View itemView)
        {
            super(itemView);
            switcher = (ViewSwitcher) itemView.findViewById(R.id.lit_viewswitcher);
            fab = (FloatingActionButton) itemView.findViewById(R.id.lit_fab);
            checkBox = (CheckBox) itemView.findViewById(R.id.lit_checkbox);
            title = (TextView) itemView.findViewById(R.id.lit_title);
            details = (TextView) itemView.findViewById(R.id.lit_details);
            progressBar = (ProgressBar) itemView.findViewById(R.id.lit_progressbar);
        }

        public static int getLayoutRes()
        {
            return LAYOUT_RES;
        }

        public TextView getTitle()
        {
            return title;
        }

        public TextView getDetails()
        {
            return details;
        }

        public FloatingActionButton getFab()
        {
            return fab;
        }

        public ViewSwitcher getSwitcher()
        {
            return switcher;
        }

        public ProgressBar getProgressBar()
        {
            return progressBar;
        }

        public CheckBox getCheckBox()
        {
            return checkBox;
        }
    }

    private static class TorrentAdapter extends RecyclerViewAdapterFactory<TorrentViewholder>
    {
        private final LayoutInflater inflater;
        private final Handler mHandler = new Handler(Looper.getMainLooper());
        private final TorrentEngine mTorrentEngine;
        private List<String> mTorrentIds;

        public TorrentAdapter(Context context, TorrentEngine torrentEngine)
        {
            this.inflater = LayoutInflater.from(context);
            this.mTorrentEngine = torrentEngine;
            this.mTorrentIds = new ArrayList<>(mTorrentEngine.getTorrentIds());
        }

        @Override
        public TorrentViewholder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new TorrentViewholder(inflater.inflate(TorrentViewholder.getLayoutRes(), parent, false));
        }

        @Override
        public void onBindViewHolder(final TorrentViewholder holder, int position)
        {
            final String id = mTorrentIds.get(position);
            final String torrentName = mTorrentEngine.getTorrentName(id);
            if (torrentName != null)
                holder.getTitle().setText(torrentName);

            mTorrentEngine.setListener(id, makeListener(holder));
        }

        @Override
        public int getItemCount()
        {
            return mTorrentIds.size();
        }

        private TorrentObject.TorrentListener makeListener(final TorrentViewholder holder)
        {
            return new TorrentObject.TorrentListener()
            {
                final WeakReference<TextView> mDetailsWeakRef = new WeakReference<>(holder.getDetails());
                final WeakReference<ProgressBar> mProgressBarWeakRef = new WeakReference<>(holder.getProgressBar());
                private long lastT = 0;
                private long uploadedT = 0;
                private long downloadedT = 0;

                @Override
                public void onUpdate(long downloaded, long uploaded, long completedBytes, long size, final int activePeersNumber, final int seeders)
                {

                    if (lastT == 0)
                    {
                        lastT = System.currentTimeMillis();
                        this.downloadedT = downloaded;
                        this.uploadedT = uploaded;
                        return; //skip first update
                    }

                    final int progress = (int) (completedBytes / size);
                    final long deltaT = System.currentTimeMillis() - lastT;
                    final long upSpeed = (downloaded - downloadedT) / deltaT;
                    final long downSpeed = (uploaded - uploadedT) / deltaT;

                    final String uploadSpeed = Util.formatSize(upSpeed, 1);
                    final String downloadSpeed = Util.formatSize(downSpeed, 1);
                    final String completed = Util.formatSize(completedBytes, 1);
                    final String fullsize = Util.formatSize(size, 1);
                    final String detailsString = String.format("D %s/s · U %s/s · %d/%d Connected · %s / %s", uploadSpeed, downloadSpeed, activePeersNumber, seeders, completed, fullsize);
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final ProgressBar progressBar = mProgressBarWeakRef.get();
                            if (progressBar != null)
                            {
                                progressBar.setProgress(progress);
                            }

                            final TextView details = mDetailsWeakRef.get();
                            if (details != null)
                            {

                                details.setText(detailsString);
                            }
                        }
                    });
                }
            };
        }
    }
}
