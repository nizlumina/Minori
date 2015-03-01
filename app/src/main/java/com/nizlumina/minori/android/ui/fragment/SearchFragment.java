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

package com.nizlumina.minori.android.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.SearchController;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.ui.adapter.GenericAdapter;
import com.nizlumina.minori.android.wrapper.ParcelableNyaaFansubGroup;
import com.nizlumina.minori.common.Nyaa.NyaaEntry;
import com.nizlumina.minori.common.Nyaa.NyaaFansubGroup;
import com.nizlumina.minori.common.Nyaa.Parser.NyaaXMLParser;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment
{
    private final SearchController mSearchController = new SearchController();
    private GenericAdapter<NyaaFansubGroup> mGenericAdapter;

    public SearchFragment() {}

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        final EditText searchText = (EditText) view.findViewById(R.id.sf_search_edittext);
        final ImageButton searchButton = (ImageButton) view.findViewById(R.id.sf_fab_search);

        setupSearch(searchText, searchButton);

        final ListView listView = (ListView) view.findViewById(R.id.sf_listview);
        setupList(listView);
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
                NyaaFansubGroup nyaaFansubGroup = mGenericAdapter.getItem(position);
                if (nyaaFansubGroup != null)
                {
                    //Because the Android framework literally force you to do shit like this
                    SetupFragment setupFragment = new SetupFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(SetupFragment.NYAAFANSUBGROUP_PARCELKEY, new ParcelableNyaaFansubGroup(nyaaFansubGroup));
                    setupFragment.setArguments(bundle);

                    getFragmentManager().beginTransaction().replace(R.id.base_contentfragment, setupFragment).addToBackStack(SearchFragment.class.getSimpleName()).commit();
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
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    //do search
                    String terms = searchText.getText().toString().trim();
                    if (terms.length() > 0) invokeSearch(terms);
                }
                return false;
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                searchText.onEditorAction(EditorInfo.IME_ACTION_DONE); // this will do the above
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    private void invokeSearch(String terms)
    {
        mGenericAdapter.clear();
        mSearchController.searchNyaa(terms, new OnFinishListener<List<NyaaEntry>>()
        {
            @Override
            public void onFinish(final List<NyaaEntry> result)
            {
                Log.v(SearchFragment.class.getSimpleName(), "Nyaa Results: " + result.size());
                if (getActivity() != null)
                {
                    final List<NyaaFansubGroup> groups = NyaaXMLParser.Group(result);
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mGenericAdapter.addAll(groups);
                        }
                    });
                }
            }
        });
    }

    private static class SearchItemHolder implements GenericAdapter.ViewHolder<NyaaFansubGroup>
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

            View container = inflatedConvertView.findViewById(R.id.qualifiers_container);
            trust = (TextView) container.findViewById(R.id.text_trust);
            resolution = (TextView) container.findViewById(R.id.text_res);
            quality = (TextView) container.findViewById(R.id.text_quality);
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
