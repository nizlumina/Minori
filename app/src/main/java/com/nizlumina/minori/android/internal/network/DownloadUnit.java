package com.nizlumina.minori.android.internal.network;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A simple download unit to be for IPC
 */
public class DownloadUnit implements Parcelable
{
    public static final Creator<DownloadUnit> CREATOR = new Parcelable.Creator<DownloadUnit>()
    {
        @Override
        public DownloadUnit createFromParcel(Parcel source)
        {
            return new DownloadUnit(source);
        }

        @Override
        public DownloadUnit[] newArray(int size)
        {
            return new DownloadUnit[size];
        }
    };
    int id;
    String url;
    String outputName;
    String notificationTitle;

    public DownloadUnit() {}

    public DownloadUnit(int id, String url, String outputName, String notificationTitle)
    {
        this.id = id;
        this.url = url;
        this.outputName = outputName;
        this.notificationTitle = notificationTitle;
    }

    public DownloadUnit(Parcel parcel)
    {
        this.id = parcel.readInt();
        this.url = parcel.readString();
        this.outputName = parcel.readString();
        this.notificationTitle = parcel.readString();
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getOutputName()
    {
        return outputName;
    }

    public void setOutputName(String outputName)
    {
        this.outputName = outputName;
    }

    public String getNotificationTitle()
    {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle)
    {
        this.notificationTitle = notificationTitle;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags)
    {
        destination.writeInt(id);
        destination.writeString(url);
        destination.writeString(outputName);
        destination.writeString(notificationTitle);
    }
}
