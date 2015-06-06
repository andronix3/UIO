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

package com.imagero.uio.blob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import com.imagero.java.util.Debug;
import com.imagero.uio.RandomAccessIO;
import com.imagero.uio.RandomAccessInput;
import com.imagero.uio.io.UnexpectedEOFException;

/**
 * Blob - Object which encapsulates (possible deferred) data which may come from
 * different sources.
 * 
 * @author Andrey Kuznetsov
 */
public abstract class IBlob implements Blob {

    protected long length;

    Object compression;

    public abstract byte[] getBytes(long pos, int length);

    /**
     * retrieve data from this Blob
     * 
     * @param start
     *            start offset
     * @param dest
     *            where to copy data
     * @return how much byte were copied
     * @throws java.io.IOException
     */
    // public abstract int get(long start, byte[] dest) throws IOException;

    /**
     * determine if this Blob is writable and method set(long, byte[]) can be
     * used to change content of this Blob
     * 
     * @return true if Blob is writable
     */
    public boolean writable() {
	return false;
    }

    /**
     * set data (work only if writable returns true)
     * 
     * @param start
     *            start in destination
     * @param data
     *            new data
     */
    public int setBytes(long start, byte[] data) throws SQLException {
	throw new SQLFeatureNotSupportedException();
    }

