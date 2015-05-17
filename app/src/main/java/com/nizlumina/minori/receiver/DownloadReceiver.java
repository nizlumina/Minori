package com.nizlumina.minori.receiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.nizlumina.minori.service.DownloadService;

public class DownloadReceiver extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        intent.setClass(context, DownloadService.class);
        startWakefulService(context, intent);
    }
}
