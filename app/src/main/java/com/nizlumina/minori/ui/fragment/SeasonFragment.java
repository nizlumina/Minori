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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import com.nizlumina.minori.ui.common.GridItemSetting;
import com.nizlumina.minori.ui.common.MarginItemDecoration;
import com.nizlumina.syncmaru.model.CompositeData;
import com.nizlumina.syncmaru.model.LiveChartObject;
import com.nizlumina.syncmaru.model.Season;

import java.lang.ref.SoftReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SeasonFragment extends Fragment
{
    public static final String ARGS_GRIDVIEW_PADDING_TOP = "ARGS_GRIDVIEW_PADDING_TOP";
    private static final int minItemWidthDp = 96;
    private static final float widthToHeightRatio = 0.7f;
    private static final int minItemSingleHorizontalMarginDP = 8;
    private static final int minItemWidthTargetForTablet = 140;

    private SeasonController mSeasonController;
    private FloatingActionButton mBatchFab;
    private RecyclerView mRecyclerView;
    private boolean selectionMode = false;

    public static SeasonFragment newInstance(Season season)
    {
        final SeasonFragment seasonFragment = new SeasonFragment();
        seasonFragment.mSeasonController = new SeasonController(season);
        return seasonFragment;
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

        //Because there's no freaking way to measure view in your normal Android lifecycle unless you opt for deprecated methods.
        View view = getView();
        if (view != null && savedInstanceState == null)
        {
            view.post(new Runnable()
            {
                @Override
                public void run()
                {
                    setupViews();
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    private void setupViews()
    {
        final List<CompositeData> compositeDatas = new ArrayList<>();

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int finalMinItemWidthDp = this.minItemWidthDp;
        if (displayMetrics.widthPixels / displayMetrics.density > 600)
        {
            finalMinItemWidthDp = minItemWidthTargetForTablet;
        }
        final GridItemSetting gridItemSetting = new GridItemSetting(displayMetrics.density, mRecyclerView.getWidth(), finalMinItemWidthDp, widthToHeightRatio, minItemSingleHorizontalMarginDP);


        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), gridItemSetting.getColumnCount()));
        mRecyclerView.addItemDecoration(new MarginItemDecoration(gridItemSetting.getMarginPixels(), gridItemSetting.getColumnCount()));

        final SeasonGridRecyclerAdapter adapter = new SeasonGridRecyclerAdapter(compositeDatas, gridItemSetting, getActivity(), SeasonFragment.this);
        mRecyclerView.setAdapter(adapter);

        mSeasonController.getCompositeDatas(new OnFinishListener<List<CompositeData>>()
        {
            @Override
            public void onFinish(final List<CompositeData> result)
            {

                //Even though the result is complete, we only display series for now (since that's what people will use it for anyway).
                //Sectioning parts of the UI for complete results will be decided later in the future
                for (CompositeData compositeData : result)
                {
                    if (compositeData.getLiveChartObject().getCategory() == LiveChartObject.Category.TV)
                        compositeDatas.add(compositeData);
                }

                Collections.sort(compositeDatas, new Comparator<CompositeData>()
                {
                    @Override
                    public int compare(CompositeData lhs, CompositeData rhs)
                    {
                        //return lhs.getLiveChartObject().getTitle().compareTo(rhs.getLiveChartObject().getTitle());
                        //try by rating
                        return Float.compare(rhs.getMalObject().getScore(), lhs.getMalObject().getScore());
                    }
                });

                adapter.notifyDataSetChanged();

            }
        }, false);
    }

    private void startDetailFragmentForItem(CompositeData detailData)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(CompositeData.PARCELKEY_COMPOSITEDATA, detailData);
        DetailFragment detailFragment = DetailFragment.newInstance(null);
        detailFragment.setArguments(bundle);
    }

    private static class SeasonGridRecyclerAdapter extends RecyclerView.Adapter<SeasonGridItem>
    {
        private final List<CompositeData> compositeDatas;
        private final GridItemSetting setting;
        private final SoftReference<Fragment> fragmentSoftRef;
        private final DecimalFormat decimalFormat = new DecimalFormat("#.##");
        private LayoutInflater inflater;

        public SeasonGridRecyclerAdapter(List<CompositeData> compositeDatas, GridItemSetting setting, Context context, Fragment fragment)
        {
            this.compositeDatas = compositeDatas;
            this.setting = setting;
            this.inflater = LayoutInflater.from(context);
            fragmentSoftRef = new SoftReference<>(fragment);
        }

        @Override
        public SeasonGridItem onCreateViewHolder(ViewGroup parent, int viewType)
        {
            return new SeasonGridItem(inflater.inflate(SeasonGridItem.LAYOUT_RESOURCE, parent, false), setting.getWidthPixels(), setting.getHeightPixels());

        }

        @Override
        public void onBindViewHolder(SeasonGridItem holder, int position)
        {
            final CompositeData data = compositeDatas.get(position);

            holder.getTitle().setText(data.getLiveChartObject().getTitle());
            holder.getSource().setText(data.getLiveChartObject().getSource());
            holder.getScore().setText(decimalFormat.format(data.getMalObject().getScore()));
            Glide.with(fragmentSoftRef.get())
                    .load(data.getMalObject().getImage())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.getImageView());
        }

        @Override
        public int getItemCount()
        {
            return compositeDatas.size();
        }
    }

    private static class SeasonGridItem extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public static final int LAYOUT_RESOURCE = R.layout.listitem_season;
        private final TextView title;
        private final TextView score;
        private final TextView source;
        private final ImageView imageView;

        public SeasonGridItem(View itemView, int posterWidth, int posterHeight)
        {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_title);
            score = (TextView) itemView.findViewById(R.id.item_score);
            source = (TextView) itemView.findViewById(R.id.item_source);
            imageView = (ImageView) itemView.findViewById(R.id.item_image);
            final ViewGroup.LayoutParams containerParams = itemView.getLayoutParams();
            containerParams.width = posterWidth;

            final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
            layoutParams.height = posterHeight;

            //No need to request layout since OnBindViewHolder does that.
        }

        @Override
        public void onClick(View v)
        {

        }

        @Override
        public boolean onLongClick(View v)
        {
            return false;
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
    }
}
