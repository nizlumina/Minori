package com.nizlumina.minori.android.data.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nizlumina.minori.android.data.WatchData;
import com.nizlumina.minori.android.service.ScanService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Handles creation of Alarm for AlarmManager and cancelling them
 * <l>
 * 1. Create a single AlarmFactory instance
 * 2. Use createAlarm/cancelAlarm as many as necessary
 * 3. Call release() when finished.
 * </l>
 */
public final class AlarmFactory
{
    public static void createAlarm(Context context, WatchData watchData, long timeInMilis)
    {
        try
        {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, watchData.getId(), new Intent(context, AlarmReceiver.class).putExtra(ScanService.IKEY_SCAN_INT, watchData.getId()), PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMilis, pendingIntent);
            Date date = new Date(timeInMilis);
            Log.v("AlarmFactory", "Alarm created at: " + date.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void cancelAlarm(Context context, WatchData watchData)
    {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, watchData.getNyaaEntry().id, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public static boolean skipWeek(Context context, WatchData watchData)
    {
        Alarm currData = watchData.getAlarm();
        if (currData != null)
        {
            if (currData.nextAlarmInMilis > 0 && currData.previousPubDate != null)
            {
                GregorianCalendar pubDateInstanceCal = new GregorianCalendar();
                pubDateInstanceCal.setTime(currData.previousPubDate);

                GregorianCalendar alarmInstanceCal = new GregorianCalendar();
                alarmInstanceCal.setTimeInMillis(currData.nextAlarmInMilis);

                int weekOffset = Math.abs(alarmInstanceCal.get(Calendar.WEEK_OF_YEAR) - pubDateInstanceCal.get(Calendar.WEEK_OF_YEAR));
                pubDateInstanceCal.add(Calendar.WEEK_OF_YEAR, weekOffset);
                currData.alarmCount = 0; //IMPORTANT
                currData.initModes(currData.originalMode, null);
                currData.getNextDate(pubDateInstanceCal);

                createAlarm(context, watchData, currData.nextAlarmInMilis);
                return true;
            }
        }
        return false;
    }

    public static void resetAlarm(Context context, WatchData watchData, Alarm.Mode optionalNewMode) //Set mode to null if resetting with original mode
    {
        Alarm currData = watchData.getAlarm();
        if (currData != null)
        {
            if (optionalNewMode != null)
            {
                currData.initModes(optionalNewMode, null);
            }
            else
            {
                currData.initModes(currData.originalMode, null);
            }

            currData.alarmCount = 0;
            currData.nextAlarmInMilis = 0;
            currData.getNextDate();

            createAlarm(context, watchData, currData.nextAlarmInMilis);
        }
    }
}