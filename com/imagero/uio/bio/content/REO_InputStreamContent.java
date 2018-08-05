package com.imagero.uio.bio.content;

import java.io.IOException;

import com.imagero.uio.io.IOutils;
import com.imagero.uio.io.REO_InputStream;

public class REO_InputStreamContent extends StreamContent {

	private REO_InputStream in;
	private boolean closed;

	public REO_InputStreamContent(REO_InputStream in) {
		this.in = in;
	}

	@Override
	public int load(long offset, int bpos, byte[] buffer) throws IOException {
		in.seek(offset);
		return in.read(buffer, bpos, buffer.length - bpos);
	}

	@Override
	public void save(long offset, int bpos, byte[] buffer, int length) throws IOException {

	}

	@Override
	public long length() throws IOException {
		return in.length();
	}

	@Override
	public void close() {
		closed = true;
		IOutils.closeStream(in);
	}

	@Override
	public boolean canReload() {
		return true;
	}

	@Override
	public boolean writable() {
		return false;
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

}
