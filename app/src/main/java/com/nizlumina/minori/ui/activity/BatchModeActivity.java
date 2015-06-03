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
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.common.nyaa.NyaaFansubGroup;
import com.nizlumina.common.nyaa.Parser.NyaaXMLParser;
import com.nizlumina.minori.R;
import com.nizlumina.minori.internal.factory.CoreNetworkFactory;
import com.nizlumina.minori.ui.adapter.GenericAdapter;
import com.nizlumina.minori.utility.Util;
import com.nizlumina.syncmaru.model.CompositeData;

import java.util.ArrayList;
import java.util.List;

/**
 * The activity that deals with batching a selection of CompositeData and adds it to the main watchlist.
 * Loaders are implemented where each titles is auto searched.
 * We don't use fragments but instead opt for switching views/reapplying data and view state to on the fly. Why?
 * <p/>
 * While its easier with fragments, instead of reinflating the same layout over and over (and implementing a sliding viewpager),
 * a simple fade-in of the data (which is just texts) is much more faster experience and more in line with the "batching" process.
 */
public class BatchModeActivity extends AppCompatActivity
{
    private final SparseArrayCompat<BatchData> mBatchDatas = new SparseArrayCompat<>(20);
    private TextView mBatchCardTitle;
    private int mCurrentId = -1;
    private GenericAdapter<NyaaFansubGroup> mSearchResultListAdapter;
    private final LoaderManager.LoaderCallbacks<BatchData> mBatchDataLoaderCallbacks = new LoaderManager.LoaderCallbacks<BatchData>()
    {
        @Override
        public Loader<BatchData> onCreateLoader(int id, Bundle args)
        {
            return new NyaaSearchResultLoader(BatchModeActivity.this, mBatchDatas.get(id), args);
        }

        @Override
        public void onLoadFinished(Loader<BatchData> loader, final BatchData data)
        {
            //Refresh list result if its the current BatchData
            if (mCurrentId == data.id)
            {
                final Runnable runnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (!mSearchResultListAdapter.isEmpty())
                            mSearchResultListAdapter.clear();

                        mSearchResultListAdapter.addAll(data.searchResults);
                    }
                };

                runOnUiThread(runnable);
            }
        }

        @Override
        public void onLoaderReset(Loader<BatchData> loader)
        {
            //TODO:Loader reset
        }
    };
    private EditText mSearchQueryEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batchmode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            final View view = getWindow().getDecorView().getRootView();
            if (view instanceof ViewGroup)
            {
                ((ViewGroup) view).getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            }
        }

        final Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            final ArrayList<CompositeData> passedCompositeDatas = extras.getParcelableArrayList(CompositeData.PARCELKEY_COMPOSITEDATA);
            if (passedCompositeDatas != null)
            {
                //NOTE: i is used as index!

                for (int i = 0; i < passedCompositeDatas.size(); i++)
                {
                    mBatchDatas.append(i, new BatchData(i, passedCompositeDatas.get(i)));
                }

                attachViews(savedInstanceState);

                //initial setups
                mCurrentId = 0;
                applyViewsData(mBatchDatas.get(0));
                setupLoaders(mBatchDatas);
            }
        }
    }

    private void setupLoaders(SparseArrayCompat<BatchData> batchDatas)
    {
        for (int i = 0; i < batchDatas.size(); i++)
        {
            final BatchData batchData = batchDatas.get(i);

            //each batch data use its own loader ID so it can be reused
            getSupportLoaderManager().initLoader(batchData.getId(), null, mBatchDataLoaderCallbacks);
        }
    }

    private void searchNyaa()
    {
        final String terms = mSearchQueryEditText.getText().toString();
        if (!terms.isEmpty())
        {
            final Bundle args = new Bundle(20);
            args.putString(NyaaSearchResultLoader.KEY_SEARCH_ARG, terms);
            getSupportLoaderManager().restartLoader(mCurrentId, args, mBatchDataLoaderCallbacks);
        }
        else
        {
            Toast.makeText(this, R.string.toast_noqueryinput, Toast.LENGTH_SHORT).show();
        }
    }

    private void attachViews(Bundle savedInstanceState)
    {
        final View batchCard = findViewById(R.id.abm_cv_titlecard);
        if (batchCard != null)
        {
            mBatchCardTitle = (TextView) batchCard.findViewById(R.id.abm_titlecard_tv_title);
            batchCard.findViewById(R.id.abm_titlecard_btn_setdata).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    //TODO: Reset MAL data
                }
            });
        }


        final View searchCard = findViewById(R.id.abm_cv_searchcard);
        if (searchCard != null)
        {
            final ListView mListViewSearchResult = (ListView) searchCard.findViewById(R.id.lbcs_lv_searchresult);
            if (mListViewSearchResult != null)
            {
                mSearchResultListAdapter = new GenericAdapter<>(BatchModeActivity.this, new ArrayList<NyaaFansubGroup>(100), new NyaaFansubGroupViewHolder());
                mListViewSearchResult.setAdapter(mSearchResultListAdapter);
            }

            mSearchQueryEditText = (EditText) searchCard.findViewById(R.id.lbcs_et_searchquery);
            if (mSearchQueryEditText != null)
            {
                mSearchQueryEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                mSearchQueryEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
                    {
                        if (actionId == EditorInfo.IME_ACTION_DONE)
                        {
                            searchNyaa();
                        }
                        return false;
                    }
                });

                final ImageButton mSearchImageButton = (ImageButton) searchCard.findViewById(R.id.lbcs_ib_search);
                mSearchImageButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        searchNyaa();
                    }
                });
            }
        }

        final View setupCard = findViewById(R.id.abm_cv_setupcard);
        {
            if (setupCard != null)
            {
//                setupCard.findViewById();
            }
        }
    }

    //TODO: HERE
    private void applyViewsData(BatchData batchData)
    {
        mBatchCardTitle.setText(batchData.compositeData.getMalObject().getTitle());
    }

    private static final class BatchData
    {
        private int id;
        private CompositeData compositeData;
        private List<NyaaFansubGroup> searchResults;

        private BatchData(int index, CompositeData compositeData)
        {
            BatchData.this.id = index;
            BatchData.this.compositeData = compositeData;
        }

        public int getId()
        {
            return id;
        }

        public CompositeData getCompositeData()
        {
            return compositeData;
        }

        public List<NyaaFansubGroup> getSearchResults()
        {
            return searchResults;
        }

        public void setSearchResults(List<NyaaFansubGroup> searchResults)
        {
            this.searchResults = searchResults;
        }
    }

    private static final class NyaaFansubGroupViewHolder implements GenericAdapter.ViewHolder<NyaaFansubGroup>
    {
        private TextView nGroupTextView, mTitleTextView;

        @Override
        public int getLayoutResource()
        {
            return R.layout.list_item_batchmode_search;
        }

        @Override
        public GenericAdapter.ViewHolder<NyaaFansubGroup> getNewInstance()
        {
            return new NyaaFansubGroupViewHolder();
        }

        @Override
        public GenericAdapter.ViewHolder<NyaaFansubGroup> setupViewSource(View inflatedConvertView)
        {
            nGroupTextView = (TextView) inflatedConvertView.findViewById(R.id.libs_search_group);
            mTitleTextView = (TextView) inflatedConvertView.findViewById(R.id.libs_search_title);
            return this;
        }

        @Override
        public void applySource(Context context, NyaaFansubGroup source)
        {
            final String groupName = source.getGroupName();
            if (groupName != null)
                nGroupTextView.setText(groupName);

            final String seriesTitle = source.getSeriesTitle();
            if (seriesTitle != null)
                mTitleTextView.setText(seriesTitle);
        }
    }

    private static final class NyaaSearchResultLoader extends AsyncTaskLoader<BatchData>
    {
        public static final String KEY_SEARCH_ARG = "search_arg";
        private final BatchData batchData;
        private final String overridedSearchTerms;

        public NyaaSearchResultLoader(Context context, BatchData batchData, Bundle args)
        {
            super(context);
            this.batchData = batchData;
            if (args != null)
            {
                this.overridedSearchTerms = args.getString(NyaaSearchResultLoader.KEY_SEARCH_ARG, null);
            }
            else
            {
                overridedSearchTerms = null;
            }
        }

        @Override
        public BatchData loadInBackground()
        {
            final List<NyaaEntry> result = new ArrayList<>();
            final String searchTerms;

            if (overridedSearchTerms != null)
            {
                searchTerms = overridedSearchTerms;
            }
            else
            {
                searchTerms = Util.getBestTerms(batchData.getCompositeData().getMalObject().getTitle());
            }

            CoreNetworkFactory.getNyaaEntries(searchTerms, result);
            batchData.setSearchResults(NyaaXMLParser.group(result));
            return batchData;
        }

        //TODO: Implement better (more correct) loader for resources release, reuse, etc etc
    }
}
