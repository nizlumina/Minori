package com.nizlumina.minori.android.factory;

import com.nizlumina.minori.android.network.ConnectionUnit;
import com.nizlumina.minori.android.network.CoreQuery;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;
import com.nizlumina.minori.core.Nyaa.Parser.NyaaXMLParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Use this to build the WatchData members from the network.
 * Threaded calls/delegates should be handled elsewhere.
 */
public class CoreNetworkFactory
{

    public static synchronized void getNyaaEntries(final String searchTerms, final ArrayList<NyaaEntry> outputList, final ConnectionUnit.NetworkProgressListener listener)
    {
        ConnectionUnit unit = new ConnectionUnit();

        ConnectionUnit.Callable callable = new ConnectionUnit.Callable()
        {
            @Override
            public void onStreamReceived(InputStream inputStream)
            {
                NyaaXMLParser parser = new NyaaXMLParser();
                outputList.addAll(parser.Parse(inputStream));
            }
        };

        unit.invokeOnStream(CoreQuery.Nyaa.getEnglishSubRSS(searchTerms).toString(), callable, listener);
    }

    public static synchronized void getAnimeObject(final String searchTerms, final ArrayList<AnimeObject> outputList, final ConnectionUnit.NetworkProgressListener listener)
    {
        ConnectionUnit unit = new ConnectionUnit();

        String resultString = unit.getResponseString(CoreQuery.Hummingbird.searchQuery(searchTerms).toString(), listener);
        if (resultString != null)
        {
            try
            {
                JSONArray jsonArray = new JSONArray(resultString);
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    AnimeObject animeObject = CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
                    if (animeObject != null) outputList.add(animeObject);
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }
}
