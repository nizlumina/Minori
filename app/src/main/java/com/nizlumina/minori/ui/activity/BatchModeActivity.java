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

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.nizlumina.minori.R;

import java.util.ArrayList;
import java.util.Random;

public class BatchModeActivity extends AppCompatActivity
{

    private int bottomCardTop;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batchmode);
        setupViews(savedInstanceState);
    }

    private void setupViews(Bundle savedInstanceState)
    {
//        final View includeTopCard = findViewById(R.id.abm_batchcard);
//        final View includeBottomCard = findViewById(R.id.abm_searchcard);
//        final Toolbar mainToolbar = (Toolbar) findViewById(R.id.abm_maintoolbar);
//        includeBottomCard.post(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                bottomCardTop = includeBottomCard.getTop();
//            }
//        });

        ListView listView = (ListView) findViewById(R.id.lbm_lv_searchresult);
        if (listView != null)
        {
            ArrayList<String> list = new ArrayList<>(50);
            for (int i = 0; i < 50; i++)
            {
                list.add("Banzai " + new Random().nextInt(500));
            }
            listView.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_batchmode_search, R.id.libs_search_title, list));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    if (view instanceof ViewGroup)
                    {
                        ((ViewGroup) view).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
                    }
//                    view.findViewById(R.id.libs_rl_expanded).setVisibility(View.VISIBLE);
                }
            });
        }
    }


    //doubleSlideUp(includeTopCard, includeBottomCard, -mainToolbar.getHeight()).start();
    final ValueAnimator doubleSlideUp(final View topCard, final View bottomCard, final int translationY)
    {
        final ValueAnimator animator = ValueAnimator.ofFloat(0, translationY);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                final float animatedValue = (float) animation.getAnimatedValue();
                topCard.setTranslationY(animatedValue);
//                bottomCard.setTop((int)(bottomCardTop + animatedValue));
                final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bottomCard.getLayoutParams();
                params.topMargin = (int) animatedValue;
                bottomCard.setLayoutParams(params);
            }
        });

        return animator;
    }
}
