package com.imagero.uio.bio.content;

import com.imagero.java.util.Debug;
import com.imagero.uio.io.IOutils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

/**
 * BlobContent - Blob as source for UIO Stream.
 * 
 * @author Andrey Kuznetsov
 */
public class BlobContent extends StreamContent {

    Blob blob;

    public BlobContent(Blob blob) {
	this.blob = blob;
    }

    public int load(long offset, int bpos, byte[] buffer) throws IOException {
	InputStream in = null;
	int count = 0;
	try {
	    in = blob.getBinaryStream(offset, buffer.length);
	    while (count < buffer.length) {
		buffer[count++] = (byte) in.read();
	    }
	} catch (Exception ex) {
	    Debug.print(ex);
	} finally {
	    IOutils.closeStream(in);
	}
	return count;
    }

    public void save(long offset, int bpos, byte[] buffer, int length) throws IOException {
    }

    public long length() throws IOException {
	try {
	    return blob.length();
	} catch (SQLException ex) {
	    throw new IOException(ex);
	}
    }

    public void close() {
    }

    public boolean canReload() {
	return true;
    }

    public boolean writable() {
	return false;
    }
}
