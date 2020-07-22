package edu.cmu.pocketsphinx;

import java.util.Iterator;

public class NBestIterator implements Iterator<NBest> {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected NBestIterator(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    protected static long getCPtr(NBestIterator obj) {
        if (obj == null) {
            return 0;
        }
        return obj.swigCPtr;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                PocketSphinxJNI.delete_NBestIterator(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public NBestIterator(SWIGTYPE_p_void ptr) {
        this(PocketSphinxJNI.new_NBestIterator(SWIGTYPE_p_void.getCPtr(ptr)), true);
    }

    public NBest next() {
        long cPtr = PocketSphinxJNI.NBestIterator_next(this.swigCPtr, this);
        if (cPtr == 0) {
            return null;
        }
        return new NBest(cPtr, true);
    }

    public boolean hasNext() {
        return PocketSphinxJNI.NBestIterator_hasNext(this.swigCPtr, this);
    }
}
