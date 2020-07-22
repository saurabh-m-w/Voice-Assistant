package edu.cmu.pocketsphinx;

import java.util.Iterator;

public class NGramModelSetIterator implements Iterator<NGramModel> {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected NGramModelSetIterator(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    protected static long getCPtr(NGramModelSetIterator obj) {
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
                SphinxBaseJNI.delete_NGramModelSetIterator(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public NGramModelSetIterator(SWIGTYPE_p_void ptr) {
        this(SphinxBaseJNI.new_NGramModelSetIterator(SWIGTYPE_p_void.getCPtr(ptr)), true);
    }

    public NGramModel next() {
        long cPtr = SphinxBaseJNI.NGramModelSetIterator_next(this.swigCPtr, this);
        if (cPtr == 0) {
            return null;
        }
        return new NGramModel(cPtr, true);
    }

    public boolean hasNext() {
        return SphinxBaseJNI.NGramModelSetIterator_hasNext(this.swigCPtr, this);
    }
}
