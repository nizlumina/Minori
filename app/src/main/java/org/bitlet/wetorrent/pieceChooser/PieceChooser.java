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

package org.bitlet.wetorrent.pieceChooser;

import org.bitlet.wetorrent.Event;
import org.bitlet.wetorrent.Torrent;
import org.bitlet.wetorrent.peer.Peer;
import org.bitlet.wetorrent.peer.message.Cancel;
import org.bitlet.wetorrent.peer.message.Request;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;


public abstract class PieceChooser
{

    public final static int blockLength = 1 << 14;
    protected Torrent torrent;
    /**
     * Pieces in this set are almost complete or complete. We have already requested all the blocks
     */
    private Set<Integer> completingPieces = new HashSet<Integer>();
    /**
     * Associates a peer to the next piece that should be suggested to download
     */
    private Map<Peer, Integer> peerPiece = new HashMap<Peer, Integer>();
    /**
     * Associates a peer to its pending requests
     */
    private Map<Peer, Set<RequestExtended>> peerPendingRequests = new HashMap<Peer, Set<RequestExtended>>();
    /**
     * Associates a piece to its pending requests
     */
    private Map<Integer, Set<RequestExtended>> piecePendingRequests = new HashMap<Integer, Set<RequestExtended>>();
    /*
     * All the pending requests, ordered by the creation date
     */
    private Set<RequestExtended> allPendingRequests = new HashSet<RequestExtended>();
    /*
     * All the pending requests, ordered by the creation date
     */
    private Set<RequestExtended> orphanPendingRequests = new HashSet<RequestExtended>();

    protected abstract Integer choosePiece(Peer peer, int[] piecesFrequencies);

    protected Set<RequestExtended> getPiecePendingRequests(int index)
    {
        return piecePendingRequests.get(index);
    }

    protected RequestExtended recoverRequest(Peer peer)
    {

        /* Search request in limbo */
        for (RequestExtended re : orphanPendingRequests)
        {
            if (peer.hasPiece(re.getIndex()))
            {

                if (Torrent.verbose)
                    torrent.addEvent(new Event(this, "Recovering limbo piece ", Level.FINER));
                return re;
            }
        }
        return null;
    }

    public synchronized Request getNextBlockRequest(Peer peer, int[] piecesFrequencies)
    {

        RequestExtended recoveredRequest = recoverRequest(peer);
        if (recoveredRequest != null)
        {
            associateRequest(peer, recoveredRequest);
            return recoveredRequest;
        }

        Integer pieceIndex = peerPiece.get(peer);
        // null if not downloading, completing if piece is completing
        if (pieceIndex == null || torrent.getTorrentDisk().isCompleted(pieceIndex) || completingPieces.contains(pieceIndex))
        {

            peerPiece.remove(peer);
            // get next piece
            pieceIndex = choosePiece(peer, piecesFrequencies);

            if (Torrent.verbose)
                torrent.addEvent(new Event(this, "chosen piece " + pieceIndex, Level.FINER));

            // no more pieces to download
            if (pieceIndex == null)
                return endGameBlockRequest(peer);

            // requested piece pieceIndex to peer
            peerPiece.put(peer, pieceIndex);
        }

        int firstMissingByte = getFirstMissingByte(pieceIndex);
        if (firstMissingByte >= torrent.getTorrentDisk().getLength(pieceIndex))
            throw new AssertionError("this piece is already completely requested, we should not be here.");
        int pieceMissingBytes = torrent.getTorrentDisk().getLength(pieceIndex) - firstMissingByte;
        int requestBlockLength = blockLength;

        // we have all but the last block
        if (pieceMissingBytes <= blockLength)
        {
            completingPieces.add(pieceIndex);
            peerPiece.remove(peer);

            requestBlockLength = pieceMissingBytes;

            if (Torrent.verbose)
                torrent.addEvent(new Event(this, "request piece completed " + pieceIndex + " " + firstMissingByte + " " + requestBlockLength, Level.FINER));
        }

        RequestExtended re = new RequestExtended(pieceIndex, firstMissingByte, requestBlockLength);

        associateRequest(peer, re);
        return re;
    }

