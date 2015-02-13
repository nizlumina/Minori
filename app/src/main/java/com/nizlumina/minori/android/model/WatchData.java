package com.nizlumina.minori.android.model;

import android.content.Context;

import com.nizlumina.minori.android.alarm.Alarm;
import com.nizlumina.minori.android.alarm.AlarmFactory;
import com.nizlumina.minori.core.Hummingbird.AnimeObject;
import com.nizlumina.minori.core.Nyaa.NyaaEntry;

/**
 * Base Minori data. Access this class to do stuffs.
 */
public class WatchData
{
    private int id;
    private NyaaEntry nyaaEntry;
    private Alarm alarm;
    private AnimeObject animeObject;

    public WatchData() {}

    public WatchData(int id, NyaaEntry nyaaEntry, Alarm alarm, AnimeObject animeObject)
    {
        this.id = id;
        this.nyaaEntry = nyaaEntry;
        this.alarm = alarm;
        this.animeObject = animeObject;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public NyaaEntry getNyaaEntry()
    {
        return nyaaEntry;
    }

    public void setNyaaEntry(NyaaEntry nyaaEntry)
    {
        this.nyaaEntry = nyaaEntry;
    }

    public Alarm getAlarm()
    {
        return alarm;
    }

    public void setAlarm(Alarm alarm)
    {
        this.alarm = alarm;
    }

    public AnimeObject getAnimeObject()
    {
        return animeObject;
    }

    public void setAnimeObject(AnimeObject animeObject)
    {
        this.animeObject = animeObject;
    }

    public void updateNyaaEntry(Context context, NyaaEntry latestNyaaEntry)
    {
        if (latestNyaaEntry != null)
        {
            this.alarm = new Alarm(latestNyaaEntry.getPubDate(), this.alarm.originalMode, null);
            this.updateAlarm(context);
        }
    }

    public void updateAlarm(Context context)
    {
        long nextDate = this.alarm.getNextDate();
        if (nextDate > 0)
            AlarmFactory.createAlarm(context, this, nextDate);
    }

}
