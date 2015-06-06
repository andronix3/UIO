package com.imagero.uio.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class REO_FileInputStream extends REO_InputStream {
    File file;

    public REO_FileInputStream(File file) throws IOException {
	this.file = file;
	reopen();
    }

    @Override
    protected InputStream reopenImpl() throws IOException {
	return new FileInputStream(file);
    }

    @Override
    public String toString() {
	return file.getAbsolutePath();
    }

    @Override
    public long length() {
	return file.length();
    }
}
