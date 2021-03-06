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
package com.imagero.uio.bio.content;

import com.imagero.uio.impl.RandomAccessFileX;
import com.imagero.uio.io.IOutils;
import com.imagero.uio.io.UnexpectedEOFException;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.io.EOFException;

/**
 * Content with access to one or more predefined areas in File or
 * RandomAccessFile. Length can not be changed. UnexpectedEOFException is thrown
 * if we try to write outside our length.
 *
 * Date: 05.01.2008
 *
 * @author Andrey Kuznetsov
 */
public class SpannedRandomAccessFileContent extends StreamContent {
	private RandomAccessFile raf;

	private Span[] spans;
	private long length;
	private boolean closed;

	public SpannedRandomAccessFileContent(File f, Span[] spans) throws IOException {
		this(f, getMode(f), spans);
	}

	public SpannedRandomAccessFileContent(File f, String mode, Span[] spans) throws IOException {
		this(new RandomAccessFileX(f, mode), spans);

	}

	static String getMode(File f) {
		if (!f.exists() || f.canWrite()) {
			return "rw";
		}
		return "r";
	}

	public SpannedRandomAccessFileContent(RandomAccessFile raf, Span[] spans) throws IOException {
		this.raf = raf;
		this.spans = spans;
		long rafLength = raf.length();
		for (int i = 0; i < spans.length; i++) {
			Span span = spans[i];
			if (span.offset > rafLength || span.offset + span.length > rafLength) {
				throw new IOException("Illegal span: " + span.offset + " " + span.length);
			}
		}
		for (int i = 0; i < spans.length; i++) {
			length += spans[i].length;
		}
	}

	Span currentSpan;
	long spanOffset;

	void seek(long offset) throws IOException {
		int sp = 0;
		while (offset > 0) {
			Span span = spans[sp++];
			if (offset < span.length) {
				currentSpan = span;
				spanOffset = offset;
				raf.seek(span.offset + offset);
				return;
			}
			offset -= span.length;
		}
	}

	public int load(long offset, int bpos, byte[] b) throws IOException {
		seek(offset);

		long available = currentSpan.length - spanOffset;
		int len = (int) Math.min(available, b.length - bpos);
		if (len > 0) {
			raf.readFully(b, bpos, len);
			return len;
		}
		throw new EOFException();
	}

	public boolean canReload() {
		return true;
	}

	public void close() {
		IOutils.closeStream(raf);
	}

	public boolean writable() {
		return true;
	}

	public void save(long offset, int bpos, byte[] buffer, int length) throws IOException {
		long len = length;
		while (len > 0) {
			seek(offset);
			long available = currentSpan.length - spanOffset;
			int w = (int) Math.min(available, len);
			if (w == 0) {
				throw new UnexpectedEOFException(length - len);
			}
			raf.write(buffer, bpos, w);
			offset += w;
			bpos += w;
			len -= w;
		}
	}

	public long length() throws IOException {
		return length;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		raf = null;
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}
}
