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

package com.nizlumina.syncmaru.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.nizlumina.syncmaru.model.CompositeData;

import java.util.List;

public class CompositeDataJSONFactory
{
    public static String toJSON(CompositeData compositeData)
    {
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        return gson.toJson(compositeData);
    }

    public static String listToJSON(List<CompositeData> compositeDatas)
    {
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        return gson.toJson(compositeDatas, new TypeToken<List<CompositeData>>() {}.getType());
    }

    public static CompositeData fromJSON(String jsonString)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(jsonString, CompositeData.class);
    }

    public static List<CompositeData> listFromJSON(String jsonString)
    {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.fromJson(jsonString, new TypeToken<List<CompositeData>>() {}.getType());
    }
}
