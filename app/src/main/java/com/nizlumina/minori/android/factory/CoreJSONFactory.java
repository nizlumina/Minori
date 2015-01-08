package com.nizlumina.minori.android.factory;

import com.nizlumina.minori.android.alarm.Alarm;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Static factory for saving and loading Core data.
 */
public class CoreJSONFactory
{
    public static NyaaEntry nyaaEntryFromJSON(JSONObject jsonObject)
    {
        NyaaEntry entry = new NyaaEntry();
        try
        {
            entry.id = jsonObject.getInt(NyaaEntry.Static.ID);
            entry.title = jsonObject.getString(NyaaEntry.Static.TITLE);
            entry.fansub = jsonObject.getString(NyaaEntry.Static.FANSUB);

            String mTrustCat = jsonObject.getString(NyaaEntry.Static.TRUST);
            if (mTrustCat.equalsIgnoreCase(NyaaEntry.Trust.APLUS.name()))
                entry.trustCategory = NyaaEntry.Trust.APLUS;
            else if (mTrustCat.equalsIgnoreCase(NyaaEntry.Trust.TRUSTED.name()))
                entry.trustCategory = NyaaEntry.Trust.TRUSTED;
            else if (mTrustCat.equalsIgnoreCase(NyaaEntry.Trust.REMAKES.name()))
                entry.trustCategory = NyaaEntry.Trust.REMAKES;
            else entry.trustCategory = NyaaEntry.Trust.ALL;

            String mResolution = jsonObject.getString(NyaaEntry.Static.RESOLUTION);
            if (mResolution == null)
            {
                entry.resolutionString = null;
                entry.resolution = NyaaEntry.Resolution.DEFAULT;
            }
            else
            {
                entry.resolutionString = mResolution;
                entry.resolution = NyaaEntry.Resolution.matchResolution(mResolution);
            }

            String mQuality = jsonObject.getString(NyaaEntry.Static.QUALITY);
            if (mQuality.equals(NyaaEntry.Static.UNDEFINED_STRING))
                entry.quality = null;
            else
                entry.quality = mQuality;

            entry.currentEpisode = jsonObject.getInt(NyaaEntry.Static.CURRENT_EPISODE);
            entry.episodeString = jsonObject.getString(NyaaEntry.Static.CURRENT_EPISODE_STRING);
            entry.pubDate = jsonObject.getString(NyaaEntry.Static.PUBDATE);

            return entry;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;

    }

    public static AnimeObject animeObjectFromJSON(JSONObject jsonObject, boolean hummingbirdImageSource)
    {
        AnimeObject animeObject = new AnimeObject();
        try
        {
            animeObject.id = jsonObject.getInt(AnimeObject.JSON_ID);
            animeObject.status = jsonObject.getString(AnimeObject.JSON_STATUS);
            animeObject.url = jsonObject.getString(AnimeObject.JSON_URL);
            animeObject.title = jsonObject.getString(AnimeObject.JSON_TITLE);
            animeObject.episodeCount = jsonObject.getInt(AnimeObject.JSON_EPS_COUNT);
            animeObject.imageUrl = jsonObject.getString(AnimeObject.JSON_COVER_IMG);
            animeObject.synopsis = jsonObject.getString(AnimeObject.JSON_SYNOPSIS);
            animeObject.startedAiring = jsonObject.getString(AnimeObject.JSON_STARTED_AIRING);
            animeObject.finishedAiring = jsonObject.getString(AnimeObject.JSON_FINISHED_AIRING);

            if (!hummingbirdImageSource)
                animeObject.cachedImageURI = jsonObject.getString(AnimeObject.JSON_NONAPI_CACHED_IMG_URI);
            return animeObject;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static List<AnimeObject> animeObjectsFromJSON(JSONArray jsonArray, boolean hummingbirdImageSource)
    {
        List<AnimeObject> results = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null)
                    results.add(animeObjectFromJSON(jsonObject, hummingbirdImageSource));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return results;
    }

    public static Alarm alarmFromJSON(JSONObject jsonObject)
    {
        try
        {
            String mPubDate = jsonObject.getString(Alarm.PUB_DATE);
            int mOriginalMode = jsonObject.getInt(Alarm.ORIGINAL_MODE);
            int mCurrentMode = jsonObject.getInt(Alarm.SECONDARY_MODE);
            int mAlarmCount = jsonObject.getInt(Alarm.INTERNAL_ALARM_COUNT);
            long mNextAlarmInMilis = jsonObject.getLong(Alarm.NEXT_ALARM);

            Alarm jsonAlarm = new Alarm(mPubDate, Alarm.Mode.getMode(mOriginalMode), Alarm.Mode.getMode(mCurrentMode));
            jsonAlarm.alarmCount = mAlarmCount;
            jsonAlarm.nextAlarmInMilis = mNextAlarmInMilis;
            return jsonAlarm;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject toJSON(AnimeObject animeObject, boolean hummingbirdImageSource)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(AnimeObject.JSON_ID, animeObject.id);
            jsonObject.put(AnimeObject.JSON_STATUS, animeObject.status);
            jsonObject.put(AnimeObject.JSON_URL, animeObject.url);
            jsonObject.put(AnimeObject.JSON_TITLE, animeObject.title);
            jsonObject.put(AnimeObject.JSON_EPS_COUNT, animeObject.episodeCount);
            jsonObject.put(AnimeObject.JSON_COVER_IMG, animeObject.imageUrl);
            jsonObject.put(AnimeObject.JSON_SYNOPSIS, animeObject.synopsis);
            jsonObject.put(AnimeObject.JSON_STARTED_AIRING, animeObject.startedAiring);
            jsonObject.put(AnimeObject.JSON_FINISHED_AIRING, animeObject.finishedAiring);

            if (!hummingbirdImageSource)
                jsonObject.put(AnimeObject.JSON_NONAPI_CACHED_IMG_URI, animeObject.cachedImageURI);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        return jsonObject;
    }

    public static JSONObject toJSON(NyaaEntry entry)
    {
        JSONObject obj = new JSONObject();
        try
        {
            obj.put(NyaaEntry.Static.ID, entry.id);
            obj.put(NyaaEntry.Static.TITLE, entry.title);
            obj.put(NyaaEntry.Static.FANSUB, entry.fansub);

            obj.put(NyaaEntry.Static.TRUST, entry.trustCategory.name());
            if (entry.resolutionString == null)
                obj.put(NyaaEntry.Static.RESOLUTION, NyaaEntry.Static.UNDEFINED_STRING);
            else
                obj.put(NyaaEntry.Static.RESOLUTION, entry.resolutionString);
            if (entry.quality == null)
                obj.put(NyaaEntry.Static.QUALITY, NyaaEntry.Static.UNDEFINED_STRING);
            else
                obj.put(NyaaEntry.Static.QUALITY, entry.quality);
            obj.put(NyaaEntry.Static.CURRENT_EPISODE, entry.currentEpisode);
            obj.put(NyaaEntry.Static.CURRENT_EPISODE_STRING, entry.episodeString);
            obj.put(NyaaEntry.Static.PUBDATE, entry.pubDate);

        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        return obj;
    }

    public static JSONObject toJSON(Alarm alarm)
    {
        JSONObject obj = new JSONObject();
        try
        {
            if (alarm.originalMode != null)
                obj.put(Alarm.ORIGINAL_MODE, alarm.originalMode.ordinal());
            else
                obj.put(Alarm.ORIGINAL_MODE, Alarm.UNDEFINED_VALUE);

            if (alarm.secondaryMode != null)
                obj.put(Alarm.SECONDARY_MODE, alarm.secondaryMode.ordinal());
            else
                obj.put(Alarm.SECONDARY_MODE, Alarm.UNDEFINED_VALUE);

            obj.put(Alarm.INTERNAL_ALARM_COUNT, alarm.alarmCount);
            obj.put(Alarm.NEXT_ALARM, alarm.nextAlarmInMilis);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return null;
        }
        return obj;
    }

    public static JSONArray toJSONArray(List<AnimeObject> animeObjects, boolean hummingbirdImageSource)
    {
        JSONArray results = new JSONArray();
        if (animeObjects != null && animeObjects.size() > 0)
        {
            for (AnimeObject animeObject : animeObjects)
            {
                results.put(toJSON(animeObject, hummingbirdImageSource));

            }
        }
        return results;
    }

}
