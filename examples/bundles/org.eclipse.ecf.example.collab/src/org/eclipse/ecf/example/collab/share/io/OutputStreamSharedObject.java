/****************************************************************************
* Copyright (c) 2004 Composent, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Composent, Inc. - initial API and implementation
*****************************************************************************/

package org.eclipse.ecf.example.collab.share.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class OutputStreamSharedObject extends OutputStream
{
    public static final int DEFAULT_BUFF_SIZE = 900;
    public static final boolean DEFAULT_COMPRESSION = true;

    protected StreamSender myObj;
    protected ByteArrayOutputStream myOuts;
    protected GZIPOutputStream myGZIP;
    protected int myDefaultLength;
    protected int currentCount = 0;
    protected boolean useCompression;

    public OutputStreamSharedObject(StreamSender obj, int size, boolean compression)
        throws IOException
    {
        myObj = obj;
        myDefaultLength = size;
        useCompression = compression;
        resetStreams();
    }
    public OutputStreamSharedObject(StreamSender obj) throws IOException
    {
        this(obj, DEFAULT_BUFF_SIZE, DEFAULT_COMPRESSION);
    }
    public OutputStreamSharedObject(StreamSender obj, int size) throws IOException
    {
        this(obj, size, DEFAULT_COMPRESSION);
    }
    public OutputStreamSharedObject(StreamSender obj, boolean compression) throws IOException
    {
        this(obj, DEFAULT_BUFF_SIZE, compression);
    }
    protected void resetStreams() throws IOException
    {
        myOuts = new ByteArrayOutputStream(myDefaultLength);
        if (useCompression) {
            myGZIP = new GZIPOutputStream(myOuts);
        }
    }
    public void close() throws IOException
    {
        if (useCompression) {
            myGZIP.close();
        } else myOuts.close();
    }

    public void flush() throws IOException
    {
        sendMsgAndResetStream();
    }
    protected final void sendMsgAndResetStream() throws IOException
    {
        if (useCompression) {
            myGZIP.flush();
            myGZIP.finish();
        } else myOuts.flush();
        // Actually ask our StreamSender to send msg with count of size and data
        myObj.sendDataMsg(currentCount, myOuts.toByteArray());
        resetStreams();
        currentCount = 0;
    }
    public void write(int a) throws IOException
    {
        if (currentCount >= myDefaultLength) {
            sendMsgAndResetStream();
        }
        currentCount++;
        streamWrite(a);
    }
    protected void streamWrite(int a) throws IOException
    {
        if (useCompression) {
            myGZIP.write(a);
        } else myOuts.write(a);
    }
}
