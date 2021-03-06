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
package com.imagero.uio.ba;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * 
 * @author Andrey Kuznetsov
 */
public abstract class BitBufferIn {

    static final int[] bmask = createMask();

    public static int[] andTable = create();

    static class InputStreamBuffer extends ByteArrayInputStream {

	private final InputStream in;

	public InputStreamBuffer(InputStream in) {
	    super(new byte[1024]);
	    this.in = in;
	}

	public int read() {
	    if (available() == 0) {
		fillBuffer();
	    }
	    return super.read();
	}

	private void fillBuffer() {
	    int count = 0;
	    try {
		for (int i = 0; i < buf.length; i++) {
		    int r = in.read();
		    if (r == -1) {
			break;
		    }
		    buf[count++] = (byte) r;
		    count++;
		}
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }
	    this.count = count;
	    this.pos = 0;
	}
    }

    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[1024]);

    public static BitBufferIn create(InputStream in) {
	return new BitBufferIS(in);
    }

    private static int[] createMask() {
	int[] msk = new int[32];
	for (int i = 0; i < msk.length; i++) {
	    msk[i] = (1 << i) - 1;
	}
	return msk;
    }

    private static int[] create() {
	int[] table = new int[32];
	for (int i = 0; i < 32; i++) {
	    table[i] = (1 << i) - 1;
	}
	return table;
    }

    public static BitBufferIn create(byte[] in) {
	return new BitBufferByte(in);
    }

    protected int vbits;
    protected int buffer;
    protected int position;

    public final int get(int nbits) throws IOException {
	int k = peek(nbits);
	vbits -= nbits > vbits ? vbits : nbits;
	return k;
    }

    public abstract int peek(int nbits) throws IOException;

    public final void drop(int nbits) {
	vbits -= nbits;
    }

    public int getVbits() {
	return vbits;
    }

    public int getBuffer() {
	return buffer;
    }

    public void setVbits(int vbits) {
	this.vbits = vbits;
    }

    public void setBuffer(int buffer) {
	this.buffer = buffer;
    }

    public void pushBack(int nbits, int value) {
	if (nbits > 0) {
	    buffer = (value << vbits) | buffer;
	    vbits += nbits;
	}
    }

    public void skipToByteBoundary() throws IOException {
	if (vbits > 0) {
	    int vb = vbits % 8;
	    get(vb);
	}
    }

    public static class BitBufferIS extends BitBufferIn {
	final InputStream in;

	public BitBufferIS(InputStream in) {
	    this.in = in;
	}

	public int peek(int nbits) throws IOException {
	    if (nbits == 0) {
		return 0;
	    }
	    while (nbits > vbits) {
		int k = 0;
		k = in.read();
		position++;
		if (k == -1) {
		    if (vbits == 0) {
			throw new EOFException();
		    } else {
			int v = buffer & bmask[nbits];
			// vbits = 0;
			vbits = nbits;
			return v;
		    }
		}
		buffer = (buffer << 8) | (k & 0xFF);
		vbits += 8;
	    }
	    int k = (buffer >> (vbits - nbits)) & bmask[nbits];
	    // int v = (buffer << (32 - vbits)) >>> (32 - nbits);
	    return k;
	}
    }

    public static class BitBufferByte extends BitBufferIn {
	byte[] in;

	public BitBufferByte(byte[] buffer) {
	    this.in = buffer;
	}

	public void setSource(byte[] source) {
	    this.in = source;
	    position = 0;
	    vbits = 0;
	    buffer = 0;
	}

	public void setSource(ByteArray ba) {
	    this.in = ba.buffer;
	    position = 0;
	    vbits = 0;
	    buffer = 0;
	}

	public void seek(int position) {
	    this.position = position;
	    this.vbits = 0;
	    buffer = 0;
	}

	public void seek(int position, int vbits) throws IOException {
	    this.position = position;
	    this.vbits = 0;
	    buffer = 0;
	    if (vbits > 0) {
		get(8 - vbits);
	    }
	}

	public int peek(int nbits) {
	    if (nbits == 0) {
		return 0;
	    }
	    while (nbits > vbits) {
		if (position >= in.length) {
		    if (vbits <= 0) {
			throw new RuntimeException("EOF");
		    } else {
			int k = buffer & bmask[nbits];
//			vbits = nbits;
			return k;
		    }
		}
		int k = in[position++] & 0xFF;
		buffer = (buffer << 8) | k;
		vbits += 8;
	    }
	    int k = (buffer >> (vbits - nbits)) & bmask[nbits];
	    return k;
	}
    }
}
