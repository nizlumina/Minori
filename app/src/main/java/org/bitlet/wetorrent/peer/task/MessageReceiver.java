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

package org.bitlet.wetorrent.peer.task;

import org.bitlet.wetorrent.Event;
import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.peer.TorrentPeer;
import org.bitlet.wetorrent.peer.message.Message;
import org.bitlet.wetorrent.util.thread.ThreadTask;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.logging.Level;

public class MessageReceiver implements ThreadTask
{

    private TorrentPeer peer;
    private long lastReceivedMessageMillis;
    private long receivedBytes;

    public MessageReceiver(TorrentPeer peer)
    {
        this.peer = peer;
    }

    private synchronized void addReceivedBytes(Integer byteNumber)
    {
        receivedBytes += byteNumber;
    }

    public synchronized Long getReceivedBytes()
    {
        return receivedBytes;
    }

    public boolean execute() throws Exception
    {

        try
        {

            DataInputStream is = new DataInputStream(peer.getSocket().getInputStream());

            // peer.getPeersManager().getTorrent().addEvent(new Event(this, "Waiting new message",Level.FINEST));
            int prefixLength = is.readInt();

            addReceivedBytes(4 + prefixLength);

            if (prefixLength == 0)
            { // keep alive message

                peer.keepAlive();
            }
            else if (prefixLength > 0)
            {
                byte messageId = is.readByte();
                switch (messageId)
                {
                    case Message.CHOKE: // choke: <len=0001><id=0>

                        if (prefixLength != 1)
                        {
                            throw new ProtocolException("pl " + prefixLength);
                        }
                        peer.choke();
                        break;
                    case Message.UNCHOKE: // unchoke: <len=0001><id=1>

                        if (prefixLength != 1)
                        {
                            throw new ProtocolException();
                        }
                        peer.unchoke();
                        break;
                    case Message.INTERESTED: // interested

                        if (prefixLength != 1)
                        {
                            throw new ProtocolException();
                        }
                        peer.interested();
                        break;
                    case Message.NOT_INTERESTED: // not interested

                        if (prefixLength != 1)
                        {
                            throw new ProtocolException();
                        }
                        peer.notInterested();
                        break;
                    case Message.HAVE: // have: <len=0005><id=4><piece index>

                        if (prefixLength != 5)
                        {
                            throw new ProtocolException();
                        }
                        peer.have(is.readInt());
                        break;
                    case Message.BITFIELD: // bitfield: <len=0001+X><id=5><bitfield>

                        if (prefixLength != 1 + peer.getBitfieldCopy().length)
                        {
                            throw new ProtocolException();
                        }
                        byte[] bitField = new byte[prefixLength - 1];
                        is.readFully(bitField);
                        peer.bitfield(bitField);
                        break;
                    case Message.REQUEST: // request: <len=0013><id=6><index><begin><length>

                        if (prefixLength != 13)
                        {
                            throw new ProtocolException();
                        }
                        peer.request(is.readInt(), is.readInt(), is.readInt());
                        break;
                    case Message.PIECE: // piece: <len=0009+X><id=7><index><begin><block>

                        if (prefixLength < 10)
                        {
                            throw new ProtocolException();
                        }
                        int index = is.readInt();
                        int begin = is.readInt();
                        byte[] block = new byte[prefixLength - 9];
                        is.readFully(block);
                        peer.piece(index, begin, block);
                        break;
                    case Message.CANCEL: // cancel: <len=0013><id<=8><index><begin><length>

                        if (prefixLength != 13)
                        {
                            throw new ProtocolException();
                        }
                        peer.cancel(is.readInt(), is.readInt(), is.readInt());
                        break;
                    default:
                        // discard it
                        is.skipBytes(prefixLength - 1);
                        if (Torrent.verbose)
                        {
                            peer.getPeersManager().getTorrent().addEvent(new Event(this, "message discarded " + messageId, Level.FINE));
                        }
                        break;

                }

            }
            else
            {
                throw new Exception("Negative prefix length");
            }
            setLastReceivedMessageMillis(System.currentTimeMillis());

        }
        catch (Exception e)
        {
            if (Torrent.verbose)
            {
                peer.getPeersManager().getTorrent().addEvent(new Event(peer, "Problem waiting for new message", Level.WARNING));
            }
            e.printStackTrace(System.err);
            throw e;
        }
        return true;
    }

    public void interrupt()
    {
        try
        {
            peer.getSocket().close();
        }
        catch (IOException ex)
        {
        }
    }

    public void exceptionCought(Exception e)
    {
        peer.interrupt();
    }

    public synchronized long getLastReceivedMessageMillis()
    {
        return lastReceivedMessageMillis;
    }

    public synchronized void setLastReceivedMessageMillis(long lastReceivedMessageMillis)
    {
        this.lastReceivedMessageMillis = lastReceivedMessageMillis;
    }
}