package com.nizlumina.minori.android.internal.factory;

import com.nizlumina.minori.android.internal.network.CoreQuery;
import com.nizlumina.minori.android.internal.network.WebUnit;
import com.nizlumina.minori.android.listener.WebUnitListener;
import com.nizlumina.minori.android.utility.Util;
import com.nizlumina.minori.common.hummingbird.AnimeObject;
import com.nizlumina.minori.common.nyaa.NyaaEntry;
import com.nizlumina.minori.common.nyaa.Parser.NyaaXMLParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Use this to build the WatchData members from the network.
 * Threaded calls/delegates should be handled elsewhere.
 */
public class CoreNetworkFactory
{
    public static void getNyaaEntries(final String searchTerms, final List<NyaaEntry> outputList)
    {
        getNyaaEntries(searchTerms, outputList, null);
    }

    public static void getNyaaEntries(final String searchTerms, final List<NyaaEntry> outputList, WebUnit webUnit)
    {
        WebUnit unit = getWebUnit(webUnit);

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

    public static void getAnimeObjectAsync(final String slugOrID, final WebUnitListener<AnimeObject> listener)
    {
        getAnimeObjectAsync(slugOrID, listener, null);
    }


    public static void getAnimeObjectAsync(final String slugOrID, final WebUnitListener<AnimeObject> listener, final WebUnit webUnit)
    {
        final WebUnit unit = getWebUnit(webUnit);

        try
        {
            Util.logThread("CoreNetworkFactory");
            unit.enqueueGetString(CoreQuery.Hummingbird.getAnimeByID(slugOrID).toString(), new WebUnit.WebUnitStringListener()
            {
                @Override
                public void onFailure()
                {
                    if (listener != null) listener.onFailure();
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

                    if (jsonObject != null)
                    {
                        AnimeObject animeObject = CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
                        if (listener != null)
                            listener.onFinish(animeObject);
                    }
                    else
                    {
                        if (listener != null)
                            listener.onFinish(null);
                    }
                }
            });

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void searchAnimeAsync(final String terms, final WebUnitListener<List<AnimeObject>> resultsListener)
    {
        final WebUnit unit = new WebUnit();
        try
        {
            unit.enqueueGetString(CoreQuery.Hummingbird.searchQuery(terms).toString(), new WebUnit.WebUnitStringListener()
            {
                @Override
                public void onFailure()
                {
                    if (resultsListener != null) resultsListener.onFailure();
                }

                @Override
                public void onFinish(String responseBody)
                {
                    JSONArray jsonArray = null;
                    try
                    {
                        jsonArray = new JSONArray(responseBody);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }

                    if (jsonArray != null)
                    {
                        List<AnimeObject> animeObjects = CoreJSONFactory.animeObjectsFromJSON(jsonArray, true);
                        if (resultsListener != null) resultsListener.onFinish(animeObjects);
                    }
                    else
                    {
                        if (resultsListener != null) resultsListener.onFinish(null);
                    }
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static WebUnit getWebUnit(WebUnit overrideWebUnit)
    {
        if (overrideWebUnit == null) return new WebUnit();
        else return overrideWebUnit;
    }

}
