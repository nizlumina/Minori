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

package com.nizlumina.minori.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * An intent service for Disk IO tasks. Use the helper static method {@link #executeRead(Context, int, File, boolean)} and {@link #executeWrite(Context, int, File, String, boolean)} for starting the service. Extend/implement {@link DiskIOBroadcastReceiver} to receive broadcast (and results) from the service.
 */
public class DiskIOService extends IntentService
{

    private static final String CLASS_NAME = DiskIOService.class.getName();
    private static final String ACTION_WRITE = CLASS_NAME + "$write";
    private static final String ACTION_READ = CLASS_NAME + "$read";
    //    private static final String IKEY_PATH = CLASS_NAME + "$path";
    private static final String IKEY_BOOL_LOCALBROADCAST = CLASS_NAME + "$use_localbroadcast";
    //    private static final String IKEY_PAYLOAD = CLASS_NAME + "$payload";
    private static final String IKEY_REQ_ID = CLASS_NAME + "$reqID";
    private static final String PARCELKEY_DISKTASK = CLASS_NAME + "$parcel_disktask";

    /**
     * {@inheritDoc}
     */
    public DiskIOService()
    {
        super("DiskIOService");
    }

    /**
     * Starts an auto wakelock-ed (and de-wakelock-ed) DiskIOService for writing the file to the disk.
     *
     * @param context           Context where this call originated.
     * @param requestID         Request ID. Also used for receiving results.
     * @param outFile           The file to be written. If it doesn't exist, the file will be created automatically. If it does, it will be overwritten (WARNING: Not appended, hence this is a destructive write).
     * @param payload           The string data to be written
     * @param useLocalBroadcast Only set this to true if {@link LocalBroadcastManager} is used to register the {@link DiskIOBroadcastReceiver} w.r.t. the request
     */
    public static void executeWrite(@NonNull Context context, int requestID, @NonNull File outFile, @NonNull String payload, boolean useLocalBroadcast)
    {
        String filePath = outFile.getPath();
        DiskTask diskTask = new DiskTask(requestID, filePath, payload, useLocalBroadcast);
        Intent intent = new Intent(ACTION_WRITE).putExtra(PARCELKEY_DISKTASK, diskTask);

        WakefulBroadcastReceiver.startWakefulService(context, intent);
    }

    /**
     * Starts n auto wakelock-ed (and de-wakelock-ed) DiskIOService for reading the inputfile.
     *
     * @param context           Context where this call originated.
     * @param requestID         Request ID. Also used for receiving results.
     * @param inputFile         The file to be read. The file must exist, be a file, and can be read.
     * @param useLocalBroadcast Only set this to true if {@link LocalBroadcastManager} is used to register the {@link DiskIOBroadcastReceiver} (the result is marshalled thru this receiver)
     */
    public static void executeRead(Context context, int requestID, File inputFile, boolean useLocalBroadcast)
    {
        if (inputFile.exists() && inputFile.isFile() && inputFile.canRead())
        {
            String filePath = inputFile.getPath();
            DiskTask diskTask = new DiskTask(requestID, filePath, null, useLocalBroadcast);
            Intent intent = new Intent(ACTION_READ).putExtra(PARCELKEY_DISKTASK, diskTask);
            WakefulBroadcastReceiver.startWakefulService(context, intent);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            String action = intent.getAction();
            if (action != null)
            {
                DiskTask diskTask = intent.getParcelableExtra(PARCELKEY_DISKTASK);
                if (diskTask != null)
                {
                    if (action.equals(ACTION_WRITE))
                    {
                        try
                        {
                            IOUtils.write(diskTask.getPayload(), new BufferedOutputStream(new FileOutputStream(diskTask.getPath(), false)), Charset.defaultCharset());
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if (action.equals(ACTION_READ))
                    {
                        String outString = null;
                        try
                        {
                            outString = IOUtils.toString(new BufferedInputStream(new FileInputStream(diskTask.getPath())), Charset.defaultCharset());
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                        if (outString != null)
                        {
                            diskTask.setPayload(outString);
                        }
                    }

                    Intent outIntent = new Intent(getApplicationContext(), DiskIOBroadcastReceiver.class);
                    outIntent.putExtra(IKEY_REQ_ID, diskTask.getRequestID()).putExtra(PARCELKEY_DISKTASK, diskTask);

                    if (intent.getBooleanExtra(IKEY_BOOL_LOCALBROADCAST, false))
                    {
                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(outIntent);
                    }
                    else
                    {
                        getApplicationContext().sendBroadcast(outIntent);
                    }
                }
            }
            WakefulBroadcastReceiver.completeWakefulIntent(intent);
        }
    }

    public static class DiskTask implements Parcelable
    {
        public static final Parcelable.Creator<DiskTask> CREATOR = new Parcelable.Creator<DiskTask>()
        {
            public DiskTask createFromParcel(Parcel source) {return new DiskTask(source);}

            public DiskTask[] newArray(int size) {return new DiskTask[size];}
        };
        final int requestID;
        final String path;
        final boolean useLocalBroadcast;
        String payload; //null on input for read. nonnull for write input.

        public DiskTask(int requestID, String path, String payload, boolean useLocalBroadcast)
        {
            this.requestID = requestID;
            this.path = path;
            this.payload = payload;
            this.useLocalBroadcast = useLocalBroadcast;
        }

        protected DiskTask(Parcel in)
        {
            this.requestID = in.readInt();
            this.path = in.readString();
            this.useLocalBroadcast = in.readByte() != 0;
            this.payload = in.readString();
        }

        public int getRequestID()
        {
            return requestID;
        }

        public String getPath()
        {
            return path;
        }

        public String getPayload()
        {
            return payload;
        }

        public void setPayload(String payload)
        {
            this.payload = payload;
        }

        public boolean isUseLocalBroadcast()
        {
            return useLocalBroadcast;
        }

        @Override
        public int describeContents() { return 0; }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            dest.writeInt(this.requestID);
            dest.writeString(this.path);
            dest.writeByte(useLocalBroadcast ? (byte) 1 : (byte) 0);
            dest.writeString(this.payload);
        }
    }

    /**
     * A broadcast receiver for listening events from the DiskIOService. If you override {@link #onReceive(Context, Intent)}, make sure to call super.onReceive(context, intent);
     */
    public abstract class DiskIOBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent != null)
            {
                String action = intent.getAction();
                if (action != null)
                {
                    DiskTask diskTask = intent.getParcelableExtra(PARCELKEY_DISKTASK);
                    if (action.equals(ACTION_WRITE))
                    {
                        onWriteCompleted(diskTask.getRequestID());
                    }
                    if (action.equals(ACTION_READ))
                    {
                        onReadCompleted(diskTask.getRequestID(), diskTask.getPayload());
                    }
                }
            }
        }

        /**
         * Called when the service completed the request for reading the file associated with the request ID.
         *
         * @param requestID Original id for the request
         * @param data      The data read from the file (default encoding is UTF-8) in String object.
         */
        abstract void onReadCompleted(int requestID, String data);

        /**
         * Called when the service completed writing the file assocaited with the request ID.
         *
         * @param requestID Original id for the request
         */
        abstract void onWriteCompleted(int requestID);

        //todo implement error event reports
    }
}
