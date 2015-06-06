package com.imagero.uio.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class CountInputStream extends PushbackInputStream {

    long count;
    long markCount;
    
    public CountInputStream(InputStream in) {
	super(in);
    }
    
    public CountInputStream(InputStream in, long count) {
	super(in);
	this.count = count;
    }

    public int read() throws IOException {
	count++;
	return super.read();
    }

    public int read(byte[] b, int off, int len) throws IOException {
	int read = super.read(b, off, len);
	count += read;
	return read;
    }

    public int read(byte[] b) throws IOException {
	return read(b, 0, b.length);
    }

    public synchronized void reset() throws IOException {
	super.reset();
	count = markCount;
    }

    public synchronized void mark(int readlimit) {
	super.mark(readlimit);
	markCount = count;
    }

    public long skip(long n) throws IOException {
	long skip = super.skip(n);
	count += skip;
	return skip;
    }
    
    public void unread(int b) throws IOException {
	count--;
	super.unread(b);
    }
    
    public void unread(byte[] b, int off, int len) throws IOException {
	count-= len;
	super.unread(b, off, len);
    }

    public void unread(byte[] b) throws IOException {
	unread(b, 0, b.length);
    }

    public long getCount() {
        return count;
    }
}
