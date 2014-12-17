package com.nizlumina.minori.android.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.WatchlistController;
import com.nizlumina.minori.android.data.WatchData;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Refactored version
 */
public class GalleryFragment extends android.support.v4.app.Fragment
{
    WeakReference<GridView> gridViewWeakReference;

    public GalleryFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
    {
        gridViewWeakReference = new WeakReference<GridView>((GridView) inflater.inflate(R.layout.fragment_gridview, container, false));

//            GalleryAdapter adapter = new GalleryAdapter(WatchlistSingleton.getInstance().getDataList());

        //test


        return gridViewWeakReference.get();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final ArrayList<WatchData> watchDatas = new ArrayList<WatchData>();
        final ArrayList<NyaaEntry> results = new ArrayList<NyaaEntry>();
        final GalleryAdapter adapter = new GalleryAdapter(getActivity(), watchDatas);
        WatchlistController controller = new WatchlistController();

        gridViewWeakReference.get().setAdapter(adapter);
    }

    public static class GalleryAdapter extends BaseAdapter
    {
        WeakReference<Context> contextWeakReference;
        private ArrayList<WatchData> watchDatas;

        public GalleryAdapter(Context context, ArrayList<WatchData> watchDataList)
        {
            contextWeakReference = new WeakReference<Context>(context);
            this.watchDatas = watchDataList;
        }

        @Override
        public int getCount()
        {
            return watchDatas.size();
        }

        @Override
        public WatchData getItem(int position)
        {
            return watchDatas.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return watchDatas.get(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            GalleryItemHolder holder;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(contextWeakReference.get()).inflate(R.layout.list_item_compact, null);
                holder = new GalleryItemHolder(convertView, R.id.item_image, R.id.item_title, R.id.item_group, R.id.item_episode);
                convertView.setTag(holder);
            }
            else
            {
                holder = (GalleryItemHolder) convertView.getTag();
            }

            WatchData item = watchDatas.get(position);
            if (item != null)
            {
                AnimeObject animeObject = item.getAnimeObject();
                if (animeObject != null && animeObject.cachedImageURI != null)
                {
                    holder.setCover(Drawable.createFromPath(animeObject.cachedImageURI));
                }

                String title = item.getNyaaEntry().title;
                String fansub = item.getNyaaEntry().fansub;
                String episodeString = item.getNyaaEntry().episodeString;
                if (title != null && fansub != null && episodeString != null)
                {
                    holder.setText(title, fansub, episodeString);
                }
            }

            return convertView;
        }

        public static class GalleryItemHolder
        {
            ImageView cover;
            TextView title, group, episode;

            public GalleryItemHolder(View convertView, int coverId, int titleId, int groupId, int episodeId)
            {
                cover = (ImageView) convertView.findViewById(coverId);
                title = (TextView) convertView.findViewById(titleId);
                group = (TextView) convertView.findViewById(groupId);
                episode = (TextView) convertView.findViewById(episodeId);
            }

            public void setCover(Drawable drawable)
            {
                if (cover != null) cover.setImageDrawable(drawable);
            }

            public void setText(String title, String group, String episode)
            {
                if (this.title != null)
                {
                    this.title.setText(title);
                }
                if (this.group != null)
                {
                    this.group.setText(group);
                }
                if (this.episode != null)
                {
                    this.episode.setText(episode);
                }
            }
        }
    }
}

