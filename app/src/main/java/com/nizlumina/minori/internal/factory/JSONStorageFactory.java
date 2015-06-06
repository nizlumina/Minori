package com.nizlumina.minori.internal.factory;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Accessed for saving and loading
 */
public class JSONStorageFactory
{
    public static void saveJSONArray(final JSONArray jsonArray, final FileOutputStream fileOutputStream)
    {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
        try
        {
            writer.write(jsonArray.toString(4));
        }
        catch (IOException | JSONException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Try loading a JSONArray from the given FileInputstream. Returns an empty JSON array on failure.
     *
     * @param fileInputStream The stream to be read
     * @return
     */
    public static JSONArray loadJSONArray(final FileInputStream fileInputStream)
    {
        JSONArray result = new JSONArray();
        try
        {
            String jsonString = getStringFromJSON(fileInputStream, Charset.forName("UTF-8"));

            if (jsonString != null) result = new JSONArray(jsonString);
        }
        catch (JSONException | IOException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    private static String getStringFromJSON(final FileInputStream fileInputStream, final Charset encoding) throws IOException
    {
        try
        {
            Reader rd = new BufferedReader(new InputStreamReader(fileInputStream, encoding));
            StringBuilder builder = new StringBuilder();

            char[] buffer = new char[8192];
            int read;
            while ((read = rd.read(buffer, 0, buffer.length)) > 0)
            {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        }
        finally
        {
            fileInputStream.close();
        }
    }
}
