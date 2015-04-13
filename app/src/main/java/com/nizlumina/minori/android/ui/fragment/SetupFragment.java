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

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.nizlumina.minori.R;
import com.nizlumina.minori.android.controller.HummingbirdNetworkController;
import com.nizlumina.minori.android.controller.WatchlistController;
import com.nizlumina.minori.android.listener.OnFinishListener;
import com.nizlumina.minori.android.model.alarm.Alarm;
import com.nizlumina.minori.android.wrapper.ParcelableNyaaFansubGroup;
import com.nizlumina.minori.common.hummingbird.AnimeObject;
import com.nizlumina.minori.common.nyaa.NyaaEntry;
import com.nizlumina.minori.common.nyaa.NyaaFansubGroup;

import java.lang.ref.SoftReference;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class SetupFragment extends Fragment
{
    public static final String NYAAFANSUBGROUP_PARCELKEY = "com.nizlumina.minori.nyaafansubgroup_parcelkey";
    private NyaaFansubGroup mNyaaFansubGroup;
    private NyaaEntry.Resolution mSelectedRes = NyaaEntry.Resolution.DEFAULT;
    private Alarm.Mode mSelectedMode = Alarm.Mode.RELEASE_DAY;
    private String mSelectedEpisode;
    private AnimeObject mSelectedAnimeObject;
    private SoftReference<DrawerFragmentListener> mFragmentListener = new SoftReference<DrawerFragmentListener>(null);

    public static SetupFragment newInstance(DrawerFragmentListener fragmentListener)
    {
        final SetupFragment setupFragment = new SetupFragment();
        setupFragment.mFragmentListener = new SoftReference<DrawerFragmentListener>(fragmentListener);
        return setupFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ParcelableNyaaFansubGroup parcelableNyaaFansubGroup = getArguments().getParcelable(NYAAFANSUBGROUP_PARCELKEY);
        if (parcelableNyaaFansubGroup != null)
            mNyaaFansubGroup = parcelableNyaaFansubGroup.getNyaaFansubGroup();

        DrawerFragmentListener fragmentListener = mFragmentListener.get();
        if (fragmentListener != null)
        {
            //TODO
//            Toolbar toolbar = fragmentListener.getMainToolbar();
//            if (toolbar != null)
//            {
//                toolbar.setTitle(mNyaaFansubGroup.getSeriesTitle());
//                toolbar.setSubtitle(mNyaaFansubGroup.getGroupName());
//            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        Button buttonAdd, buttonNextEps, buttonCurrEps;
        EditText editTextEpisode;
        RadioGroup radioGroupRes, radioGroupMode;
        ListView listViewHumm;
        TextView guideHummData, guideEpisode, guideRes, guideMode;

        //This part is risky to write down due to the amount of views needed. Care needed when changing this part.

        //Series data
        guideHummData = (TextView) view.findViewById(R.id.setup_humm_guide);
        assignSimpleDialogOnClick(guideHummData, R.layout.dialog_test);

        listViewHumm = (ListView) view.findViewById(R.id.setup_humm_listview);
        setupHummListView(listViewHumm);

        //Series episode
        guideEpisode = (TextView) view.findViewById(R.id.setup_episode_guide);

        editTextEpisode = (EditText) view.findViewById(R.id.setup_episode_edittext);
        buttonCurrEps = (Button) view.findViewById(R.id.setup_episode_currenteps_button);
        buttonNextEps = (Button) view.findViewById(R.id.setup_episode_nexteps_button);
        setupEpisodeViews(editTextEpisode, buttonNextEps, buttonCurrEps);

        //Resolution
        guideRes = (TextView) view.findViewById(R.id.setup_res_guide);

        radioGroupRes = (RadioGroup) view.findViewById(R.id.setup_res_radiogroup);
        setupResRadioGroup(radioGroupRes);

        //Mode
        guideMode = (TextView) view.findViewById(R.id.setup_mode_guide);
        radioGroupMode = (RadioGroup) view.findViewById(R.id.setup_mode_radiogroup);
        setupModeRadioGroup(radioGroupMode);

        //Add
        buttonAdd = (Button) view.findViewById(R.id.setup_add_button);
        finalizeWatchDataSelection(buttonAdd);
    }

    private void setupHummListView(final ListView listView)
    {
        if (listView != null)
        {
            HummingbirdNetworkController controller = new HummingbirdNetworkController();
            final ArrayAdapter<AnimeObject> mAnimeObjectAdapter = new ArrayAdapter<AnimeObject>(getActivity(), R.layout.list_item_singletext)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    TextView textView = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.list_item_singletext, parent, false);
                    textView.setText(getItem(position).getTitle());
                    return textView;
                }
            };

            listView.setAdapter(mAnimeObjectAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    listView.setItemChecked(position, true);
                    mSelectedAnimeObject = mAnimeObjectAdapter.getItem(position);
                }
            });


            if (mNyaaFansubGroup != null)
            {
                controller.searchAnimeAsync(mNyaaFansubGroup.getSeriesTitle(), new OnFinishListener<List<AnimeObject>>()
                {
                    @Override
                    public void onFinish(final List<AnimeObject> result)
                    {
                        if (getActivity() != null)
                        {
                            Log.v(SetupFragment.class.getSimpleName(), "Humm Search result:" + result.size());
                            getActivity().runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    if (mAnimeObjectAdapter.getCount() > 0)
                                        mAnimeObjectAdapter.clear();

                                    mAnimeObjectAdapter.addAll(result);
                                    mAnimeObjectAdapter.notifyDataSetChanged();
                                    if (result.size() == 1)
                                    {
                                        listView.setItemChecked(0, true);
                                        mSelectedAnimeObject = mAnimeObjectAdapter.getItem(0);
                                    }
                                }
                            });
                        }
                    }
                });
            }

        }
    }

    private void setupEpisodeViews(final EditText editText, final Button nextEps, final Button currEps)
    {
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                mSelectedEpisode = v.getText().toString().trim();
                return false;
            }
        });

        nextEps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String nextEps = String.valueOf(mNyaaFansubGroup.getLatestEpisode() + 1);
                mSelectedEpisode = nextEps;
                editText.setText(nextEps);
            }
        });

        currEps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String currEps = String.valueOf(mNyaaFansubGroup.getLatestEpisode());
                mSelectedEpisode = currEps;
                editText.setText(currEps);
            }
        });
    }

    private void setupResRadioGroup(final RadioGroup resRadioGroup)
    {
        Map<NyaaEntry.Resolution, View> resViewMap = new Hashtable<>();

        //The below only works due to enum values + length + order correspond to the ones in radiogroup
        //Code will fail on purpose if child views is added without adding to the original enums
        Log.v(getClass().getSimpleName(), "Resolution child size: " + resRadioGroup.getChildCount());

        for (int i = 0; i < resRadioGroup.getChildCount(); i++)
        {
            resViewMap.put(NyaaEntry.Resolution.values()[i], resRadioGroup.getChildAt(i));
        }

        final List<NyaaEntry.Resolution> resolutions = mNyaaFansubGroup.getResolutions();
        if (resolutions.size() > 0)
        {
            for (final NyaaEntry.Resolution resolution : resolutions)
            {
                //Force resolution option. DEFAULT is special case where no resolution at all is provided with the NyaaEntry.
                if (resolution != NyaaEntry.Resolution.DEFAULT)
                {
                    RadioButton radioButton = (RadioButton) resViewMap.get(resolution);
                    radioButton.setEnabled(true);
                    radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                    {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                        {
                            if (isChecked)
                                mSelectedRes = resolution;
                        }
                    });
                }
            }

            //init the default choice. If only DEFAULT resolution is available, this also works.
            mSelectedRes = resolutions.get(resolutions.size() - 1);
            ((RadioButton) resViewMap.get(mSelectedRes)).setChecked(true);
        }
        else
        {
            resViewMap.get(NyaaEntry.Resolution.DEFAULT).setEnabled(true);
        }
    }

    private void setupModeRadioGroup(final RadioGroup modeRadioGroup)
    {
        if (modeRadioGroup != null)
        {
            Map<Alarm.Mode, View> modeViewMap = new Hashtable<>();

            //Same implementation as above with few changes
            for (int i = 0; i < modeRadioGroup.getChildCount(); i++)
            {
                modeViewMap.put(Alarm.Mode.values()[i], modeRadioGroup.getChildAt(i));
            }

            for (final Alarm.Mode mode : Alarm.Mode.values())
            {

                RadioButton radioButton = (RadioButton) modeViewMap.get(mode);
                if (mode == Alarm.Mode.RELEASE_DAY)
                {
                    //init the default choice
                    radioButton.setChecked(true);
                }

                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                    {
                        if (isChecked)
                            mSelectedMode = mode;
                    }
                });
            }
        }
    }

    private void assignSimpleDialogOnClick(final View view, @LayoutRes final int layoutResourceID)
    {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(layoutResourceID, true)
                .theme(Theme.DARK)
                .positiveText(R.string.dialog_dismiss)
                .build();

        view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.show();
            }
        });
    }

    private void finalizeWatchDataSelection(final View triggerButton)
    {
        triggerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                WatchlistController controller = new WatchlistController();
                controller.addNewWatchData(getActivity(), mNyaaFansubGroup.getLatestEntry(mSelectedRes), mSelectedEpisode, mSelectedAnimeObject, mSelectedMode);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_setup, container, false);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }
}
