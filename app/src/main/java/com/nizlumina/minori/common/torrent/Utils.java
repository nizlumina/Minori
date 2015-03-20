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

package com.nizlumina.minori.common.torrent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A short util class to be used by the TorrentEngine
 */
public class Utils
{
    /**
     * Save serializable to disk
     *
     * @param objectToBeWritten Object of type T. The base object must implements {@link java.io.Serializable}
     */
    public static <T> boolean saveSerializable(final String fileName, final File targetDirectory, T objectToBeWritten)
    {
        boolean success = false;
        final File targetFile = new File(targetDirectory, fileName);

        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try
        {
            fileOutputStream = new FileOutputStream(targetFile);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(objectToBeWritten);
            success = true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fileOutputStream != null)
            {
                try
                {

                    fileOutputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            if (objectOutputStream != null)
            {
                try
                {
                    objectOutputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    /**
     * Load serializable from disk
     *
     * @return T object of type T which base object implements {@link java.io.Serializable}. Null on failure or if file doesn't exist.
     */
    public static <T> T loadSerializable(final String fileName, final File targetDirectory)
    {
        T result = null;

        final File metafileIndex = new File(targetDirectory, fileName);
        if (metafileIndex.exists())
        {
            FileInputStream fileInputStream = null;
            ObjectInputStream objectInputStream = null;
            try
            {
                fileInputStream = new FileInputStream(metafileIndex);
                objectInputStream = new ObjectInputStream(fileInputStream);
                //noinspection unchecked
                result = (T) objectInputStream.readObject();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (fileInputStream != null)
                {
                    try
                    {
                        fileInputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                if (objectInputStream != null)
                {
                    try
                    {
                        objectInputStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }


}