    public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
	throw new SQLFeatureNotSupportedException();
    }

    /**
     * release reloadable resources
     */
    public void free() {

    }

    protected boolean lengthKnown() {
	return length > 0;
    }

    protected abstract long computeLength() throws IOException;

    public long length() {
	if (!lengthKnown()) {
	    try {
		length = computeLength();
	    } catch (IOException ex) {
		Debug.error("Can't compute Blob length. " + ex.getMessage());
	    }
	}
	return length;
    }

    public void truncate(long length) {

    }

    public InputStream getBinaryStream() {
	return new BlobInputStream(this, length());
    }

    public InputStream getBinaryStream(long pos, long length) {
	return new BlobInputStream(this, pos, length);
    }

    public final OutputStream setBinaryStream(long pos) throws SQLException {
	throw new SQLFeatureNotSupportedException();
    }

    public long position(byte pattern[], long start) throws SQLException {
	throw new SQLFeatureNotSupportedException();
    }

    public long position(Blob pattern, long start) throws SQLException {
	throw new SQLFeatureNotSupportedException();
    }

    public long getChildPosition(InputStream in) {
	if (in instanceof BlobInputStream) {
	    return ((BlobInputStream) in).getPosition();
	}
	return -1;
    }

    public void setChildPosition(InputStream in, long pos) {
	if (in instanceof BlobInputStream) {
	    ((BlobInputStream) in).setPosition(pos);
	}
    }

    public Object getCompression() {
	return compression;
    }

    public void setCompression(Object property) {
	compression = property;
    }

    public static class BaBlob extends IBlob {
	int offset;

	private byte[] blob;
	
	protected BaBlob() {
	    
	}

	public BaBlob(byte[] blob) {
	    this(blob, 0, blob.length);
	}

	public BaBlob(byte[] blob, int offset, int length) {
	    this.offset = offset;
	    this.length = length;
	    this.setBlob(blob);
	}

	protected long computeLength() {
	    return length;
	}

	static final byte[] empty = new byte[0];

	public byte[] getBytes(long start, int length) {
	    int len = (int) Math.max(Math.min(length, this.length - start), 0);
	    if (len == 0) {
		return empty;
	    }
	    byte[] dest = new byte[len];
	    System.arraycopy(getBlob(), (int) start + offset, dest, 0, len);
	    return dest;
	}

	public int get(long start, byte[] dest) {
	    long max = Math.max(Math.min(dest.length, this.length - start), 0);
	    if (max > 0) {
		System.arraycopy(getBlob(), (int) start + offset, dest, 0, (int) max);
	    }
	    return (int) max;
	}

	public boolean writable() {
	    return true;
	}

	public int setBytes(long start, byte[] data) {
	    System.arraycopy(data, 0, getBlob(), (int) start, data.length);
	    return data.length;
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
	    System.arraycopy(bytes, offset, getBlob(), (int) pos, len);
	    return len;
	}

	public InputStream getBinaryStream() {
	    return new ByteArrayInputStreamP(getBlob(), offset, (int) length());
	}

	public InputStream getBinaryStream(long pos, long length) {
	    return new ByteArrayInputStreamP(getBlob(), offset + (int) pos, (int) length);

	}

	public long getChildPosition(InputStream in) {
	    if (in instanceof ByteArrayInputStreamP) {
		return ((ByteArrayInputStreamP) in).getPosition();
	    }
	    return -1;
	}

	public void setChildPosition(InputStream in, long pos) {
	    if (in instanceof ByteArrayInputStreamP) {
		((ByteArrayInputStreamP) in).setPosition(pos);
	    }
	}

	protected void setBlob(byte[] blob) {
	    this.blob = blob;
	}

	protected byte[] getBlob() {
	    return blob;
	}
    }

    static class ByteArrayInputStreamP extends ByteArrayInputStream {
	public ByteArrayInputStreamP(byte buf[]) {
	    super(buf);
	}

	public ByteArrayInputStreamP(byte buf[], int offset, int length) {
	    super(buf, offset, length);
	}

	public long getPosition() {
	    return pos;
	}

	public void setPosition(long pos) {
	    this.pos = (int) (pos & Integer.MAX_VALUE);
	}
    }

    public static class RoBlob extends IBlob {
	RandomAccessInput ro;
	long start;

	public RoBlob(RandomAccessInput ro, long start, long length) {
	    this.ro = ro;
	    this.start = start;
	    this.length = length;
	}

	protected long computeLength() {
	    return length;
	}

	public byte[] getBytes(long start, int length) {
	    long pos = 0;
	    byte[] dest = new byte[length];
	    try {
		pos = ro.getFilePointer();
		ro.seek(this.start + start);
		ro.readFully(dest);
	    } catch (IOException ex) {
		Debug.print(ex);
	    } finally {
		try {
		    ro.seek(pos);
		} catch (IOException ex) {
		    Debug.print(ex);
		}
	    }
	    return dest;
	}

	public int get(long start, byte[] dest) throws IOException {
	    long max = Math.min(dest.length, length - start);
	    long pos = ro.getFilePointer();
	    try {
		ro.seek(this.start + start);
		ro.readFully(dest, 0, (int) max);
	    } catch (UnexpectedEOFException ex) {
		return (int) ex.getCount();
	    } finally {
		ro.seek(pos);
	    }
	    return (int) max;
	}

	public boolean writable() {
	    return ro instanceof RandomAccessIO;
	}

	public int setBytes(long start, byte[] data) {
	    if (writable()) {
		RandomAccessIO ra = (RandomAccessIO) ro;
		try {
		    ra.seek(start);
		    ra.write(data);
		} catch (IOException ex) {
		    Debug.print(ex);
		}
	    }
	    return data.length;
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
	    if (writable()) {
		RandomAccessIO ra = (RandomAccessIO) ro;
		try {
		    ra.seek(pos);
		    ra.write(bytes, offset, len);
		} catch (IOException ex) {
		    Debug.print(ex);
		}
	    }
	    return len;
	}

	public InputStream getBinaryStream() {
	    return ro.createInputStream(start, (int) length());
	}

	public InputStream getBinaryStream(long pos, long length) {
	    return ro.createInputStream(start + pos, (int) length);
	}

	public long getChildPosition(InputStream in) {
	    return ro.getChildPosition(in);
	}

	public void setChildPosition(InputStream in, long pos) {
	    ro.setChildPosition(in, pos);
	}
    }

    public static class SeqBlob extends IBlob {

	Blob first;
	Blob second;

	public SeqBlob(IBlob first, Blob second) {
	    this.first = first;
	    this.second = second;
	}

	public byte[] getBytes(long start, int length) {
	    try {
		if (start < first.length()) {
		    return first.getBytes(start, length);
		}
		return second.getBytes(start - first.length(), length);
	    } catch (SQLException ex) {
		return new byte[0];
	    }
	}

	public int setBytes(long pos, byte[] bytes) throws SQLException {
	    try {
		final long len = first.length();
		if (pos < len) {
		    long len_1 = len - pos;
		    if (len_1 > bytes.length) {
			return first.setBytes(pos, bytes);
		    }
		    first.setBytes(pos, bytes, 0, (int) len_1);
		    int len_2 = (int) (bytes.length - len_1);
		    second.setBytes(0, bytes, (int) len_1, len_2);
		    return bytes.length;
		}
		return second.setBytes(pos - len, bytes, 0, bytes.length);
	    } catch (SQLException ex) {
		return 0;
	    }
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
	    throw new SQLFeatureNotSupportedException();
	}

	public void free() {
	    try {
		first.free();
		second.free();
	    } catch (SQLException ex) {
		Debug.error(ex);
	    }
	}

	protected long computeLength() throws IOException {
	    try {
		return first.length() + second.length();
	    } catch (SQLException ex) {
		Debug.error(ex);
		return 0;
	    }
	}

	public InputStream getBinaryStream() {
	    InputStream stream1;
	    try {
		stream1 = first.getBinaryStream();
		InputStream stream2 = second.getBinaryStream();
		return new SequenceInputStream(stream1, stream2);
	    } catch (SQLException ex) {
		Debug.error(ex);
		return null;
	    }
	}

	public InputStream getBinaryStream(long pos, long length) {
	    long len;
	    try {
		len = first.length();

		if (len < pos) {
		    long len_1 = len - pos;
		    if (len_1 >= length) {
			return first.getBinaryStream(pos, length);
		    }
		    return new SequenceInputStream(first.getBinaryStream(pos, len_1), second.getBinaryStream(0, length - len_1));
		}
		long offset = len - pos;
		return second.getBinaryStream(offset, length);
	    } catch (SQLException ex) {
		Debug.error(ex);
		return null;
	    }
	}
    }

    public static BlobRef getRef(Blob blob) {
	if (blob instanceof BlobRef) {
	    return (BlobRef) blob;
	}
	return new BlobRef(blob);
    }

    public static class BlobRef implements Blob {
	Blob ref;

	protected BlobRef(Blob ref) {
	    this.ref = ref;
	}

	public void free() throws SQLException {
	    ref.free();
	}

	public byte[] getBytes(long start, int length) throws SQLException {
	    return ref.getBytes(start, length);
	}

	public int setBytes(long start, byte[] data) throws SQLException {
	    return ref.setBytes(start, data);
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
	    return ref.setBytes(pos, bytes, offset, len);
	}

	public void replace(Blob newRef) {
	    this.ref = newRef;
	}

	public InputStream getBinaryStream() throws SQLException {
	    return ref.getBinaryStream();
	}

	public InputStream getBinaryStream(long pos, long length) throws SQLException {
	    return ref.getBinaryStream(pos, length);
	}

	public long length() throws SQLException {
	    return ref.length();
	}

	public long position(byte[] pattern, long start) throws SQLException {
	    return ref.position(pattern, start);
	}

	public long position(Blob pattern, long start) throws SQLException {
	    return ref.position(pattern, start);
	}

	public OutputStream setBinaryStream(long pos) throws SQLException {
	    return ref.setBinaryStream(pos);
	}

	public void truncate(long len) throws SQLException {
	    ref.truncate(len);
	}
    }
}