    private int getFirstMissingByte(Integer pieceIndex)
    {
        int firstMissingByte = torrent.getTorrentDisk().getFirstMissingByte(pieceIndex);
        Collection<RequestExtended> pendingRequests = piecePendingRequests.get(pieceIndex);
        
        /*
         * This works just because we made the requests in a sequential order,
         * So, we know that the max( firstMissingByte ,max( pendingRequests.getEnd() )) 
         * is the next missing byte.
         */
        if (pendingRequests != null)
        {
            for (RequestExtended r : pendingRequests)
            {
                // if the first missing byte is being downloaded by the other peer
                if (firstMissingByte < r.getBegin() + r.getLength())
                {
                    firstMissingByte = r.getBegin() + r.getLength();
                }
            }
        }

        return firstMissingByte;
    }

    public synchronized void interrupted(Peer peer)
    {
        peerPiece.remove(peer);
        Set<RequestExtended> pendingRequests = peerPendingRequests.get(peer);
        if (pendingRequests != null)
        {
            for (RequestExtended re : pendingRequests)
            {
                re.getPeers().remove(peer);
                if (re.getPeers().size() == 0)
                    orphanPendingRequests.add(re);
            }
            peerPendingRequests.remove(peer);
        }
    }

    /**
     * A piece has been received
     */
    public synchronized void piece(int index, int begin, byte[] block, Peer peer)
    {
        RequestExtended request = null;
        Set<RequestExtended> pendingRequests = null;

        pendingRequests = peerPendingRequests.get(peer);
        if (pendingRequests != null)
        {
            for (RequestExtended re : pendingRequests)
            {
                if (re.getIndex() == index && re.getBegin() == begin && re.getLength() == block.length)
                {
                    request = re;
                    break;
                }
            }
            if (request != null)
            {
                pendingRequests.remove(request);
                if (pendingRequests.size() == 0)
                    peerPendingRequests.remove(peer);
                request.getPeers().remove(peer);
                
                /*
                 * Cancel the same request to all the other peers
                 */
                for (Peer p : request.getPeers())
                {
                    p.sendMessage(new Cancel(index, begin, block.length));
                    pendingRequests = peerPendingRequests.get(p);
                    pendingRequests.remove(request);
                    if (pendingRequests.size() == 0)
                        peerPendingRequests.remove(p);
                }

                piecePendingRequests.get(index).remove(request);
                allPendingRequests.remove(request);
            }
        } 
        /*
         * else, the peer is sending us a block we didn't request
         * or a block we already canceled
         */

    }

    private Request endGameBlockRequest(Peer peer)
    {
        RequestExtended reToReturn = null;
        for (RequestExtended re : allPendingRequests)
        {
            if (peer.hasPiece(re.getIndex()) && !re.getPeers().contains(peer) &&
                    (reToReturn == null || reToReturn.getPeers().size() > re.getPeers().size()))
                reToReturn = re;
        }

        if (reToReturn != null)
        {
            associateRequest(peer, reToReturn);
            if (Torrent.verbose)
                torrent.addEvent(new Event(this, "Duplicated request ", Level.FINER));
        }
        return reToReturn;
    }

    private void associateRequest(Peer peer, RequestExtended re)
    {
        Set<RequestExtended> pendingRequests = null;
        /*
         * piecePengingRequests
         */
        pendingRequests = piecePendingRequests.get(re.getIndex());
        if (pendingRequests == null)
        {
            pendingRequests = new HashSet<RequestExtended>();
            piecePendingRequests.put(re.getIndex(), pendingRequests);
        }
        pendingRequests.add(re);
        
        /*
         * peerPengingRequests
         */
        pendingRequests = peerPendingRequests.get(peer);
        if (pendingRequests == null)
        {
            pendingRequests = new HashSet<RequestExtended>();
            peerPendingRequests.put(peer, pendingRequests);
        }
        pendingRequests.add(re);

        allPendingRequests.add(re);
        orphanPendingRequests.remove(re);
        re.getPeers().add(peer);
    }

    public Torrent getTorrent()
    {
        return torrent;
    }

    public void setTorrent(Torrent torrent)
    {
        this.torrent = torrent;
    }

    public boolean isCompletingPiece(int pieceIndex)
    {
        return completingPieces.contains(pieceIndex);
    }
}