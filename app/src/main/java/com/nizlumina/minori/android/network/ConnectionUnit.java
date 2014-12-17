package com.nizlumina.minori.android.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A basic connection unit
 */
public class ConnectionUnit
{

    public static final int CONNECT_TIMEOUT = 40000;
    public static final int READ_TIMEOUT = 50000;
    final String requestMethodGET = "GET";

    public String getResponseString(final String url)
    {
        return getResponseString(url, null);
    }

    public String getResponseString(final String url, NetworkProgressListener listener)
    {
        HttpURLConnection connection = null;
        String stringResponse = null;
        try
        {
            connection = (HttpURLConnection) (new URL(url).openConnection());
            connection.setRequestMethod(requestMethodGET);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();

            if (listener != null)
                listener.onProgressUpdate(NetworkProgressListener.ConnectionState.CONN_OPENED);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                if (listener != null)
                    listener.onProgressUpdate(NetworkProgressListener.ConnectionState.RESPONSE_RETRIEVED);

                StringBuilder stringBuilder = new StringBuilder();

                char[] buffer = new char[8192];
                int read;
                while ((read = rd.read(buffer, 0, buffer.length)) > 0)
                {
                    stringBuilder.append(buffer, 0, read);
                }
                stringResponse = stringBuilder.toString();
                rd.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (connection != null) connection.disconnect();

            if (listener != null)
            {
                listener.onProgressUpdate(NetworkProgressListener.ConnectionState.CONNECTION_CLOSED);
                listener.onNetworkFinished();
            }
        }
        return stringResponse;
    }

    public void invokeOnStream(final String url, Callable callable)
    {
        invokeOnStream(url, callable, null);
    }

    /**
     * Pass url, and pass a new Callable interface to do stuffs with the inputStream.
     * It will automatically disconnect the socket upon exit.
     *
     * @param url      The HTTP url set. Requested with with the GET method.
     * @param callable Pass the method to do stuffs with it in onStreamReceived()
     * @param listener Optional listener that respond to ConnectionState updates
     */
    public void invokeOnStream(final String url, Callable callable, NetworkProgressListener listener)
    {
        HttpURLConnection connection = null;
        try
        {
            connection = (HttpURLConnection) (new URL(url).openConnection());
            connection.setRequestMethod(requestMethodGET);
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.connect();

            if (listener != null)
                listener.onProgressUpdate(NetworkProgressListener.ConnectionState.CONN_OPENED);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                InputStream streamResponse = connection.getInputStream();

                if (streamResponse != null) callable.onStreamReceived(streamResponse);

                if (listener != null)
                    listener.onProgressUpdate(NetworkProgressListener.ConnectionState.RESPONSE_RETRIEVED);
            }


        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (connection != null) connection.disconnect();
            if (listener != null)
            {
                listener.onProgressUpdate(NetworkProgressListener.ConnectionState.CONNECTION_CLOSED);
                listener.onNetworkFinished();
            }

        }
    }

    public interface Callable
    {
        public void onStreamReceived(InputStream inputStream);
    }

    public static interface NetworkProgressListener
    {
        void onProgressUpdate(ConnectionState state);

        void onNetworkFinished();

        public enum ConnectionState
        {
            CONN_OPENED, RESPONSE_OK, RESPONSE_RETRIEVED, CONNECTION_CLOSED
        }
    }
}
