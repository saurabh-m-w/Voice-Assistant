package edu.cmu.pocketsphinx;

import java.util.Iterator;

public class JsgfIterator implements Iterator<JsgfRule> {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected JsgfIterator(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    protected static long getCPtr(JsgfIterator obj) {
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
                SphinxBaseJNI.delete_JsgfIterator(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public JsgfIterator(SWIGTYPE_p_void ptr) {
        this(SphinxBaseJNI.new_JsgfIterator(SWIGTYPE_p_void.getCPtr(ptr)), true);
    }

    public JsgfRule next() {
        long cPtr = SphinxBaseJNI.JsgfIterator_next(this.swigCPtr, this);
        if (cPtr == 0) {
            return null;
        }
        return new JsgfRule(cPtr, true);
    }

    public boolean hasNext() {
        return SphinxBaseJNI.JsgfIterator_hasNext(this.swigCPtr, this);
    }
}
