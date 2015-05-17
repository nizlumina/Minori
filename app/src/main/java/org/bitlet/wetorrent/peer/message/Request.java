/*
 *              bitlet - Simple bittorrent library
 *  Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.bitlet.wetorrent.peer.message;

import org.bitlet.wetorrent.util.Utils;

public class Request extends Message
{

    private int index;
    private int length;
    private int begin;

    /**
     * Creates a new instance of Piece
     */
    public Request(int index, int begin, int length)
    {
        super(REQUEST, null);

        this.index = index;
        this.begin = begin;
        this.length = length;

        byte[] payload = new byte[12];
        System.arraycopy(Utils.intToByteArray(index), 0, payload, 0, 4);
        System.arraycopy(Utils.intToByteArray(begin), 0, payload, 4, 4);
        System.arraycopy(Utils.intToByteArray(length), 0, payload, 8, 4);
        setPayload(payload);
    }

    public int getIndex()
    {
        return index;
    }

    public int getLength()
    {
        return length;
    }

    public int getBegin()
    {
        return begin;
    }
}
