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

package org.bitlet.wetorrent.peer;

import org.bitlet.wetorrent.peer.message.Message;
import org.bitlet.wetorrent.peer.message.Request;
import org.bitlet.wetorrent.util.thread.InterruptableTasksThread;
import org.bitlet.wetorrent.util.thread.ThreadTask;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alex
 */
public class WebSeed implements Peer
{

    //    static final int TOT_SIZE = 422279;
    static final int TOT_SIZE = 3576986;
    private final static Request requestEnd = new Request(Integer.MAX_VALUE, 0, 0);
    static FileOutputStream fos;
    static int wrote = 0;
    private final BlockingQueue<Request> pendingRequests = new LinkedBlockingQueue<Request>();
    private URL url;
    private HttpURLConnection connection;
    private InterruptableTasksThread downloaderThread;
    private PeersManager peersManager;
    private byte[] peerId = new byte[20];
    private byte[] bitfield;
    private long downloaded = 0;

    public WebSeed(URL url, PeersManager peersManager)
    {

        this.url = url;
        this.peersManager = peersManager;

        bitfield = peersManager.getTorrent().getTorrentDisk().getBitfieldCopy();

        for (int i = 0; i < bitfield.length; i++)
            bitfield[i] |= 0xFF;

        Random random = new Random(System.currentTimeMillis());
        random.nextBytes(peerId);
        System.arraycopy("-WT-HTTP".getBytes(), 0, peerId, 0, 8);

        downloaderThread = new InterruptableTasksThread();
        downloaderThread.addTask(new HttpRangeDownloader());
    }

    public void requestEnd()
    {
        pendingRequests.add(requestEnd);
    }

    public void start()
    {
        downloaderThread.start();
    }

    public void interrupt()
    {
        downloaderThread.interrupt();
    }

    public void sendMessage(Message message)
    {
        if (message.getType() == Message.REQUEST)
        {
            pendingRequests.add((Request) message);
        }
    }

    public void setAmInterested(boolean b)
    {
        peersManager.getTorrent().unchoke(this);
    }

    public void setIsChoked(boolean b) {}

    public long getDownloaded()
    {
        return downloaded;
    }

    public InetAddress getIp()
    {
        InetAddress ia = null;
        try
        {
            ia = InetAddress.getByName(url.getHost());
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(WebSeed.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ia;
    }

    public int getPort()
    {
        return url.getPort();
    }

    public byte[] getPeerId()
    {
        return peerId;
    }

    public long getLastReceivedMessageMillis()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int getUnfulfilledRequestNumber()
    {
        return pendingRequests.size();
    }

    public long getUploaded()
    {
        return 0;
    }

    public boolean hasPiece(int index)
    {
        return true;
    }

//    public static void main(String[] args) throws Exception {
//        final int TOT_SIZE = 22636132;
//        final int BLOCK_SIZE = 1024;
//
//        URL theUrl = new URL("http://nofatclips.com/02009/01/21/magic/Preproduction.ogv");
//
//        WebSeed seed = new WebSeed(theUrl);
//
//        HttpURLConnection conn = (HttpURLConnection) theUrl.openConnection();
//
//        conn.connect();
//        InputStream is = conn.getInputStream();
//
//        FileOutputStream fos = new FileOutputStream("/Users/Alex/Desktop/target.ogv");
//
//        byte[] buf = new byte[BLOCK_SIZE];
//        int len = 0;
//        int offset = 0;
//
//        while ((len = is.read(buf)) > 0) {
//            fos.write(buf, 0, len);
//            offset += len;
//
//            System.out.println("offset: " + offset);
//        }
//
//        is.close();
//        fos.close();
//        conn.disconnect();
//    }

    public boolean isAmChoked()
    {
        return false;
    }

    public boolean isSeeder()
    {
        return true;
    }

    private void byteSink(byte[] stuff) throws Exception
    {
        fos.write(stuff);
        wrote += stuff.length;
        System.out.println("wrote: " + wrote);

        if (wrote == TOT_SIZE)
        {
            fos.close();
        }

    }

    class HttpRangeDownloader implements ThreadTask
    {

        public boolean execute() throws Exception
        {
            Request nextRequest = pendingRequests.take();

            if (nextRequest != requestEnd)
            {
                byte[] range = downloadRange(nextRequest);

                // XXX placeholder
                byteSink(range);
                return true;
            }
            else
                return false;
        }

        public void interrupt()
        {
            try
            {
                pendingRequests.put(requestEnd);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(WebSeed.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        public void exceptionCaught(Exception e)
        {
            e.printStackTrace();
        }

        @Deprecated
        public void exceptionCought(Exception e)
        {
            exceptionCaught(e);
        }

        private byte[] downloadRange(Request request) throws IOException
        {
            byte[] result = new byte[request.getLength()];
            // TODO connection should be openend elsewhere
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Range", "bytes=" + request.getBegin() + "-" + (request.getBegin() + request.getLength() - 1));
            System.out.println("Range: bytes=" + request.getBegin() + "-" + (request.getBegin() + request.getLength() - 1));

            connection.connect();

            int response = connection.getResponseCode();
            DataInputStream is = new DataInputStream(connection.getInputStream());

            if (response == HttpURLConnection.HTTP_ACCEPTED ||
                    response == HttpURLConnection.HTTP_OK ||
                    response == HttpURLConnection.HTTP_PARTIAL)
            {

                is.readFully(result, 0, request.getLength());
            }

            is.close();

            return result;
        }
    }
/*
    public static void main(String[] args) throws Exception {
        final int BLOCK_SIZE = 1 << 19;

        URL theUrl = new URL("http://nofatclips.com/02009/01/21/magic/Preproduction.ogv");
        fos = new FileOutputStream("/Users/casta/Desktop/target.ogv");

//        URL theUrl = new URL("http://www.ietf.org/rfc/rfc2616.txt");
//        fos = new FileOutputStream("/Users/Alex/Desktop/target.txt");

        WebSeed seed = new WebSeed(theUrl);

        int i = 0;
        int offset = 0;
        int length = BLOCK_SIZE;
        // splittone
        while (offset < TOT_SIZE) {
            if (offset + length > TOT_SIZE)
                length = TOT_SIZE - offset;
            seed.requestPiece(i, offset, length);
            offset += length;
        }

        seed.requestEnd();

        seed.start();


    }
*/

}
