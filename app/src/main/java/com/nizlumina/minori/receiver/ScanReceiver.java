package com.nizlumina.minori.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.nizlumina.minori.service.ScanService;

public class ScanReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        intent.setClass(context, ScanService.class);
        startWakefulService(context, intent);
    }
}
