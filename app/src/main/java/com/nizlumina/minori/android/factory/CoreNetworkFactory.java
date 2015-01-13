package com.nizlumina.minori.android.factory;

import com.nizlumina.minori.android.network.CoreQuery;
import com.nizlumina.minori.android.network.WebUnit;
import com.nizlumina.minori.android.utility.Util;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;
import com.nizlumina.minori.core.Nyaa.Parser.NyaaXMLParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Use this to build the WatchData members from the network.
 * Threaded calls/delegates should be handled elsewhere.
 */
public class CoreNetworkFactory
{
    public static void getNyaaEntries(final String searchTerms, final ArrayList<NyaaEntry> outputList)
    {
        WebUnit unit = new WebUnit();

        WebUnit.StreamCallable callable = new WebUnit.StreamCallable()
        {
            @Override
            public void onStreamReceived(InputStream inputStream)
            {
                NyaaXMLParser parser = new NyaaXMLParser();
                outputList.addAll(parser.Parse(inputStream));
            }
        };

        try
        {
            unit.invokeOnStream(CoreQuery.Nyaa.getEnglishSubRSS(searchTerms).toString(), callable);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void getAnimeObject(final String searchTerms, final ArrayList<AnimeObject> outputList)
    {
        WebUnit unit = new WebUnit();
        try
        {
            String resultString = unit.getString(CoreQuery.Hummingbird.searchQuery(searchTerms).toString());
            if (resultString != null)
            {

                JSONArray jsonArray = new JSONArray(resultString);
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    AnimeObject animeObject = CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
                    if (animeObject != null) outputList.add(animeObject);
                }

            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static AnimeObject getAnimeObject(String slugOrID)
    {
        WebUnit unit = new WebUnit();
        try
        {
            String resultString = unit.getString(CoreQuery.Hummingbird.getAnimeByID(slugOrID).toString());
            JSONObject jsonObject = new JSONObject(resultString);
            return CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void getAnimeObjectAsync(final String slugOrID, final NetworkFactoryListener<AnimeObject> listener)
    {
        WebUnit unit = new WebUnit();
        try
        {
            Util.logThread("CoreNetworkFactory");
            unit.enqueueGetString(CoreQuery.Hummingbird.getAnimeByID(slugOrID).toString(), new WebUnit.WebUnitListener()
            {
                @Override
                public void onFailure()
                {

                }

                @Override
                public void onFinish(String responseBody)
                {
                    JSONObject jsonObject = null;
                    try
                    {
                        jsonObject = new JSONObject(responseBody);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                        this.onFailure();
                    }
                    AnimeObject animeObject = CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
                    if (listener != null) listener.onFinish(animeObject);
                }
            });

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public interface NetworkFactoryListener<T>
    {
        public void onFinish(T result);
    }
}
