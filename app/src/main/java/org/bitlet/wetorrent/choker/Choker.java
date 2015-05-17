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

package org.bitlet.wetorrent.choker;

import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.peer.Peer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Choker
{

    private static final int maxUnchoked = 16;
    private Torrent torrent;
    private Map<Peer, Long> unchoked = new HashMap<Peer, Long>();
    private Set<Peer> interested = new HashSet<Peer>();

    public Choker(Torrent torrent)
    {
        this.torrent = torrent;
    }

    public synchronized void interested(Peer peer)
    {
        interested.add(peer);
        if (unchoked.size() < maxUnchoked)
        {
            peer.setIsChoked(false);
            unchoked.put(peer, System.currentTimeMillis());
        }
    }

    public synchronized void notInterested(Peer peer)
    {
        peer.setIsChoked(true);
        unchoked.remove(peer);
        interested.remove(peer);
    }

    public synchronized void choke(Peer peer)
    {
    }

    public synchronized void unchoke(Peer peer)
    {
    }

    public synchronized void interrupted(Peer peer)
    {
        unchoked.remove(peer);
        interested.remove(peer);
    }

    public synchronized void tick()
    {


        if (unchoked.size() == maxUnchoked && interested.size() > maxUnchoked)
        {
            long now = System.currentTimeMillis();
            Peer slowest = null;
            for (Map.Entry<Peer, Long> e : unchoked.entrySet())
            {

                if (now - e.getValue() > 10000)
                {
                    if (slowest == null)
                    {
                        slowest = e.getKey();
                    }
                    else
                    {
                        if ((float) slowest.getDownloaded() / (now - unchoked.get(slowest)) >
                                (float) e.getKey().getDownloaded() / (now - e.getValue()))
                        {
                            slowest = e.getKey();
                        }
                    }
                }
            }

            if (slowest != null)
            {
                slowest.setIsChoked(true);
                unchoked.remove(slowest);

                List<Peer> interestedChoked = new LinkedList<Peer>(interested);
                interestedChoked.removeAll(unchoked.keySet());

                Peer newUnchoked = interestedChoked.get(new Random().nextInt(interestedChoked.size()));
                newUnchoked.setIsChoked(false);
                unchoked.put(newUnchoked, now);
            }

        }

    }
}
