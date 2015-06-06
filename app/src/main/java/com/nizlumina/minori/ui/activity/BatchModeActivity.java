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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.Collections;
import java.util.Comparator;
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
    private SetupCard mSetupCard;
    private GenericAdapter<NyaaFansubGroup> mSearchResultListAdapter;

    private EditText mSearchQueryEditText;
    private LoaderManager.LoaderCallbacks<BatchData> callback;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batchmode);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            final LinearLayout view = (LinearLayout) findViewById(R.id.abm_root);
            if (view != null)
            {
                view.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            }
        }

        final ArrayList<CompositeData> passedCompositeDatas = getIntent().getParcelableArrayListExtra(CompositeData.PARCELKEY_COMPOSITEDATA);
        if (passedCompositeDatas != null)
        {
            //NOTE: i is used as index!

            for (int i = 0; i < passedCompositeDatas.size(); i++)
            {

                mBatchDatas.append(i, new BatchData(i, passedCompositeDatas.get(i)));

            }

            Log.v(getClass().getSimpleName(), "Loading size: " + mBatchDatas.size());
            attachViews(savedInstanceState);

            //initial setups
            mCurrentId = 0;
            applyViewsData(mBatchDatas.get(0));
            setupLoaders(mBatchDatas);
        }
    }

    private void setupLoaders(SparseArrayCompat<BatchData> batchDatas)
    {
        for (int i = 0; i < batchDatas.size(); i++)
        {
            final BatchData batchData = batchDatas.get(i);
            Log.v(getClass().getSimpleName(), "Loading ID: " + batchData.getId());
            //each batch data use its own loader ID so it can be reused
            callback = new LoaderManager.LoaderCallbacks<BatchData>()
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
                    if (mCurrentId == data.getId())
                    {
                        Log.v(getClass().getSimpleName(), mCurrentId + " equals " + data.getId());
                        final Runnable runnable = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (!mSearchResultListAdapter.isEmpty())
                                    mSearchResultListAdapter.clear();

                                mSearchResultListAdapter.addAll(data.searchResults);
                                mSearchResultListAdapter.notifyDataSetChanged();
                            }
                        };

                        mSearchQueryEditText.post(runnable);
                    }
                }

                @Override
                public void onLoaderReset(Loader<BatchData> loader)
                {
                    //TODO:Loader reset
                }
            };
            getSupportLoaderManager().initLoader(batchData.getId(), null, callback).forceLoad();
        }
    }

    private void searchNyaa()
    {
        final String terms = mSearchQueryEditText.getText().toString();
        if (!terms.isEmpty())
        {
            final Bundle args = new Bundle(20);
            args.putString(NyaaSearchResultLoader.KEY_SEARCH_ARG, terms);
            //TODO:Reapply loader
            getSupportLoaderManager().restartLoader(mCurrentId, args, callback).forceLoad();
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


        final View setupCardView = findViewById(R.id.abm_cv_setupcard);
        {
            if (setupCardView != null)
            {
                mSetupCard = new SetupCard(setupCardView);
                mSetupCard.initViews();
                mSetupCard.setDefaultRes(NyaaEntry.Resolution.R720); //TODO: Apply sharedPrefs for default checked RES
            }
        }

        //declared after setupCardView since clicking list item trigger setup card visibility
        final View searchCard = findViewById(R.id.abm_cv_searchcard);
        if (searchCard != null)
        {
            final ListView searchResultListView = (ListView) searchCard.findViewById(R.id.lbcs_lv_searchresult);
            if (searchResultListView != null)
            {
                mSearchResultListAdapter = new GenericAdapter<>(BatchModeActivity.this, new ArrayList<NyaaFansubGroup>(100), new NyaaFansubGroupViewHolder());
                searchResultListView.setAdapter(mSearchResultListAdapter);
                searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        if (mSetupCard != null)
                        {
                            mSetupCard.setData(mSearchResultListAdapter.getItem(position));
                            mSetupCard.updateViews();
                            if (mSetupCard.getVisibility() != View.VISIBLE)
                            {
                                mSetupCard.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
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
                        if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
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
    }

    //TODO: HERE
    private void applyViewsData(BatchData batchData)
    {
        final String title = batchData.compositeData.getLiveChartObject().getTitle();
        mBatchCardTitle.setText(title);
        mSearchQueryEditText.setText(Util.getBestTerms(title, false));
    }

    private static final class BatchData
    {
        private int id;
        private CompositeData compositeData;
        private List<NyaaFansubGroup> searchResults;
        private NyaaFansubGroup selectedNyaaFansubGroup;

        private BatchData(int index, CompositeData compositeData)
        {
            BatchData.this.id = index;
            BatchData.this.compositeData = compositeData;
        }

        public NyaaFansubGroup getSelectedNyaaFansubGroup()
        {
            return selectedNyaaFansubGroup;
        }

        public void setSelectedNyaaFansubGroup(NyaaFansubGroup selectedNyaaFansubGroup)
        {
            this.selectedNyaaFansubGroup = selectedNyaaFansubGroup;
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

    //Small wrapper class
    private static final class SetupCard
    {
        private final View mSetupCard;
        private NyaaFansubGroup mNyaaFansubGroup;
        private CheckBox res480CheckBox;
        private CheckBox res720CheckBox;
        private CheckBox res1080CheckBox;
        private View mResolutionsContainer;
        private EditText mEpisodeEditText;
        private SparseArrayCompat<CheckBox> mResCheckboxSparseArray;
        private Spinner modeSpinner;
        private boolean mResAvailable = false;
        private NyaaEntry.Resolution mDefaultRes = null;

        public SetupCard(View setupCardView)
        {
            this.mSetupCard = setupCardView;
        }

        public void setDefaultRes(NyaaEntry.Resolution mDefaultRes)
        {
            this.mDefaultRes = mDefaultRes;
        }

        public void initViews()
        {
            mEpisodeEditText = (EditText) mSetupCard.findViewById(R.id.libs_et_episode);

            mSetupCard.findViewById(R.id.libs_btn_firstep).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mEpisodeEditText.setText("1");
                }
            });
            mSetupCard.findViewById(R.id.libs_btn_currep).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final int ep = mNyaaFansubGroup.getLatestEpisode();
                    mEpisodeEditText.setText(String.valueOf(ep));
                }
            });
            mSetupCard.findViewById(R.id.libs_btn_nextep).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final int ep = mNyaaFansubGroup.getLatestEpisode();
                    mEpisodeEditText.setText(String.valueOf(ep + 1));
                }
            });

            //Checkboxes
            mResolutionsContainer = mSetupCard.findViewById(R.id.libs_ll_mid);
            res480CheckBox = (CheckBox) mResolutionsContainer.findViewById(R.id.libs_btn_480);
            res720CheckBox = (CheckBox) mResolutionsContainer.findViewById(R.id.libs_btn_720);
            res1080CheckBox = (CheckBox) mResolutionsContainer.findViewById(R.id.libs_btn_1080);

            mResCheckboxSparseArray = new SparseArrayCompat<>(3);
            mResCheckboxSparseArray.append(NyaaEntry.Resolution.R480.ordinal(), res480CheckBox);
            mResCheckboxSparseArray.append(NyaaEntry.Resolution.R720.ordinal(), res720CheckBox);
            mResCheckboxSparseArray.append(NyaaEntry.Resolution.R1080.ordinal(), res1080CheckBox);

            modeSpinner = (Spinner) mSetupCard.findViewById(R.id.libs_spinner_modes);
        }

        public int getVisibility()
        {
            return mSetupCard.getVisibility();
        }

        public void setVisibility(int visibility)
        {
            mSetupCard.setVisibility(visibility);
        }

        public void setData(NyaaFansubGroup nyaaFansubGroup)
        {
            this.mNyaaFansubGroup = nyaaFansubGroup;
        }


        //by updating views upon list item clicked, we do not need to reset their states fully due to overriding.
        public void updateViews()
        {
            if (mNyaaFansubGroup != null)
            {
                mEpisodeEditText.setText(String.valueOf(mNyaaFansubGroup.getLatestEpisode()));

                //if(mNyaaFansubGroup.getResolutions())
                List<NyaaEntry.Resolution> resolutions = mNyaaFansubGroup.getResolutions();

                mResAvailable = false;
                if (resolutions.size() > 0)
                {
                    for (final NyaaEntry.Resolution resolution : mNyaaFansubGroup.getResolutions())
                    {
                        if (resolution != null)
                        {
                            final CheckBox checkBox = mResCheckboxSparseArray.get(resolution.ordinal());
                            if (checkBox != null)
                            {
                                mResAvailable = true;
                                checkBox.setVisibility(View.VISIBLE);

                                //Auto checked if default res is set
                                if (mDefaultRes != null && mDefaultRes == resolution)
                                {
                                    checkBox.setChecked(true);
                                }
                                else if (checkBox.isChecked())
                                {
                                    checkBox.setChecked(false);
                                }
                            }
                        }
                    }
                }

                if (!mResAvailable || resolutions.size() == 0)
                {
                    mResolutionsContainer.setVisibility(View.GONE);
                }
                else if (mResolutionsContainer
                        .getVisibility() != View.VISIBLE)
                    mResolutionsContainer.setVisibility(View.VISIBLE);

            }
        }

        public NyaaFansubGroup applyFinalData(Context context)
        {
            final NyaaFansubGroup finalNyaaFansubGroup = new NyaaFansubGroup(mNyaaFansubGroup.getGroupName());

            if (mEpisodeEditText.getText() != null && mEpisodeEditText.length() > 0)
            {
                finalNyaaFansubGroup.setLatestEpisode(Integer.parseInt(mEpisodeEditText.getText().toString()));
            }
            else
            {
                Toast.makeText(context, R.string.error_noepsentered, Toast.LENGTH_LONG).show();
                return null;
            }

            if (mResAvailable) //only applicable if any res is available to be selected
            {
                boolean selectAny = false;
                for (NyaaEntry.Resolution resolution : NyaaEntry.Resolution.values())
                {
                    final CheckBox checkBox = mResCheckboxSparseArray.get(resolution.ordinal());
                    if (checkBox != null)
                    {
                        if (checkBox.isChecked())
                        {
                            selectAny = checkBox.isChecked();
                            finalNyaaFansubGroup.getResolutions().add(resolution);
                        }
                    }
                }
                if (!selectAny)
                {
                    Toast.makeText(context, R.string.error_noresselected, Toast.LENGTH_LONG).show();
                    return null;
                }
            }

            finalNyaaFansubGroup.setModes(modeSpinner.getSelectedItemPosition());

            finalNyaaFansubGroup.setSeriesTitle(mNyaaFansubGroup.getSeriesTitle());
            finalNyaaFansubGroup.setId(mNyaaFansubGroup.getId());
            finalNyaaFansubGroup.setTrustCategory(mNyaaFansubGroup.getTrustCategory());

            //TODO:return modes as well
            return finalNyaaFansubGroup;
        }
    }

    private static final class NyaaFansubGroupViewHolder implements GenericAdapter.ViewHolder<NyaaFansubGroup>
    {
        private TextView mGroupTextView, mTitleTextView;

        private int primary = -1, secondary = -1, tertiary = -1;
        private boolean colorInit = false;

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
            mGroupTextView = (TextView) inflatedConvertView.findViewById(R.id.libs_search_group);
            mTitleTextView = (TextView) inflatedConvertView.findViewById(R.id.libs_search_title);
            return this;
        }

        @Override
        public void applySource(Context context, NyaaFansubGroup source)
        {
            final String groupName = source.getGroupName();
            if (groupName != null)
                mGroupTextView.setText(groupName);

            //lazy init
            if (!colorInit)
            {
                colorInit = true;
                int[] attrs = {R.attr.textColorPrimary, R.attr.textColorSecondary, R.attr.textColorTertiary};
                final TypedArray typedArray = context.obtainStyledAttributes(R.style.MinoriDark, attrs);
                primary = typedArray.getColor(0, Color.WHITE);
                secondary = typedArray.getColor(1, Color.WHITE);
                tertiary = typedArray.getColor(2, Color.WHITE);
                typedArray.recycle();
            }

            switch (source.getTrustCategory())
            {
                case REMAKES:
                    mGroupTextView.setTextColor(tertiary);
                    break;
                case TRUSTED:
                    mGroupTextView.setTextColor(primary);
                    break;
                case APLUS:
                    mGroupTextView.setTextColor(secondary);
                    break;
            }


            final String seriesTitle = source.getSeriesTitle();
            if (seriesTitle != null)
                mTitleTextView.setText(seriesTitle);
        }
    }

    private static final class NyaaSearchResultLoader extends AsyncTaskLoader<BatchData>
    {
        public static final String KEY_SEARCH_ARG = "search_arg";
        static final Comparator<NyaaFansubGroup> trustComparator = new Comparator<NyaaFansubGroup>()
        {
            @Override
            public int compare(NyaaFansubGroup lhs, NyaaFansubGroup rhs)
            {
                int result = lhs.getTrustCategory().compareTo(rhs.getTrustCategory());
                if (result == 0)
                {
                    return lhs.getGroupName().compareTo(rhs.getGroupName());
                }
                return result;
            }
        };
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
            Log.v(getClass().getSimpleName(), "Loader started");
            final List<NyaaEntry> result = new ArrayList<>();
            final String searchTerms;

            if (overridedSearchTerms != null)
            {
                searchTerms = overridedSearchTerms;
            }
            else
            {
                searchTerms = Util.getBestTerms(batchData.getCompositeData().getLiveChartObject().getTitle(), false);
            }

            CoreNetworkFactory.getNyaaEntries(searchTerms, result);
            final List<NyaaFansubGroup> searchResults = NyaaXMLParser.group(result);
            Collections.sort(searchResults, trustComparator);
            batchData.setSearchResults(searchResults);
            return batchData;
        }

        //TODO: Implement better (more correct) loader for resources release, reuse, etc etc
    }
}
