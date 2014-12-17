package com.nizlumina.minori.android.data.alarm;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


public class AlarmReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent alarmIntent)
    {
        Log.v("AlarmReceiver.OnReceive()", "Alarm started!");

        if (alarmIntent.getAction() != null && alarmIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            //Watchlist.getInstance().recreateAlarmsOnBoot();
            return;
        }

        //Global.incrementScanCount();
        //alarmIntent.setClass(context, ScanService.class);
        startWakefulService(context, alarmIntent);
    }
}

