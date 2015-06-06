package com.imagero.uio.io;

import java.io.PrintStream;

import com.imagero.uio.Sys;

public class DumpUtil {
    
    private PrintStream out = Sys.out;
    
    public DumpUtil() {
	this(Sys.out);
    }

    public DumpUtil(PrintStream out) {
	this.out = out;
    }

    public void printHexByte(int value) {
        printHexImpl(value & 0xFFFF, 2);
    }

    public void printlnHexByte(int value) {
        printHexImpl(value & 0xFFFF, 2);
        out.println("");
    }

    public void printHexShort(int value) {
        printHexImpl(value & 0xFFFF, 4);
    }

    public void printlnHexShort(int value) {
        printHexImpl(value & 0xFFFF, 4);
        out.println("");
    }

    public void printHexInt(int value) {
        printHexImpl(value & 0xFFFFFFFF, 8);
    }

    public void printlnHexInt(int value) {
        printHexImpl(value & 0xFFFFFFFF, 8);
        out.println("");
    }

    public void printHexLong(long value) {
        printHexImpl(value & 0xFFFFFFFFFFFFFFFFL, 16);
    }

    public void printlnHexLong(long value) {
        printHexImpl(value & 0xFFFFFFFFFFFFFFFFL, 16);
        out.println("");
    }

    void printHexImpl(long value, int length) {
        String s = Long.toHexString(value);
        // out.println("***********************" + s + " " + value);
        for (int i = 0, size = length - s.length(); i < size; i++) {
            out.print("0");
        }
        out.print(s);
    }

    void printHexImpl(int value, int length) {
        String s = Integer.toHexString(value);
        if (s.length() > length) {
            s = s.substring(s.length() - length);
        }
        // out.println("***********************" + s + " " + value);
        for (int i = 0, size = length - s.length(); i < size; i++) {
            out.print("0");
        }
        out.print(s);
    }
}
