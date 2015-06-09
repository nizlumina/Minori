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

package com.nizlumina.minori.internal.factory;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.common.nyaa.Parser.NyaaXMLParser;
import com.nizlumina.minori.internal.network.CoreQuery;
import com.nizlumina.minori.internal.network.WebUnit;

import java.io.IOException;
import java.io.InputStream;
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
                outputList.addAll(parser.parse(inputStream));
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

//    public static void getAnimeObject(final String searchTerms, final ArrayList<AnimeObject> outputList)
//    {
//        WebUnit unit = new WebUnit();
//        try
//        {
//            String resultString = unit.getString(CoreQuery.Hummingbird.searchQuery(searchTerms).toString());
//            if (resultString != null)
//            {
//
//                JSONArray jsonArray = new JSONArray(resultString);
//                for (int i = 0; i < jsonArray.length(); i++)
//                {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//                    AnimeObject animeObject = CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
//                    if (animeObject != null) outputList.add(animeObject);
//                }
//
//            }
//        }
//        catch (JSONException | IOException e)
//        {
//            e.printStackTrace();
//        }
//    }

//    public static AnimeObject getAnimeObject(String slugOrID)
//    {
//        WebUnit unit = new WebUnit();
//        try
//        {
//            String resultString = unit.getString(CoreQuery.Hummingbird.getAnimeByID(slugOrID).toString());
//            JSONObject jsonObject = new JSONObject(resultString);
//            return CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
//        }
//        catch (JSONException | IOException e)
//        {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public static void getAnimeObjectAsync(final String slugOrID, final WebUnitListener<AnimeObject> listener)
//    {
//        getAnimeObjectAsync(slugOrID, listener, null);
//    }


//    public static void getAnimeObjectAsync(final String slugOrID, final WebUnitListener<AnimeObject> listener, final WebUnit webUnit)
//    {
//        final WebUnit unit = getWebUnit(webUnit);
//
//        try
//        {
//            Util.logThread("CoreNetworkFactory");
//            unit.enqueueGetString(CoreQuery.Hummingbird.getAnimeByID(slugOrID).toString(), new WebUnit.WebUnitStringListener()
//            {
//                @Override
//                public void onFailure()
//                {
//                    if (listener != null) listener.onFailure();
//                }
//
//                @Override
//                public void onFinish(String responseBody)
//                {
//                    JSONObject jsonObject = null;
//                    try
//                    {
//                        jsonObject = new JSONObject(responseBody);
//                    }
//                    catch (JSONException e)
//                    {
//                        e.printStackTrace();
//                        this.onFailure();
//                    }
//
//                    if (jsonObject != null)
//                    {
//                        AnimeObject animeObject = CoreJSONFactory.animeObjectFromJSON(jsonObject, true);
//                        if (listener != null)
//                            listener.onFinish(animeObject);
//                    }
//                    else
//                    {
//                        if (listener != null)
//                            listener.onFinish(null);
//                    }
//                }
//            });
//
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }

//    public static void searchAnimeAsync(final String terms, final WebUnitListener<List<AnimeObject>> resultsListener)
//    {
//        final WebUnit unit = new WebUnit();
//        try
//        {
//            unit.enqueueGetString(CoreQuery.Hummingbird.searchQuery(terms).toString(), new WebUnit.WebUnitStringListener()
//            {
//                @Override
//                public void onFailure()
//                {
//                    if (resultsListener != null) resultsListener.onFailure();
//                }
//
//                @Override
//                public void onFinish(String responseBody)
//                {
//                    JSONArray jsonArray = null;
//                    try
//                    {
//                        jsonArray = new JSONArray(responseBody);
//                    }
//                    catch (JSONException e)
//                    {
//                        e.printStackTrace();
//                    }
//
//                    if (jsonArray != null)
//                    {
//                        List<AnimeObject> animeObjects = CoreJSONFactory.animeObjectsFromJSON(jsonArray, true);
//                        if (resultsListener != null) resultsListener.onFinish(animeObjects);
//                    }
//                    else
//                    {
//                        if (resultsListener != null) resultsListener.onFinish(null);
//                    }
//                }
//            });
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//    }

    private static WebUnit getWebUnit(WebUnit overrideWebUnit)
    {
        if (overrideWebUnit == null) return new WebUnit();
        else return overrideWebUnit;
    }

}
