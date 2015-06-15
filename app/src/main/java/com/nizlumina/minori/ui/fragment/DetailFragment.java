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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nizlumina.minori.R;
import com.nizlumina.minori.ui.activity.DrawerActivity;
import com.nizlumina.minori.ui.presenter.CompositeDataPresenter;
import com.nizlumina.minori.ui.view.BadgeView;
import com.nizlumina.syncmaru.model.CompositeData;

public class DetailFragment extends DrawerActivity.DrawerFragment
{
    private static final String SCORE_CATEGORY = "score";
    private static final String STUDIO_CATEGORY = "studio";
    private static final String SOURCE_CATEGORY = "source";
    private static final String EPISODECOUNT_CATEGORY = "episodes";

    private CompositeData mCompositeData;

    public static DetailFragment newInstance(CompositeData compositeData)
    {
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.mCompositeData = compositeData;
        return detailFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        setupViews(view);
    }

    private void setupViews(View inflatedView)
    {
        final CompositeDataPresenter presenter = new CompositeDataPresenter(mCompositeData);
//        Toolbar mainToolbar = getToolbarContract().getToolbar();
//
//        mainToolbar.setTitle(presenter.getTitle());

        final ImageView detailImageView = (ImageView) inflatedView.findViewById(R.id.detail_image);
        Glide.with(DetailFragment.this).load(presenter.getImageURL()).diskCacheStrategy(DiskCacheStrategy.ALL).into(detailImageView);

        final TextView synopsisView = (TextView) inflatedView.findViewById(R.id.detail_synopsis);
        synopsisView.setText(Html.fromHtml(presenter.getSynopsis()));

        final BadgeView studioBadge = BadgeView.quickBuild(inflatedView, R.id.detail_badge_studio, STUDIO_CATEGORY, presenter.getStudio());

        if (studioBadge != null)
        {
            studioBadge.quickTint(R.color.blue_800);
        }
        else
        {
            //mainToolbar.setSubtitle(presenter.getStudio());
        }


        final BadgeView ratingBadge = BadgeView.quickBuild(inflatedView, R.id.detail_badge_rating, SCORE_CATEGORY, presenter.getScore());
        ratingBadge.quickTint(R.color.orange_800);

        final BadgeView sourceBadge = BadgeView.quickBuild(inflatedView, R.id.detail_badge_source, SOURCE_CATEGORY, presenter.getSource());
        sourceBadge.quickTint(R.color.purple_800);

        final BadgeView episodeCountBadge = BadgeView.quickBuild(inflatedView, R.id.detail_badge_episodecount, EPISODECOUNT_CATEGORY, presenter.getEpisodesCount());
        episodeCountBadge.quickTint(R.color.green_800);

//        final View downloadButton = inflatedView.findViewById(R.id.detail_button_download);
//        if (downloadButton != null)
//        {
//            downloadButton.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    if (drawerFragmentListener != null)
//                    {
//                        drawerFragmentListener.invokeFragmentChange(SearchFragment.newInstance(presenter.getTitle()));
//                    }
//                }
//            });
//        }
    }
}
