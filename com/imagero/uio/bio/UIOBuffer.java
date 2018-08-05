package com.imagero.uio.bio;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface UIOBuffer {

	int read(BufferPosition position);

	long skip(long n, BufferPosition position);

	BufferPosition createPosition();

	int availableForReading(BufferPosition position);

	int availableForWriting(BufferPosition position);

	int read(byte[] dest, int offset, int length, BufferPosition position);

	/**
	 * write given byte to buffer.
	 * 
	 * @param b
	 *            int to write
	 */
	void write(int b, BufferPosition position);

	int getCount();

	int getPosition(BufferPosition position);

	/**
	 * write buffer contents to OutputStream
	 * 
	 * @param wholeBuffer
	 *            if true then whole buffer is written, otherwise only getCount()
	 *            bytes are written
	 */
	void writeBuffer(OutputStream out, boolean wholeBuffer) throws IOException;

	void writeBuffer(DataOutput out, boolean wholeBuffer) throws IOException;

	/**
	 * write whole buffer contents to OutputStream (count is ignored)
	 */
	void writeBuffer(OutputStream out) throws IOException;

	void writeBuffer(DataOutput out) throws IOException;

	int write(byte src[], int offset, int length, BufferPosition position);

	default int read(ByteBuffer dst, BufferPosition position) {
		int TRANSFER_SIZE = 8192;
		int len = dst.remaining();
		int totalRead = 0;
		int bytesRead = 0;

		byte[] buf = new byte[TRANSFER_SIZE];

		synchronized (this) {
			int available = availableForReading(position);
			while (totalRead < len && (available = availableForReading(position)) > 0) {
				int bytesToRead = Math.min((len - totalRead), TRANSFER_SIZE);
				bytesToRead = Math.min(bytesRead, available);
				bytesRead = read(buf, 0, bytesToRead, position);
				if (bytesRead < 0) {
					break;
				} else {
					totalRead += bytesRead;
				}
				dst.put(buf, 0, bytesRead);
			}
		}
		if ((bytesRead < 0) && (totalRead == 0)) {
			return -1;
		}
		return totalRead;
	}

	default int write(ByteBuffer src, BufferPosition position) throws IOException {
		int TRANSFER_SIZE = 8192;

		int len = src.remaining();
		int totalWritten = 0;

		byte[] buf = new byte[TRANSFER_SIZE];

		synchronized (this) {
			while (totalWritten < len) {
				int bytesToWrite = Math.min((len - totalWritten), TRANSFER_SIZE);
				src.get(buf, 0, bytesToWrite);
				write(buf, 0, bytesToWrite, position);
				totalWritten += bytesToWrite;
			}
			return totalWritten;
		}
	}

}