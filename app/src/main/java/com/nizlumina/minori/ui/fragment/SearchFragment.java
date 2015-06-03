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
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.common.nyaa.NyaaFansubGroup;
import com.nizlumina.common.nyaa.Parser.NyaaXMLParser;
import com.nizlumina.minori.R;
import com.nizlumina.minori.controller.SearchController;
import com.nizlumina.minori.listener.OnFinishListener;
import com.nizlumina.minori.ui.ToolbarContract;
import com.nizlumina.minori.ui.adapter.GenericAdapter;
import com.nizlumina.minori.utility.ParcelableNyaaFansubGroup;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends DrawerContentFragment
{
    public static final String SEARCH_STRING = "SEARCH_STRING";
    private final SearchController mSearchController = new SearchController();
    private GenericAdapter<NyaaFansubGroup> mGenericAdapter;
    private ProgressBar mProgressBar;

    public SearchFragment() {}

    public static SearchFragment newInstance(@Nullable String searchString)
    {
        Bundle bundle = new Bundle();
        if (searchString != null)
        {
            bundle.putString(SEARCH_STRING, searchString);
        }
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(bundle);
        return searchFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.view_search_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final ToolbarContract toolbarContract = getToolbarContract();
        final EditText searchText = (EditText) toolbarContract.setToolbarChild(inflater, R.layout.view_search_edittext);

        final ImageButton searchButton = null;

        setupSearch(searchText, searchButton);

        final ListView listView = (ListView) view;

        mProgressBar = (ProgressBar) toolbarContract.setToolbarSiblingView(inflater, R.layout.view_progressbar);
        mProgressBar.setVisibility(View.GONE);
        listView.setOnScrollListener(toolbarContract.getAutoDisplayToolbarListener());

        setupList(listView);

        final Bundle args = getArguments();
        if (args != null)
        {
            String searchTerms = args.getString(SEARCH_STRING);
            if (searchTerms != null)
            {
                invokeSearch(searchTerms);
            }
        }
    }

    private void setupList(ListView listView)
    {
        mGenericAdapter = new GenericAdapter<NyaaFansubGroup>(getActivity(), new ArrayList<NyaaFansubGroup>(), new SearchItemHolder());
        mGenericAdapter.setNotifyOnChange(true);
        listView.setAdapter(mGenericAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                final NyaaFansubGroup nyaaFansubGroup = mGenericAdapter.getItem(position);
                if (nyaaFansubGroup != null)
                {
                    //Because the Android framework literally force you to do shit like this
                    final SetupFragment setupFragment = new SetupFragment();
                    final Bundle bundle = new Bundle();
                    bundle.putParcelable(SetupFragment.NYAAFANSUBGROUP_PARCELKEY, new ParcelableNyaaFansubGroup(nyaaFansubGroup));
                    setupFragment.setArguments(bundle);

                    getFragmentManager().beginTransaction().replace(R.id.base_content_container, setupFragment).addToBackStack(SearchFragment.class.getSimpleName()).commit();
                }
            }
        });
    }


    //This is a very useful template method for setting a pseudo-search view
    private void setupSearch(final EditText searchText, final ImageButton searchButton)
    {
        searchText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    //do search
                    String terms = searchText.getText().toString().trim();
                    if (terms.length() > 0) invokeSearch(terms);
                }
                return false;
            }
        });

//        searchButton.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                searchText.onEditorAction(EditorInfo.IME_ACTION_DONE); // this will do the above
//            }
//        });
    }

    private void invokeSearch(String terms)
    {
        mProgressBar.setVisibility(View.VISIBLE);
        mGenericAdapter.clear();
        mSearchController.searchNyaa(terms, new OnFinishListener<List<NyaaEntry>>()
        {
            @Override
            public void onFinish(final List<NyaaEntry> result)
            {
                if (getActivity() != null && result != null)
                {
                    Log.v(SearchFragment.class.getSimpleName(), "Nyaa Results: " + result.size());
                    final List<NyaaFansubGroup> groups = NyaaXMLParser.group(result);
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (mGenericAdapter != null) mGenericAdapter.addAll(groups);
                            if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private class SearchItemHolder implements GenericAdapter.ViewHolder<NyaaFansubGroup>
    {
        private TextView episode;
        private TextView title;
        private TextView group;
        private TextView trust;
        private TextView resolution;
        private TextView quality;

        @Override
        public int getLayoutResource()
        {
            return R.layout.list_item_search;
        }

        @Override
        public GenericAdapter.ViewHolder<NyaaFansubGroup> getNewInstance()
        {
            return new SearchItemHolder();
        }

        @Override
        public GenericAdapter.ViewHolder<NyaaFansubGroup> setupViewSource(final View inflatedConvertView)
        {
            title = (TextView) inflatedConvertView.findViewById(R.id.text_title);
            episode = (TextView) inflatedConvertView.findViewById(R.id.text_episode);
            group = (TextView) inflatedConvertView.findViewById(R.id.text_group);

            trust = (TextView) inflatedConvertView.findViewById(R.id.text_trust);
            resolution = (TextView) inflatedConvertView.findViewById(R.id.text_res);
            quality = (TextView) inflatedConvertView.findViewById(R.id.text_quality);
            return this;
        }

        @Override
        public void applySource(final Context context, final NyaaFansubGroup fansubGroup)
        {
            //Log.v(SearchFragment.class.getSimpleName(), fansubGroup.stringData());
            title.setText(fansubGroup.getSeriesTitle());
            episode.setText(String.valueOf(fansubGroup.getLatestEpisode()));
            group.setText(fansubGroup.getGroupName());
            if (fansubGroup.getTrustCategory() != NyaaEntry.Trust.ALL)
            {
                if (trust.getVisibility() != View.VISIBLE) trust.setVisibility(View.VISIBLE);

                trust.setText(NyaaEntry.Trust.getTrustString(fansubGroup.getTrustCategory()));
            }
            else trust.setVisibility(View.GONE);

            if (fansubGroup.getResolutions().size() > 0)
            {
                if (resolution.getVisibility() != View.VISIBLE)
                    resolution.setVisibility(View.VISIBLE);

                String resDisplayString = fansubGroup.getResolutionDisplayString();
                if (resDisplayString != null) resolution.setText(resDisplayString);
            }
            else resolution.setVisibility(View.GONE);

            if (fansubGroup.getQualities().size() > 0)
            {
                if (quality.getVisibility() != View.VISIBLE) quality.setVisibility(View.VISIBLE);

                quality.setText(fansubGroup.getQualities().get(0));
            }
            else quality.setVisibility(View.GONE);
        }
    }

}
