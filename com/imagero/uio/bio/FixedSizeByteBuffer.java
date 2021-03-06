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
package com.imagero.uio.bio;

import com.imagero.uio.RandomAccessIO;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class can be used to read from and write to byte array.
 * 
 * @author Andrey Kuznetsov
 */
public class FixedSizeByteBuffer implements UIOBuffer {

	protected byte[] buf;
	protected int count;

	boolean changed;
	BufferIndex index;

	protected FixedSizeByteBuffer(byte buf[]) {
		this.buf = buf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#read(com.imagero.uio.bio.BufferPosition)
	 */
	@Override
	public int read(BufferPosition position) {
		if (availableForReading(position) > 0) {
			int v = buf[position.pos++] & 0xFF;
			return v;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#skip(long,
	 * com.imagero.uio.bio.BufferPosition)
	 */
	@Override
	public long skip(long n, BufferPosition position) {
		long p = Math.max(0, Math.min(count - position.pos, n));
		position.pos += p;
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#createPosition()
	 */
	@Override
	public BufferPosition createPosition() {
		return new BufferPosition(buf.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#availableForReading(com.imagero.uio.bio.
	 * BufferPosition)
	 */
	@Override
	public int availableForReading(BufferPosition position) {
		int min = count > position.bufferSize ? position.bufferSize : count;
		int avail = min - position.pos;
		return avail < 0 ? 0 : avail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#availableForWriting(com.imagero.uio.bio.
	 * BufferPosition)
	 */
	@Override
	public int availableForWriting(BufferPosition position) {
		int avail = buf.length - position.pos;
		return avail < 0 ? 0 : avail;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#read(byte[], int, int,
	 * com.imagero.uio.bio.BufferPosition)
	 */
	@Override
	public int read(byte[] dest, int offset, int length, BufferPosition position) {
		final int available = availableForReading(position);
		int toCopy = Math.max(0, Math.min(length, available));
		if (toCopy > 0) {
			try {
				System.arraycopy(buf, position.pos, dest, offset, toCopy);
			} catch (ArrayIndexOutOfBoundsException ex) {
				ex.printStackTrace();
			}
			position.pos += toCopy;
			return toCopy;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#write(int,
	 * com.imagero.uio.bio.BufferPosition)
	 */
	@Override
	public void write(int b, BufferPosition position) {
		buf[position.pos++] = (byte) b;
		count = Math.max(position.pos, count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#getCount()
	 */
	@Override
	public int getCount() {
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.imagero.uio.bio.UIOBuffer#getPosition(com.imagero.uio.bio.BufferPosition)
	 */
	@Override
	public int getPosition(BufferPosition position) {
		return position.pos;
	}

	public void setCount(int count) {
		this.count = Math.min(Math.max(count, 0), buf.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#writeBuffer(java.io.OutputStream, boolean)
	 */
	@Override
	public void writeBuffer(OutputStream out, boolean wholeBuffer) throws IOException {
		if (wholeBuffer) {
			out.write(buf);
		} else {
			out.write(buf, 0, count);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#writeBuffer(java.io.DataOutput, boolean)
	 */
	@Override
	public void writeBuffer(DataOutput out, boolean wholeBuffer) throws IOException {
		if (wholeBuffer) {
			out.write(buf);
		} else {
			out.write(buf, 0, count);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#writeBuffer(java.io.OutputStream)
	 */
	@Override
	public void writeBuffer(OutputStream out) throws IOException {
		out.write(buf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#writeBuffer(java.io.DataOutput)
	 */
	@Override
	public void writeBuffer(DataOutput out) throws IOException {
		out.write(buf);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.imagero.uio.bio.UIOBuffer#write(byte[], int, int,
	 * com.imagero.uio.bio.BufferPosition)
	 */
	@Override
	public int write(byte src[], int offset, int length, BufferPosition position) {
		int available = availableForWriting(position);
		int toCopy = Math.max(0, Math.min(length, available));
		if (toCopy > 0) {
			System.arraycopy(src, offset, buf, position.pos, toCopy);
			position.pos += toCopy;
			count = Math.max(count, position.pos);
		}
		return toCopy;
	}

	public RandomAccessIO create() {
		return new FSBRandomAccessIO(this);
	}

	public RandomAccessIO create(int offset, int length) {
		return new FSBRandomAccessIO(this, offset, length);
	}

	public static FixedSizeByteBuffer createBuffer(byte buf[]) {
		return new FixedSizeByteBuffer(buf);
	}
}
