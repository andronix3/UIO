package com.imagero.uio;

public enum ByteOrder {
	BIG_ENDIAN(0x4D4D), LITTLE_ENDIAN(0x4949);

	private final int endian;
	private final boolean bigEndian;

	private ByteOrder(int endian) {
		this.endian = endian;
		this.bigEndian = endian == 0x4D4D;
	}

	public int getEndian() {
		return endian;
	}

	public boolean isBigEndian() {
		return bigEndian;
	}

	public static ByteOrder valueOf(int endian) {
		switch (endian) {
		case 0x4D4D:
			return ByteOrder.BIG_ENDIAN;
		case 0x4949:
			return ByteOrder.LITTLE_ENDIAN;
		default:
			throw new AssertionError();
		}
	}
}