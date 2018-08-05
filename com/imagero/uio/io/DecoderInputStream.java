package com.imagero.uio.io;

import com.imagero.java.event.IActionListener;
import com.imagero.java.event.IActionEvent;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Date: 19.07.2009
 *
 * Default processing for Encoded Streams.
 * 
 * @author Andrey Kuznetsov
 */
public abstract class DecoderInputStream extends FilterInputStream {
	private byte[] buffer;
	private ByteArrayInputStreamX bais;
	protected final ByteArrayOutputStreamExt out;
	protected boolean checkDrain = false;

	boolean finished;

	public DecoderInputStream(InputStream in) {
		this(in, 2048);
	}

	public DecoderInputStream(InputStream in, int bufferSize) {
		super(in);
		buffer = new byte[bufferSize];
		bais = new ByteArrayInputStreamX(buffer);
		out = create();
	}

	public final int read() throws IOException {
		if (finished) {
			return -1;
		}
		if (bais.available() <= 0) {
			decode0();
		}
		return bais.read();
	}

	public int read(byte b[]) throws IOException {
		if (finished) {
			return -1;
		}
		return read(b, 0, b.length);
	}

	public int read(byte b[], int off, int len) throws IOException {
		if (finished) {
			return -1;
		}
		if (bais.available() <= 0) {
			decode0();
		}
		return bais.read(b, off, len);
	}

	private void decode0() throws IOException {
		finished = decode();
		// if(/*checkDrain &&*/ (!out.drained || bais.available() == 0)) {
		if (bais.available() == 0) {
//			if (!out.isDrained()) {
				bufferFull();
			}
//		}
	}

	protected abstract boolean decode() throws IOException;

	protected ByteArrayOutputStreamExt create() {
		return new ByteArrayOutputStreamExt(buffer.length, new IActionListener() {
			public void actionPerformed(IActionEvent e) {
				bufferFull();
			}
		});
	}

	/**
	 * implements default action (drain) when buffer is full
	 */
	protected void bufferFull() {
		beforeDrain();
		drain();
		afterDrain();
	}

	/**
	 * can be used to set some variables just before buffer draining
	 */
	protected void beforeDrain() {
	};

	/**
	 * can be used to set some variables just after buffer draining
	 */
	protected void afterDrain() {
	};

	/**
	 * drain buffer and reinit input
	 */
	protected void drain() {
		int length = out.drain(buffer);
		bais.setPos(0);
		bais.setLength(length);
	}

	/**
	 * We need direct access to pos and count variables of ByteArrayInputStream. One
	 * wise man (Arthur van Hoff) made them protected, not private! Thanks Arthur!
	 */
	private static class ByteArrayInputStreamX extends ByteArrayInputStream {
		public ByteArrayInputStreamX(byte[] buffer) {
			super(buffer);
			count = 0;
		}

		public void setLength(int length) {
			count = length;
		}

		public void setPos(int pos) {
			this.pos = pos;
		}
	}
}
