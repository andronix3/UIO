package com.imagero.uio.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.imagero.java.event.IActionEvent;
import com.imagero.java.event.IActionListener;

public class BlockingIOBuffer {

    InputStream in;

    int bufferSize;

    byte[] buffer;

    ByteArrayOutputStreamExt out;
    ByteArrayInputStream bais;

    public BlockingIOBuffer(int bufferSize) {
	this.bufferSize = bufferSize;
	this.buffer = new byte[bufferSize * 2];
	out = new ByteArrayOutputStreamExt(bufferSize, new IActionListener() {
	    public void actionPerformed(IActionEvent e) {

	    }
	});
    }

    static class BAInputStream extends ByteArrayInputStream {

	public BAInputStream(byte[] buf) {
	    super(buf);
	}

	synchronized int copyFromBuffer(byte[] buffer) {
	    if (pos > 0) {
		int length = available();
		System.arraycopy(buf, pos, buf, 0, length);
		count -= pos;
		pos = 0;
	    }
	    int toCopy = Math.min(buffer.length, buf.length - count);
	    System.arraycopy(buffer, 0, buffer, count, toCopy);
	    
	    return toCopy;
	}
    }
}
