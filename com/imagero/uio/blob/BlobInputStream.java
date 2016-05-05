package com.imagero.uio.blob;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BlobInputStream. Read data from Blob.
 * 
 * Data from Blob first filled into internal buffer and then read.
 * 
 * Date: 23.07.2007
 * 
 * @author Andrey Kuznetsov
 */
public class BlobInputStream extends ByteArrayInputStream {
	Blob blob;

	boolean finished;

	long position;

	long length;

	public BlobInputStream(Blob blob) throws SQLException {
		this(blob, blob.length());
	}

	public BlobInputStream(Blob blob, long length) {
		this(blob, 0, length);
	}

	public BlobInputStream(Blob blob, long position, long length) {
		super(new byte[2048]);
		this.blob = blob;
		this.length = length;
		setPosition(position);
	}

	public int read() {
		if (finished) {
			return -1;
		}
		if (available() > 0) {
			if (position++ >= length) {
				finished = true;
			}
			return super.read();
		}
		return -1;
	}

	public synchronized long skip(long n) {
		int k = available();
		if (k < n) {
			fillBuffer();
		}
		long skip = super.skip(n);
		position += skip;
		if (position >= length) {
			finished = true;
		}

		return skip;
	}

	public synchronized int available() {
		int k = super.available();
		if (k > 0) {
			return k;
		}
		fillBuffer();
		return super.available();
	}

	public synchronized int read(byte b[], int off, int len) {
		if (finished) {
			return -1;
		}
		int k = available();
		int read = super.read(b, off, Math.min(k, len));
		position += read;
		if (position >= length) {
			finished = true;
		}
		return read;
	}

	private void fillBuffer() {
		try {
			byte[] bytes = blob.getBytes(position, buf.length);
			count = bytes.length;
			System.arraycopy(bytes, 0, buf, 0, count);
		} catch (Exception ex) {
			finished = true;
			Logger.getLogger(getClass().getName()).log(Level.WARNING, ex.getMessage(), ex);
		}
		if (count <= 0) {
			count = 0;
			pos = 0;
			finished = true;
			return;
		}
		// position += count;
		pos = 0;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long pos) {
		finished = false;
		position = pos;
		if (position < length) {
			finished = false;
		} else {
			finished = true;
		}
		fillBuffer();
	}
}
