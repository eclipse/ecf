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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.eclipse.ecf.core.sharedobject.util.SimpleFIFOQueue;

public class InputStreamSharedObject extends InputStream
{
    protected SimpleFIFOQueue myQueue = new SimpleFIFOQueue();

    int currentLength;
    int currentRead;
    boolean useCompression;
    ByteArrayInputStream myBIS;
    GZIPInputStream myGZIP;

    public InputStreamSharedObject(boolean compression)
    {
        useCompression = compression;
    }

    public InputStreamSharedObject()
    {
        this(OutputStreamSharedObject.DEFAULT_COMPRESSION);
    }

    protected final void resetStreams(Data d)
        throws IOException
    {
        currentRead = 0;
        myBIS = new ByteArrayInputStream(d.getData());
        currentLength = d.getLength();
        if (useCompression) {
            myGZIP = new GZIPInputStream(myBIS);
        }
    }
    public final int read() throws IOException
    {
        if (currentRead >= currentLength) {
            Data d = (Data)myQueue.dequeue();
            if (d == null) throw new IOException("No data");
            resetStreams(d);
        }
        currentRead++;
        return streamRead();
    }
    protected final int streamRead() throws IOException
    {
        if (useCompression) {
            return myGZIP.read();
        } else return myBIS.read();
    }
    // Method for replicated object to add data to stream
    public void add(int length, byte [] d)
    {
        myQueue.enqueue(new Data(length,d));
    }

    protected static class Data {
        int myLength;
        byte [] myData;
        protected Data(int length, byte [] d)
        {
            myLength = length;
            myData = d;
        }
        protected int getLength()
        {
            return myLength;
        }
        protected byte [] getData()
        {
            return myData;
        }
    }
}
