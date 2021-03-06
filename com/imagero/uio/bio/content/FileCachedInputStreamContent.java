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

import com.imagero.uio.impl.TmpRandomAccessFile;
import com.imagero.uio.io.IOutils;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;

/**
 * Date: 05.01.2008
 * 
 * @author Andrey Kuznetsov
 */
public class FileCachedInputStreamContent extends StreamContent {
	private InputStream in;
	private File tmp;
	private TmpRandomAccessFile tmpRaf;
	private boolean closed;

	public FileCachedInputStreamContent(InputStream in, File tmp) throws IOException {
		this.in = in;
		this.tmp = tmp;
		tmpRaf = new TmpRandomAccessFile(tmp, "rw");
	}

	public int load(long offset, int bpos, byte[] buffer) throws IOException {
		long length = tmpRaf.length();
		long nl = offset + buffer.length - bpos;
		if (length < nl) {
			tmpRaf.seek(length);
			IOutils.copy(nl - length, in, tmpRaf);
		}
		tmpRaf.seek(offset);
		length = tmpRaf.length();
		try {
			long max = Math.min(length - offset, buffer.length - bpos);
			int imax = (int) max;
			imax = imax & Integer.MAX_VALUE;
			tmpRaf.readFully(buffer, bpos, imax);
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		return buffer.length - bpos;
	}

	public void save(long offset, int bpos, byte[] buffer, int length) throws IOException {
	}

	public long length() throws IOException {
		return tmp.length() + in.available();
	}


	public void close() {
		closed = true;
		IOutils.closeStream(in);
		IOutils.closeStream(tmpRaf);
	}

	protected void finalize() throws Throwable {
		super.finalize();
		IOutils.closeStream(tmpRaf);
		tmpRaf = null;
	}

	public boolean canReload() {
		return true;
	}

	public boolean writable() {
		return false;
	}

	@Override
	public boolean isOpen() {
		return !closed;
	}

}
