package com.nizlumina.minori.model.alarm;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Alarm data class.
 * Do note getNextDate() must be called explicitly after a creation of new Alarm to set the proper alarmCount and nextAlarmInMilis.
 */
public class Alarm
{
    public static final String SECONDARY_MODE = "seco_mode";
    public static final String ORIGINAL_MODE = "orig_mode";
    public static final String INTERNAL_ALARM_COUNT = "int_alarm_count";
    public static final String NEXT_ALARM = "next_alarm";
    public static final String PUB_DATE = "alarm_pub_date";

    public static final int UNDEFINED_VALUE = -1;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a, EEE d MMM");
    private static final SimpleDateFormat pubDateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    public int alarmCount = 0; //CAREFUL WHEN CHANGING THIS
    public long nextAlarmInMilis = 0;
    public Mode secondaryMode = null;
    public Mode originalMode = null;
    public Date previousPubDate;
    int cachedAlarm;
    private boolean protectAlarm = false;

    //Creation, saving and loading
    public Alarm(@NonNull String mPreviousPubDateString, Mode mOriginalMode, Mode mSecondaryMode)
    {
        try
        {
            this.previousPubDate = pubDateFormatter.parse(mPreviousPubDateString);
        }
        catch (Exception e)
        {
            this.previousPubDate = new Date(System.currentTimeMillis());
            e.printStackTrace();
        }

        initModes(mOriginalMode, mSecondaryMode);
    }

    public Alarm(Date mPreviousPubDate, Mode mOriginalMode, Mode mSecondaryMode)
    {
        this.previousPubDate = mPreviousPubDate;
        initModes(mOriginalMode, mSecondaryMode);
    }


    public final void initModes(Mode mOriginalMode, Mode mSecondaryMode)
    {
        this.originalMode = mOriginalMode;
        if (mOriginalMode.equals(Mode.RELEASE_DAY) && mSecondaryMode == null)
        {
            this.secondaryMode = Mode.RELEASE_DAY;
        }
        else if (mOriginalMode.equals(Mode.IRREGULAR) && mSecondaryMode == null)
        {
            this.secondaryMode = Mode.IRREGULAR;
        }
        else
            this.secondaryMode = mSecondaryMode;
    }

    //Call this if you do not want the internal alarm to change. Used during a manual scan.
    public final void raiseProtectAlarmCountFlag()
    {
        protectAlarm = true;
    }

    //GetNextDate will return -1 on failure
    public final long getNextDate()
    {
        return getNextDate(null);
    }

