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

package com.imagero.uio;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * @author Andrei Kouznetsov Date: 05.07.2004 Time: 15:02:01
 */
public class Sys {
    public static final PrintStream out = new PrintStreamFilter(java.lang.System.out);
    public static final PrintStream err = new PrintStreamFilter(java.lang.System.err);

    public static PrintStream getErr() {
	return err;
    }

    /**
     * Set PrintStream used for output. Set to null to disable output.
     * 
     * @param err
     *            PrintStream or null
     */
    public static void setErr(PrintStream err) {
	((PrintStreamFilter) Sys.err).setPrintStream(err);
    }
    
    public static void setErr(PrintWriter err) {
	((PrintStreamFilter) Sys.err).setPrintWriter(err);
    }

    public static PrintStream getOut() {
	return out;
    }

    /**
     * Set PrintStream used for output. Set to null to disable output.
     * 
     * @param out
     *            PrintStream or null
     */
    public static void setOut(PrintStream out) {
	((PrintStreamFilter) Sys.out).setPrintStream(out);
    }
    
    public static void setOut(PrintWriter out) {
	((PrintStreamFilter) Sys.out).setPrintWriter(out);
    }
    
    static class NullDevice extends OutputStream {

	@Override
	public void write(int b) throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
	}

	@Override
	public void write(byte[] b) throws IOException {
	}
    }

    public static class PrintStreamFilter extends PrintStream {
	PrintStream ps;
	PrintWriter pw;

	public PrintStreamFilter(PrintStream ps) {
	    super(ps);
	    this.ps = ps;
	}

	public PrintStreamFilter(PrintWriter pw) {
	    super(new NullDevice());
	    this.pw = pw;
	}

	public PrintStream getPrintStream() {
	    return ps;
	}

	public PrintWriter getPrintWriter() {
	    return pw;
	}

	public void setPrintStream(PrintStream ps) {
	    this.ps = ps;
	}
	
	public void setPrintWriter(PrintWriter pw) {
	    this.pw = pw;
	}

	public void print(String x) {
	    if (ps != null) {
		if (x == null) {
		    ps.print(x);
		    return;
		}
		char[] chars = new char[x.length()];
		x.getChars(0, x.length(), chars, 0);
		int p = chars.length;
		for (int j = 0; j < p; j++) {
		    char c = chars[j];
		    if (Character.isLetterOrDigit(c)) {
			chars[j] = c;
		    } else if (Character.isWhitespace(c)) {
			chars[j] = c;
		    } else if (Character.isISOControl(c)) {
			chars[j] = '.';
		    } else {
			chars[j] = c;
		    }
		}
		ps.print(chars);
	    }
	    if (pw != null) {
		if (x == null) {
		    pw.print(x);
		    return;
		}
		char[] chars = new char[x.length()];
		x.getChars(0, x.length(), chars, 0);
		int p = chars.length;
		for (int j = 0; j < p; j++) {
		    char c = chars[j];
		    if (Character.isLetterOrDigit(c)) {
			chars[j] = c;
		    } else if (Character.isWhitespace(c)) {
			chars[j] = c;
		    } else if (Character.isISOControl(c)) {
			chars[j] = '.';
		    } else {
			chars[j] = c;
		    }
		}
		pw.print(chars);
	    }
	}

	public void println(String x) {
	    print(x);
	    println();
	}

	public void println() {
	    if (ps != null) {
		ps.println();
	    }
	    if (pw != null) {
		pw.println();
	    }
	}

	public void flush() {
	    if (ps != null) {
		ps.flush();
	    }
	    if (pw != null) {
		pw.flush();
	    }
	}

	public void close() {
	    if (ps != null) {
		ps.close();
	    }
	    if (pw != null) {
		pw.close();
	    }
	}

	public boolean checkError() {
	    boolean error = false;
	    if (ps != null) {
		error = ps.checkError();
	    }
	    if (pw != null) {
		error |= pw.checkError();
	    }
	    return error;
	}

	public void write(int b) {
	    if (ps != null) {
		ps.write(b);
	    }
	    if (pw != null) {
		pw.write(b);
	    }
	}

	public void write(byte buf[], int off, int len) {
	    if (ps != null) {
		ps.write(buf, off, len);
	    }
	    if (pw != null) {
		pw.write(new String(buf, off, len));
	    }
	}

	public void print(boolean b) {
	    if (ps != null) {
		ps.print(b);
	    }
	    if (pw != null) {
		pw.print(b);
	    }
	}

	public void print(char c) {
	    if (ps != null) {
		ps.print(c);
	    }
	    if (pw != null) {
		pw.print(c);
	    }
	}

	public void print(int i) {
	    if (ps != null) {
		ps.print(i);
	    }
	    if (pw != null) {
		pw.print(i);
	    }
	}

	public void print(long l) {
	    if (ps != null) {
		ps.print(l);
	    }
	    if (pw != null) {
		pw.print(l);
	    }
	}

	public void print(float f) {
	    if (ps != null) {
		ps.print(f);
	    }
	    if (pw != null) {
		pw.print(f);
	    }
	}

	public void print(double d) {
	    if (ps != null) {
		ps.print(d);
	    }
	    if (pw != null) {
		pw.print(d);
	    }
	}

	public void print(char s[]) {
	    if (ps != null) {
		ps.print(s);
	    }
	    if (pw != null) {
		pw.print(s);
	    }
	}

	public void print(Object obj) {
	    if (ps != null) {
		ps.print(obj);
	    }
	    if (pw != null) {
		pw.print(obj);
	    }
	}

	public void println(boolean x) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void println(char x) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void println(int x) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void println(long x) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void println(float x) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void println(double x) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void println(char x[]) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void println(Object x) {
	    if (ps != null) {
		ps.println(x);
	    }
	    if (pw != null) {
		pw.println(x);
	    }
	}

	public void write(byte b[]) throws IOException {
	    write(b, 0, b.length);
	}

	public void println(Object[] objects) {
	    for (int i = 0; i < objects.length; i++) {
		print(objects[i]);
	    }
	    println();
	}

	public void println(Object[] objects, String delimiter) {
	    for (int i = 0; i < objects.length; i++) {
		print(objects[i]);
		print(delimiter);
	    }
	    println();
	}

	public void println(long[] longs, String delimiter) {
	    for (int i = 0; i < longs.length; i++) {
		print(longs[i]);
		print(delimiter);
	    }
	    println();
	}

	public void println(int[] numbers, String delimiter) {
	    for (int i = 0; i < numbers.length; i++) {
		print(numbers[i]);
		print(delimiter);
	    }
	    println();
	}

	public void println(char[] chars, String delimiter) {
	    for (int i = 0; i < chars.length; i++) {
		print(chars[i]);
		print(delimiter);
	    }
	    println();
	}

	public void println(short[] shorts, String delimiter) {
	    for (int i = 0; i < shorts.length; i++) {
		print(shorts[i]);
		print(delimiter);
	    }
	    println();
	}

	public void println(byte[] bytes, String delimiter) {
	    for (int i = 0; i < bytes.length; i++) {
		print(bytes[i]);
		print(delimiter);
	    }
	    println();
	}
    }
}
