package com.nizlumina.minori.internal.factory;

import com.nizlumina.common.hummingbird.AnimeObject;
import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.minori.model.alarm.Alarm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Static factory for saving and loading Core data.<br/>
 * JSONObject methods is returned as an empty object upon exception.<br/>
 * JSONArray and other collections methods omitted failures and returns data that were only successful.<br/><br/>
 * NyaaEntry and AnimeObject methods returns NULL on ANY failure due to the higher integrity needed (do null check if you call their methods).
 */
public class CoreJSONFactory
{
    public static NyaaEntry nyaaEntryFromJSON(JSONObject jsonObject)
    {
        NyaaEntry entry = new NyaaEntry();
        try
        {
            entry.setId(jsonObject.getInt(NyaaEntry.Static.ID));
            entry.setTitle(jsonObject.getString(NyaaEntry.Static.TITLE));
            entry.setFansub(jsonObject.getString(NyaaEntry.Static.FANSUB));

            String mTrustCat = jsonObject.getString(NyaaEntry.Static.TRUST);
            if (mTrustCat.equalsIgnoreCase(NyaaEntry.Trust.APLUS.name()))
                entry.setTrustCategory(NyaaEntry.Trust.APLUS);
            else if (mTrustCat.equalsIgnoreCase(NyaaEntry.Trust.TRUSTED.name()))
                entry.setTrustCategory(NyaaEntry.Trust.TRUSTED);
            else if (mTrustCat.equalsIgnoreCase(NyaaEntry.Trust.REMAKES.name()))
                entry.setTrustCategory(NyaaEntry.Trust.REMAKES);
            else entry.setTrustCategory(NyaaEntry.Trust.ALL);

            String mResolution = jsonObject.getString(NyaaEntry.Static.RESOLUTION);
            if (mResolution == null)
            {
                entry.setResolutionString(null);
            }
            else
            {
                entry.setResolutionString(mResolution);
                entry.setResolution(NyaaEntry.Resolution.matchResolution(mResolution));
            }

            String mQuality = jsonObject.getString(NyaaEntry.Static.QUALITY);
            if (mQuality.equals(NyaaEntry.Static.UNDEFINED_STRING))
                entry.setQuality(null);
            else
                entry.setQuality(mQuality);

            entry.setCurrentEpisode(jsonObject.getInt(NyaaEntry.Static.CURRENT_EPISODE));
            entry.setEpisodeString(jsonObject.getString(NyaaEntry.Static.CURRENT_EPISODE_STRING));
            entry.setPubDate(jsonObject.getString(NyaaEntry.Static.PUBDATE));

            return entry;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;

    }

    public static AnimeObject animeObjectFromJSON(JSONObject jsonObject, boolean strictHummingbirdJSON)
    {
        if (jsonObject == null) return null;
        AnimeObject animeObject = new AnimeObject();
        animeObject.setId(jsonObject.optInt(AnimeObject.JSON_API_ID));
        animeObject.setSlug(jsonObject.optString(AnimeObject.JSON_API_SLUG));
        animeObject.setStatus(jsonObject.optString(AnimeObject.JSON_API_STATUS));
        animeObject.setUrl(jsonObject.optString(AnimeObject.JSON_API_URL));
        animeObject.setTitle(jsonObject.optString(AnimeObject.JSON_API_TITLE));
        animeObject.setEpisodeCount(jsonObject.optInt(AnimeObject.JSON_API_EPS_COUNT));
        animeObject.setImageUrl(jsonObject.optString(AnimeObject.JSON_API_COVER_IMG_URL));
        animeObject.setSynopsis(jsonObject.optString(AnimeObject.JSON_API_SYNOPSIS));
        animeObject.setStartedAiring(jsonObject.optString(AnimeObject.JSON_API_STARTED_AIRING));
        animeObject.setFinishedAiring(jsonObject.optString(AnimeObject.JSON_API_FINISHED_AIRING));

        if (!strictHummingbirdJSON)
            animeObject.setCachedImageURI(jsonObject.optString(AnimeObject.JSON_CACHED_IMG_URI));
        return animeObject;

    }

    public static List<AnimeObject> animeObjectsFromJSON(JSONArray jsonArray, boolean strictHummingbirdJSON)
    {
        List<AnimeObject> results = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++)
        {
            try
            {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null)
                {
                    AnimeObject animeObject = animeObjectFromJSON(jsonObject, strictHummingbirdJSON);
                    if (animeObject != null)
                        results.add(animeObject);
                }
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

    public static JSONObject toJSON(AnimeObject animeObject, boolean strictHummingbirdJSON)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.putOpt(AnimeObject.JSON_API_ID, animeObject.getId())
                    .putOpt(AnimeObject.JSON_API_SLUG, animeObject.getSlug())
                    .putOpt(AnimeObject.JSON_API_STATUS, animeObject.getStatus())
                    .putOpt(AnimeObject.JSON_API_URL, animeObject.getUrl())
                    .putOpt(AnimeObject.JSON_API_TITLE, animeObject.getTitle())
                    .putOpt(AnimeObject.JSON_API_EPS_COUNT, animeObject.getEpisodeCount())
                    .putOpt(AnimeObject.JSON_API_COVER_IMG_URL, animeObject.getImageUrl())
                    .putOpt(AnimeObject.JSON_API_SYNOPSIS, animeObject.getSynopsis())
                    .putOpt(AnimeObject.JSON_API_STARTED_AIRING, animeObject.getStartedAiring())
                    .putOpt(AnimeObject.JSON_API_FINISHED_AIRING, animeObject.getFinishedAiring());

            if (!strictHummingbirdJSON)
                jsonObject.put(AnimeObject.JSON_CACHED_IMG_URI, animeObject.getCachedImageURI());
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
            obj.put(NyaaEntry.Static.ID, entry.getId());
            obj.put(NyaaEntry.Static.TITLE, entry.getTitle());
            obj.put(NyaaEntry.Static.FANSUB, entry.getFansub());

            obj.put(NyaaEntry.Static.TRUST, entry.getTrustCategory().name());
            if (entry.getResolutionString() == null)
                obj.put(NyaaEntry.Static.RESOLUTION, NyaaEntry.Static.UNDEFINED_STRING);
            else
                obj.put(NyaaEntry.Static.RESOLUTION, entry.getResolutionString());
            if (entry.getQuality() == null)
                obj.put(NyaaEntry.Static.QUALITY, NyaaEntry.Static.UNDEFINED_STRING);
            else
                obj.put(NyaaEntry.Static.QUALITY, entry.getQuality());
            obj.put(NyaaEntry.Static.CURRENT_EPISODE, entry.getCurrentEpisode());
            obj.put(NyaaEntry.Static.CURRENT_EPISODE_STRING, entry.getEpisodeString());
            obj.put(NyaaEntry.Static.PUBDATE, entry.getPubDate());

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

    public static JSONArray toJSONArray(List<AnimeObject> animeObjects, boolean strictHummingbirdJSON)
    {
        JSONArray results = new JSONArray();
        if (animeObjects != null && animeObjects.size() > 0)
        {
            for (AnimeObject animeObject : animeObjects)
            {
                results.put(toJSON(animeObject, strictHummingbirdJSON));

            }
        }
        return results;
    }

}
