package com.imagero.uio.bio;


/**
 * Ring - minimalistic Ring implementation.
 *
 * Date: 29.08.2007
 * @author Andrey Kuznetsov
 */
class Ring <E>{

    E[] elements;

    int size;
    int index;

    @SuppressWarnings("unchecked")
    public Ring(int size) {
        this.size = size;        
        this.elements = (E[]) new Object[size];
    }

    /**
     * add Object to ring
     * @param o Object
     * @return Object removed from ring (replaced by new Object) or null
     */
    public E add(E o) {
        E tmp = elements[index];
        elements[index] = o;
        index = (index + 1) % size;
        return tmp;
    }
}