    public final long getNextDate(final GregorianCalendar startingCal) // pubDate can only be read. It is only set in a new Alarm();
    {
        try
        {
            final GregorianCalendar currCal = new GregorianCalendar();
            currCal.setTimeInMillis(System.currentTimeMillis());

            final GregorianCalendar pubDateInstanceCal;

            if (startingCal == null)
            {
                pubDateInstanceCal = new GregorianCalendar();
                pubDateInstanceCal.setTime(previousPubDate);
            }
            else
            {
                pubDateInstanceCal = startingCal;
            }

            pubDateInstanceCal.set(Calendar.SECOND, 0);
            pubDateInstanceCal.set(Calendar.MILLISECOND, 0);

            //This is a temporary fix for a weird alarm bug that cannot find a next date even after firing.
            //Currently believe it is due to protect alarm boolean still being there in memory
            //Check prev set alarm
            if (nextAlarmInMilis > 0)
            {
                final GregorianCalendar previouslySetAlarm = new GregorianCalendar();
                previouslySetAlarm.setTimeInMillis(nextAlarmInMilis);

                if (currCal.after(previouslySetAlarm)) protectAlarm = false;
            }


            if (originalMode.equals(Mode.RELEASE_DAY))
            {
                //initial offset
                pubDateInstanceCal.add(Calendar.WEEK_OF_MONTH, 1);
                pubDateInstanceCal.add(Calendar.MINUTE, -30);
            }
            else if (originalMode.equals(Mode.IRREGULAR))
                pubDateInstanceCal.add(Calendar.DAY_OF_WEEK, 4);


            final Date returnDate = getDate(alarmCount, currCal, pubDateInstanceCal);

            if (protectAlarm)
            {
                this.protectAlarm = false; //reset flag
                return -1; //so that updateLatest() is prevented from creating alarm if entry is scanned BEFORE its next alarm is fired. Alarm for new entry wont be affected since it is init with protect alarm = false
            }
            else
            {
                this.alarmCount = cachedAlarm; //INFO: alarmCount will only be 0 in a new Alarm()
                this.nextAlarmInMilis = returnDate.getTime();
                return nextAlarmInMilis;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    private Date getDate(int i, GregorianCalendar currCal, GregorianCalendar alarmCal) throws IllegalArgumentException
    {
        if (originalMode.equals(Mode.RELEASE_DAY))
        {
            if (secondaryMode.equals(Mode.RELEASE_DAY))
            {
                while (alarmCal.before(currCal))
                {
                    //here
                    if (i < 9) //for 1hr and 30 mins for each 10 mins
                    {
                        alarmCal.add(Calendar.MINUTE, 10);
                        i++;
                        continue;
                    }
                    if (i < 18) //for 3 hour after for each 20 mins
                    {
                        alarmCal.add(Calendar.MINUTE, 20);
                        i++;
                        continue;
                    }
                    if (i < 38) //for 20 hour after for each 30 mins
                    {
                        alarmCal.add(Calendar.MINUTE, 30);
                        i++;
                        continue;
                    }
                    if (i >= 38)
                    {
                        secondaryMode = Mode.DAILY;
                        i = 0;
                        break;
                    }
                }
            }
            if (secondaryMode.equals(Mode.DAILY))
            {
                alarmCal.set(Calendar.MINUTE, 0);
                alarmCal.set(Calendar.HOUR, 0);
                while (alarmCal.before(currCal))
                {
                    alarmCal.add(Calendar.HOUR, 2);
                }
            }
        }
        else if (originalMode.equals(Mode.IRREGULAR))
        {
            boolean offsetWeek = false;

            if (secondaryMode.equals(Mode.IRREGULAR))
            {
                while (alarmCal.before(currCal))
                {
                    if (i < 72)
                    {
                        alarmCal.add(Calendar.HOUR, 2);
                        i++;
                    }
                    else
                    {
                        secondaryMode = Mode.RELEASE_DAY;
                        break;
                    }
                }
            }
            if (secondaryMode.equals(Mode.RELEASE_DAY))
            {
                while (alarmCal.before(currCal))
                {
                    int offsetCount = i - 72;

                    if (!offsetWeek)
                    {
                        alarmCal.add(Calendar.DAY_OF_WEEK, 3);// we might not need this!
                        offsetWeek = true;
                    }

                    //here
                    if (offsetCount < 9) //for 1hr and 30 mins for each 10 mins
                    {
                        alarmCal.add(Calendar.MINUTE, 10);
                        i++;
                        continue;
                    }
                    if (offsetCount < 18) //for 3 hour after for each 20 mins
                    {
                        alarmCal.add(Calendar.MINUTE, 20);
                        i++;
                        continue;
                    }
                    if (offsetCount < 38) //for 20 hour after for each 30 mins
                    {
                        alarmCal.add(Calendar.MINUTE, 30);
                        i++;
                        continue;
                    }
                    if (offsetCount >= 38)
                    {
                        secondaryMode = Mode.DAILY;
                        i = 0;
                        break;
                    }
                }
            }
            if (secondaryMode.equals(Mode.DAILY))
            {
                alarmCal.set(Calendar.MINUTE, 0);
                alarmCal.set(Calendar.HOUR, 0);
                while (alarmCal.before(currCal))
                {
                    alarmCal.add(Calendar.HOUR, 2);
                }
            }
        }
        else if (originalMode.equals(Mode.DAILY))
        {
            alarmCal.set(Calendar.MINUTE, 0);
            alarmCal.set(Calendar.HOUR, 0);
            while (alarmCal.before(currCal))
            {
                alarmCal.add(Calendar.HOUR, 2);
            }
        }

        cachedAlarm = i;
        return alarmCal.getTime();
    }

    //For logging
    public final String stringData()
    {
        if (previousPubDate != null)
        {
            Date debugNextAlarm = new Date(nextAlarmInMilis);
            return "[NEXTALARM: " + debugNextAlarm.toString() + " while previous is: " + previousPubDate.toString() + "]";
        }
        return "";
    }

    //For displays
    public final String getNextAlarmDisplayString()
    {
        if (nextAlarmInMilis != 0)
        {
            return displayFormat.format(new Date(nextAlarmInMilis));
        }
        return null;
    }

    public final String getPubDateDisplayString()
    {
        if (previousPubDate != null)
        {
            return displayFormat.format(previousPubDate);
        }
        return null;
    }

    public enum Mode
    {
        RELEASE_DAY,
        IRREGULAR,
        DAILY;

        public static Mode getMode(int i)
        {
            if (i == RELEASE_DAY.ordinal()) return RELEASE_DAY;
            if (i == IRREGULAR.ordinal()) return IRREGULAR;
            if (i == DAILY.ordinal()) return DAILY;
            return null;
        }

        public static String getDisplayName(Mode mode)
        {
            if (mode != null)
            {
                switch (mode)
                {
                    case RELEASE_DAY:
                        return "Release Day";
                    case IRREGULAR:
                        return "Irregular";
                    case DAILY:
                        return "Cat-like";
                }
            }
            return "";
        }

    }
}
