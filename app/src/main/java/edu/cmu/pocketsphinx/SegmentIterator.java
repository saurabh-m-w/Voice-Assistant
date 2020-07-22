package edu.cmu.pocketsphinx;

import java.util.Iterator;

public class SegmentIterator implements Iterator<Segment> {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected SegmentIterator(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    protected static long getCPtr(SegmentIterator obj) {
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
                PocketSphinxJNI.delete_SegmentIterator(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public SegmentIterator(SWIGTYPE_p_void ptr) {
        this(PocketSphinxJNI.new_SegmentIterator(SWIGTYPE_p_void.getCPtr(ptr)), true);
    }

    public Segment next() {
        long cPtr = PocketSphinxJNI.SegmentIterator_next(this.swigCPtr, this);
        if (cPtr == 0) {
            return null;
        }
        return new Segment(cPtr, true);
    }

    public boolean hasNext() {
        return PocketSphinxJNI.SegmentIterator_hasNext(this.swigCPtr, this);
    }
}
