package com.imagero.uio;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;

import com.smartg.java.util.StackTraceUtil;

public class UIOSeekableByteChannel implements SeekableByteChannel {

	private RandomAccessInput rai;
	private ReadableByteChannel channel;
	private InputStream in;
	private long size;
	private long offset;

	public UIOSeekableByteChannel(RandomAccessInput rai, long offset, long length) {
		this.rai = rai;
		this.size = length;
		this.offset = offset;
		if (length > 0) {
			in = rai.createInputStream(offset, length);
		} else {
			in = rai.createInputStream(offset);
		}
		channel = Channels.newChannel(in);
	}

	@Override
	public boolean isOpen() {
		return channel.isOpen();
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return channel.read(dst);
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		if(rai instanceof RandomAccessOutput) {
			
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public long position() throws IOException {
		return rai.getChildOffset(in);
	}

	@Override
	public SeekableByteChannel position(long position) throws IOException {
		rai.setChildPosition(in, position);
		return this;
	}

	@Override
	public long size() throws IOException {
		return size > 0? size: rai.length() - offset;
	}

	@Override
	public SeekableByteChannel truncate(long size) throws IOException {
		if(rai instanceof RandomAccessOutput) {
			((RandomAccessOutput) rai).setLength(size);
		}
		else {
			StackTraceUtil.warning("Unsupported Opration: truncate()");
		}
		return this;
	}

}
