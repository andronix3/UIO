/*
 * Copyright (c) Andrey Kuznetsov. All Rights Reserved.
 *
 * http://uio.imagero.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Andrey Kuznetsov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.imagero.uio.impl;

import com.imagero.uio.Endian;
import com.imagero.uio.RandomAccessInput;
import com.imagero.uio.Transformer;
import com.imagero.uio.io.IOutils;
import com.imagero.uio.io.UnexpectedEOFException;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Andrey Kuznetsov
 */
public abstract class AbstractRandomAccessInput extends InputStream implements RandomAccessInput {
    protected int byteOrder = BIG_ENDIAN;
    protected boolean child;

    public int getByteOrder() {
	return byteOrder;
    }

    public void setByteOrder(int byteOrder) {
	switch (byteOrder) {
	case Endian.BIG_ENDIAN:
	case Endian.LITTLE_ENDIAN:
	    this.byteOrder = byteOrder;
	    break;
	default:
	    throw new IllegalArgumentException("" + Integer.toHexString(byteOrder));
	}
    }

    public boolean isChild() {
	return child;
    }

    public boolean isBuffered() {
	return false;
    }

    public final int read(byte[] b) throws IOException {
	return read(b, 0, b.length);
    }

    public final void readFully(byte b[]) throws IOException {
	readFully(b, 0, b.length);
    }

    public int skipBytes(int n) throws IOException {
	return (int) skip(n);
    }

    public final boolean readBoolean() throws IOException {
	return read() != 0;
    }

    public final byte readByte() throws IOException {
	return (byte) read();
    }

    public final int readUnsignedByte() throws IOException {
	return read();
    }

    public final short readShort() throws IOException {
	return readShort(byteOrder);
    }

    public final int readUnsignedShort() throws IOException {
	return readUnsignedShort(byteOrder);
    }

    public final int readUnsignedShort(int byteOrder) throws IOException {
	int b0 = read();
	int b1 = read();
	if (b0 == -1 || b1 == -1) {
	    throw new EOFException();
	}
	if (byteOrder == BIG_ENDIAN) {
	    return (b0 << 8) | b1;
	}
	return b0 | (b1 << 8);
    }

    public final short readShort(int byteOrder) throws IOException {
	int b0 = read();
	int b1 = read();
	if (b0 == -1 || b1 == -1) {
	    throw new EOFException();
	}
	if (byteOrder == BIG_ENDIAN) {
	    return (short) ((b0 << 8) | b1);
	}
	return (short) (b0 | (b1 << 8));
    }

    public final char readChar() throws IOException {
	return readChar(byteOrder);
    }

    public final char readChar(int byteOrder) throws IOException {
	int b0 = read();
	int b1 = read();
	if (b0 < 0 || b1 < 0) {
	    throw new EOFException();
	}
	if (byteOrder == BIG_ENDIAN) {
	    return (char) ((b0 << 8) | b1);
	}
	return (char) (b0 | (b1 << 8));
    }

    public final int readInt() throws IOException {
	return readInt(byteOrder);
    }

    public final int readInt(int byteOrder) throws IOException {
	int a0 = read();
	int a1 = read();
	int a2 = read();
	int a3 = read();
	if (a0 == -1 || a1 == -1 | a2 == -1 | a3 == -1) {
	    throw new EOFException();
	}

	if (byteOrder == BIG_ENDIAN) {
	    return ((a0 << 24) | (a1 << 16) | (a2 << 8) | (a3 << 0));
	}
	return ((a0 << 0) + (a1 << 8) + (a2 << 16) + (a3 << 24));
    }

    public long readUnsignedInt() throws IOException {
	return readInt() & 0xFFFFFFFFL;
    }

    public long readUnsignedInt(int byteOrder) throws IOException {
	return readInt(byteOrder) & 0xFFFFFFFFL;
    }

    public final long readLong() throws IOException {
	return readLong(byteOrder);
    }

    public final long readLong(int byteOrder) throws IOException {
	long a0 = read();
	long a1 = read();
	long a2 = read();
	long a3 = read();
	long a4 = read();
	long a5 = read();
	long a6 = read();
	long a7 = read();
	
	if (a0 == -1 || a1 == -1 | a2 == -1 | a3 == -1 | a4 == -1 || a5 == -1 | a6 == -1 | a7 == -1) {
	    throw new EOFException();
	}

	if (byteOrder == BIG_ENDIAN) {
	    return ((a0 << 56) | (a1 << 48) | (a2 << 40) | (a3 << 32) | (a4 << 24) | (a5 << 16) | (a6 << 8) | (a7 << 0));
	}
	return ((a0 << 0) | (a1 << 8) | (a2 << 16) | (a3 << 24) | (a4 << 32) | (a5 << 40) | (a6 << 48) | (a7 << 56));
    }

    public final float readFloat() throws IOException {
	return readFloat(byteOrder);
    }

    public final float readFloat(int byteOrder) throws IOException {
	return Float.intBitsToFloat(readInt(byteOrder));
    }

    public final double readDouble() throws IOException {
	return readDouble(byteOrder);
    }

    public final double readDouble(int byteOrder) throws IOException {
	return Double.longBitsToDouble(readLong(byteOrder));
    }

    public final byte[] read(int length) throws IOException {
	byte[] b0 = new byte[length];
	for (int i = 0; i < b0.length; i++) {
	    b0[i] = readByte();
	}
	return b0;
    }

    public final String readLine() throws IOException {
	byte[] readByteLine = readByteLine();
	if (readByteLine == null) {
	    return null;
	}
	return new String(readByteLine);
    }

    public final String readUTF() throws IOException {
	int length = readUnsignedShort();
	return readUTF(length);
    }

    public final String readUTF(int length) throws IOException {
	byte data[] = new byte[length];
	readFully(data, 0, length);
	return IOutils.readUTF(data);
    }

    public final byte[] readByteLine() throws IOException {
	return Transformer.readByteLine(this);
    }

    public final int readByteLine(byte[] dest) throws IOException {
	return Transformer.readByteLine(this, dest);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
	int n = 0;
	while (n < len) {
	    int count = read(b, off + n, len - n);
	    if (count <= 0) {
		throw new UnexpectedEOFException(n);
	    }
	    n += count;
	}
    }
}
