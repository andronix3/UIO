package com.imagero.uio.bio;

/**
 * Index of Object in 2D array Date: 14.12.2007
 *
 * @author Andrey Kuznetsov
 */
class BufferIndex {
    /**
     * index of array in 2D array
     */
    int arrayIndex;
    /**
     * index of object in 1D array
     */
    int index;

    /**
     * @param arrayIndex
     *            index of array
     * @param index
     *            index of object
     */
    public BufferIndex(int arrayIndex, int index) {
	this.arrayIndex = arrayIndex;
	this.index = index;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + arrayIndex;
	result = prime * result + index;
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	BufferIndex other = (BufferIndex) obj;
	if (arrayIndex != other.arrayIndex)
	    return false;
	if (index != other.index)
	    return false;
	return true;
    }
}
