package com.imagero.uio.io;

import java.io.IOException;
import java.io.InputStream;

public abstract class REO_InputStream extends InputStream {

    private final InputStream closedInputStream = new ClosedInputStream();

    private InputStream in;
    private long count;
    private long mark;

    public void reopen() throws IOException {
	IOutils.closeStream(in);
	in = reopenImpl();
    }

    protected abstract InputStream reopenImpl() throws IOException;

    public boolean isClosed() {
	return in == closedInputStream;
    }

    public int read() throws IOException {
	int a = in.read();
	if (a >= 0) {
	    count++;
	}
	return a;
    }

    public int read(byte[] b, int off, int len) throws IOException {
	int read = in.read(b, off, len);
	count += read;
	return read;
    }

    public long skip(long n) throws IOException {
	long skip = in.skip(n);
	count += skip;
	return skip;
    }

    public long getPosition() {
	return count;
    }

    public void seek(long n) throws IOException {
	if (n > count) {
	    skip(n - count);
	} else {
	    reopen();
	    skip(n);
	}
    }

    @Override
    public void close() throws IOException {
	IOutils.closeStream(in);
	in = closedInputStream;
    }

    @Override
    public synchronized void mark(int readlimit) {
	mark = count;
    }

    @Override
    public synchronized void reset() throws IOException {
	seek(mark);
    }

    @Override
    public boolean markSupported() {
	return true;
    }

    @Override
    public int available() throws IOException {
	return ((int) (length() - getPosition())) & Integer.MAX_VALUE;
    }

    public abstract long length();

    static class ClosedInputStream extends InputStream {

	@Override
	public int read() throws IOException {
	    return -1;
	}

	@Override
	public int read(byte[] b) throws IOException {
	    return -1;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
	    return -1;
	}

	@Override
	public long skip(long n) throws IOException {
	    return 0;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean markSupported() {
	    return false;
	}
    }
}
