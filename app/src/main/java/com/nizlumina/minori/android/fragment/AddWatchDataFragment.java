package com.nizlumina.minori.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.ThreadController;
import com.nizlumina.minori.android.factory.CoreNetworkFactory;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Fragment to add watch data
 */
public class AddWatchDataFragment extends Fragment
{
    public static final String FRAG_SEARCH_TERMS_ARG = "fragment_search_terms_arg";
    WeakReference<View> containerWeakReference;

    public AddWatchDataFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        containerWeakReference = new WeakReference<View>(inflater.inflate(R.layout.fragment_setup, container, false));
        return containerWeakReference.get();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        View mainContainer = containerWeakReference.get();

        ListView listView = (ListView) mainContainer.findViewById(R.id.setup_humm_listview);
        if (listView != null)
        {
            loadIntoList(listView, getArguments().getString(FRAG_SEARCH_TERMS_ARG));
        }


    }

    private void loadIntoList(final ListView listView, final String searchTerms)
    {
        final ArrayList<AnimeObject> animeObjects = new ArrayList<AnimeObject>();
        final HummingbirdAdapter hmAdapter = new HummingbirdAdapter(animeObjects);
        listView.setAdapter(hmAdapter);
        if (searchTerms != null)
        {
            ThreadController.post(new Callable()
            {
                @Override
                public Object call() throws Exception
                {
                    CoreNetworkFactory.getAnimeObject(searchTerms, animeObjects, null);
                    return null;
                }
            }, new Callable()
            {
                @Override
                public Object call() throws Exception
                {
                    hmAdapter.notifyDataSetChanged();
                    return null;
                }
            });

        }

    }

    private class HummingbirdAdapter extends BaseAdapter
    {
        private final ArrayList<AnimeObject> mAnimeObjects;

        public HummingbirdAdapter(ArrayList<AnimeObject> animeObjects)
        {
            mAnimeObjects = animeObjects;
        }

        @Override
        public int getCount()
        {
            return mAnimeObjects.size();
        }

        @Override
        public AnimeObject getItem(int position)
        {
            return mAnimeObjects.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return mAnimeObjects.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.item_animeobject_checked_textview, parent, false);
            }

            ((CheckedTextView) convertView).setText(getItem(position).title);

            return convertView;
        }
    }
}
