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

package com.nizlumina.minori.utility;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

/**
 * Short implmentation of a Parcelable Sparse Boolean array with minor changes.
 *
 * @see <a href="http://stackoverflow.com/a/16711258/4969696" /> for extra details.
 */
public class SparseBooleanArrayParcelable extends SparseBooleanArray implements Parcelable
{
    public static final Parcelable.Creator<SparseBooleanArrayParcelable> CREATOR = new Parcelable.Creator<SparseBooleanArrayParcelable>()
    {
        public SparseBooleanArrayParcelable createFromParcel(Parcel source)
        {
            int size = source.readInt();
            SparseBooleanArrayParcelable out = new SparseBooleanArrayParcelable(size);

            int[] keys = new int[size];
            boolean[] values = new boolean[size];

            source.readIntArray(keys);
            source.readBooleanArray(values);

            for (int i = 0; i < size; i++)
            {
                out.put(keys[i], values[i]);
            }
            return out;
        }

        public SparseBooleanArrayParcelable[] newArray(int size) {return new SparseBooleanArrayParcelable[size];}
    };

    public SparseBooleanArrayParcelable(SparseBooleanArray sparseBooleanArray)
    {
        for (int i = 0; i < sparseBooleanArray.size(); i++)
        {
            this.put(sparseBooleanArray.keyAt(i), sparseBooleanArray.valueAt(i));
        }
    }

    public SparseBooleanArrayParcelable(int initialCapacity)
    {
        super(initialCapacity);
    }

    public SparseBooleanArrayParcelable()
    {
        super();
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        int[] keys = new int[size()];
        boolean[] values = new boolean[size()];

        for (int i = 0; i < size(); i++)
        {
            keys[i] = keyAt(i);
            values[i] = valueAt(i);
        }

        dest.writeInt(size());
        dest.writeIntArray(keys);
        dest.writeBooleanArray(values);
    }
}
