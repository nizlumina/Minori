package com.nizlumina.minori.internal.factory;

import com.nizlumina.common.hummingbird.AnimeObject;
import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.minori.model.WatchData;
import com.nizlumina.minori.model.alarm.Alarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON Factory for WatchData
 */
public class WatchDataJSONFactory
{
    private static final String WATCH_DATA_ID = "WATCH_DATA_ID";
    private static final String NYAA_ENTRY = "NYAA_ENTRY";
    private static final String ALARM_DATA = "ALARM_DATA";
    private static final String ANIME_OBJECT = "ANIME_OBJECT";

    public static JSONArray getJSONArray(final List<WatchData> inputList)
    {
        JSONArray jsonArray = new JSONArray();
        for (WatchData watchData : inputList)
        {
            jsonArray.put(makeJSON(watchData));
        }
        return jsonArray;
    }


    public static List<WatchData> fromJSONArray(final JSONArray jsonArray)
    {
        List<WatchData> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                WatchData watchData = fromJSON(jsonObject);
                if (watchData != null) result.add(watchData);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static JSONObject makeJSON(WatchData watchData)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(WATCH_DATA_ID, watchData.getId());
            jsonObject.put(NYAA_ENTRY, CoreJSONFactory.toJSON(watchData.getNyaaEntry()));
            jsonObject.put(ALARM_DATA, CoreJSONFactory.toJSON(watchData.getAlarm()));
            jsonObject.put(ANIME_OBJECT, CoreJSONFactory.toJSON(watchData.getAnimeObject(), false));
            return jsonObject;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This will return null if any of its parts threw an exception.
     *
     * @param jsonObject The JSONObject to be read from
     * @return WatchData from reading the JSONObject
     */
    public static WatchData fromJSON(JSONObject jsonObject)
    {
        NyaaEntry nyaaEntry = CoreJSONFactory.nyaaEntryFromJSON(jsonObject);
        Alarm alarm = CoreJSONFactory.alarmFromJSON(jsonObject);
        AnimeObject animeObject = CoreJSONFactory.animeObjectFromJSON(jsonObject, false);

        if (nyaaEntry != null && alarm != null && animeObject != null)
        {
            WatchData watchData = new WatchData();
            watchData.setNyaaEntry(nyaaEntry);
            watchData.setAlarm(alarm);
            watchData.setAnimeObject(animeObject);
            return watchData;
        }
        return null;
    }
}
