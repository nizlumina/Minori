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

package com.nizlumina.minori.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.SeasonController;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.service.global.DirectorTask;
import com.nizlumina.minori.ui.activity.BaseDrawerActivity;
import com.nizlumina.minori.ui.activity.DetailActivity;
import com.nizlumina.minori.ui.common.GridItemSetting;
import com.nizlumina.minori.ui.common.MarginItemDecoration;
import com.nizlumina.minori.utility.SparseBooleanArrayParcelable;
import com.nizlumina.minori.utility.Util;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.LiveChartObject;
import com.nizlumina.syncmaru.model.Season;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SeasonFragment extends BaseDrawerActivity.DrawerFragment
{
    private static final String CLASSNAME = SeasonFragment.class.getSimpleName();
    private static final String ARG_SEASON = CLASSNAME + "$season";
    private static final String COMPOSITE_DATA_OUT = CLASSNAME + "$dataout";

    private static final int minItemWidthDp = 96;
    private static final float widthToHeightRatio = 0.7f;
    private static final int minItemSingleHorizontalMarginDP = 8;
    private static final int minItemWidthDPTargetForTablet = 140; //this makes out for ~5 columns, while 120dp gets around ~6 columns,

    private final ArrayList<CompositeData> mCompositeDatas = new ArrayList<>();
    private FloatingActionButton mBatchFab;
    private RecyclerView mRecyclerView;
    private DisplayMetrics mDisplayMetrics;
    private final DirectorTask.Listener<List<CompositeData>> mListener = new DirectorTask.Listener<List<CompositeData>>()
    {
        @Override
        public void onProgress(int progress)
        {

        }

        @Override
        public void onFinish(List<CompositeData> result)
        {
            if (result != null)
            {
                //Even though the main result is complete, we only display tv series for now (since that's what people will use it for anyway).
                //Sectioning the UI for complete results will be decided later in the future
                mCompositeDatas.clear();
                for (CompositeData compositeData : result)
                {
                    if (compositeData.getLiveChartObject().getCategory() == LiveChartObject.Category.TV)
                        mCompositeDatas.add(compositeData);
                }

                Collections.sort(mCompositeDatas, new Comparator<CompositeData>()
                {
                    @Override
                    public int compare(CompositeData lhs, CompositeData rhs)
                    {
                        return (int) (100 * (rhs.getMalObject().getScore() - lhs.getMalObject().getScore()));
                    }
                });
            }

            //we still provide empty dataset on failure
            setupViews(mCompositeDatas);
        }
    };
    // init via fragment args
    private Season mSeason;
    private final DirectorTask<List<CompositeData>> mSeasonTask = new DirectorTask<List<CompositeData>>(null, DirectorTask.RequestThread.NETWORK)
    {
        @Override
        public void run()
        {
            SeasonController seasonController = new SeasonController(mSeason);
            seasonController.getSeasonData(false, new OnFinishListener<List<CompositeData>>()
            {
                @Override
                public void onFinish(@Nullable List<CompositeData> result)
                {
                    publishResult(result);
                }
            });
        }
    };
    private String mParcelKeyCompositeDatas;
    private boolean alreadyLoaded = false; // Explicit false. Since we don't ever utilize setRetainInstance(), rotation will reset the fragment to the initial state which, in this case, we do want.


    public static SeasonFragment newInstance(Season season)
    {
        final Bundle args = new Bundle();
        args.putParcelable(ARG_SEASON, season);
        final SeasonFragment fragment = new SeasonFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public String getCompositeDatasParcelKey()
    {
        return mParcelKeyCompositeDatas;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();

        if (args == null)
            throw new AssertionError(SeasonFragment.CLASSNAME + " started with no args");
        mSeason = args.getParcelable(ARG_SEASON); //always init this

        if (mSeason == null) throw new AssertionError("Season is null");
        final String requestId = mSeason.getIndexKey();
        mParcelKeyCompositeDatas = CLASSNAME + "$compositedatas$" + mSeason.getIndexKey();
        mSeasonTask.setId(requestId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_season, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.fs_recyclerview);
        mBatchFab = (FloatingActionButton) view.findViewById(R.id.fs_batchfab);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        mSeasonTask.attach(mListener);

        if (savedInstanceState == null && !alreadyLoaded)
        {
            alreadyLoaded = true;
            mSeasonTask.enqueue();
        }
        else if (savedInstanceState != null)
        {
            //handles configuration changes
            ArrayList<CompositeData> savedList = savedInstanceState.getParcelableArrayList(getCompositeDatasParcelKey());

            if (savedList != null)
            {
                if (mCompositeDatas.size() > 0)
                    mCompositeDatas.clear();
                mCompositeDatas.addAll(savedList);

                setupViews(mCompositeDatas);
            }
            else
            {
                //if null, result may not been completed yet. Hence:
                mSeasonTask.republishResultIfCompleted();
            }
        }
        else
        {
            setupViews(mCompositeDatas);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mSeasonTask.detach();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mDisplayMetrics = activity.getResources().getDisplayMetrics();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getCompositeDatasParcelKey(), mCompositeDatas);
    }

    private void setupViews(final List<CompositeData> compositeDatas)
    {
        Util.postOnPreDraw(mRecyclerView, new Runnable()
        {
            @Override
            public void run()
            {
                int gridWidth = mRecyclerView.getWidth();

                int finalMinItemWidthDp = minItemWidthDp;
                if (mDisplayMetrics.widthPixels / mDisplayMetrics.density > 600)
                {
                    finalMinItemWidthDp = minItemWidthDPTargetForTablet;
                }
                final GridItemSetting gridItemSetting = new GridItemSetting(mDisplayMetrics.density, gridWidth, finalMinItemWidthDp, widthToHeightRatio, minItemSingleHorizontalMarginDP);
                final SeasonGridRecyclerAdapter adapter = new SeasonGridRecyclerAdapter(compositeDatas, gridItemSetting, getLayoutInflater(null), SeasonFragment.this);
                mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), gridItemSetting.getColumnCount()));
                mRecyclerView.addItemDecoration(new MarginItemDecoration(gridItemSetting.getMarginPixels(), gridItemSetting.getExtraSideMarginPx(), gridItemSetting.getColumnCount()));
                mRecyclerView.setAdapter(adapter);

                //Most important parts here
                adapter.setOnItemClickListener(new SeasonGridRecyclerAdapter.OnItemClickListener()
                {
                    @Override
                    public void onListItemClick(int position, View view)
                    {
                        if (adapter.isSelectable())
                        {
                            adapter.toggleSelection(position);
                        }
                        else
                            launchDetailActivity(compositeDatas.get(position), view);
                    }

                    @Override
                    public boolean onListItemLongClick(int position, View view)
                    {
                        if (!adapter.isSelectable())
                        {
                            adapter.setSelectable(true);
                            adapter.toggleSelection(position);
                        }
                        return false;
                    }
                });
            }
        });

    }

    private void launchDetailActivity(CompositeData detailData, View sharedView)
    {
        final Intent intent = DetailActivity.getActivityStartIntent(getActivity(), detailData);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), sharedView, "posterImage");
            getActivity().startActivity(intent, options.toBundle());
        }
        else
            startActivity(intent);
    }

    private static class SeasonGridRecyclerAdapter extends RecyclerView.Adapter<SeasonGridItem>
    {
        private static final String SELECTIONS_BUNDLETAG = SeasonGridRecyclerAdapter.class.getName();
        private final List<CompositeData> compositeDatas;
        private final GridItemSetting setting;
        private final WeakReference<Fragment> fragmentWeakReference;
        private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
        private OnItemClickListener onItemClickListener;
        private LayoutInflater inflater;
        private boolean selectable = false;
        private SparseBooleanArrayParcelable selections = new SparseBooleanArrayParcelable();

        public SeasonGridRecyclerAdapter(List<CompositeData> compositeDatas, GridItemSetting setting, LayoutInflater inflater, Fragment fragment)
        {
            this.compositeDatas = compositeDatas;
            this.setting = setting;
            this.inflater = inflater;
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public SeasonGridItem onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new SeasonGridItem(inflater.inflate(SeasonGridItem.LAYOUT_RESOURCE, parent, false), setting.getItemWidthPixels(), setting.getItemHeightPixels(), onItemClickListener);
        }

        @Override
        public void onBindViewHolder(SeasonGridItem holder, int position)
        {
            final CompositeData data = compositeDatas.get(position);

            holder.getTitle().setText(data.getLiveChartObject().getTitle());
            holder.getSource().setText(data.getLiveChartObject().getSource());
            holder.getScore().setText(decimalFormat.format(data.getMalObject().getScore()));

            Glide.with(fragmentWeakReference.get())
                    .load(data.getMalObject().getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.getImageView());


            holder.itemView.setActivated(selections.get(position));
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener)
        {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public int getItemCount()
        {
            return compositeDatas.size();
        }

        public void toggleSelection(int position)
        {
            if (selections.get(position))
                selections.put(position, false);
            else
                selections.put(position, true);

            notifyItemChanged(position);
        }

        public void clearSelections()
        {
            selections.clear();
            notifyItemRangeChanged(0, compositeDatas.size());
        }

        public boolean isSelectable()
        {
            return selectable;
        }

        public void setSelectable(boolean enableSelection)
        {
            this.selectable = enableSelection;
        }

        public void onSaveInstanceState(Bundle outState)
        {
            outState.putParcelable(SELECTIONS_BUNDLETAG, selections);
        }

        public void onRestoreInstanceState(Bundle savedInstanceState)
        {
            if (savedInstanceState != null)
                selections = savedInstanceState.getParcelable(SELECTIONS_BUNDLETAG);
        }

        public SparseBooleanArrayParcelable getSelections()
        {
            return selections;
        }

        public interface OnItemClickListener
        {
            void onListItemClick(int position, View view);

            boolean onListItemLongClick(int position, View view);
        }
    }

    private static class SeasonGridItem extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public static final int LAYOUT_RESOURCE = R.layout.listitem_season;
        private final TextView title;
        private final TextView score;
        private final TextView source;
        private final ImageView imageView;
        private final SeasonGridRecyclerAdapter.OnItemClickListener itemClickListener;

        public SeasonGridItem(View itemView, int posterWidth, int posterHeight, SeasonGridRecyclerAdapter.OnItemClickListener onItemClickListener)
        {
            super(itemView);
            this.itemClickListener = onItemClickListener;

            this.title = (TextView) itemView.findViewById(R.id.item_title);
            this.score = (TextView) itemView.findViewById(R.id.item_score);
            this.source = (TextView) itemView.findViewById(R.id.item_source);
            this.imageView = (ImageView) itemView.findViewById(R.id.item_image);

            final ViewGroup.LayoutParams containerParams = itemView.getLayoutParams();
            containerParams.width = posterWidth;

            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.height = posterHeight;

            //No need to request layout since OnBindViewHolder does that.

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public TextView getTitle()
        {
            return title;
        }

        public TextView getScore()
        {
            return score;
        }

        public TextView getSource()
        {
            return source;
        }

        public ImageView getImageView()
        {
            return imageView;
        }

        @Override
        public void onClick(View v)
        {
            itemClickListener.onListItemClick(getAdapterPosition(), v);
        }

        @Override
        public boolean onLongClick(View v)
        {
            return itemClickListener.onListItemLongClick(getAdapterPosition(), v);
        }
    }
}
