package edu.cmu.pocketsphinx;

public class FrontEnd {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected FrontEnd(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    protected static long getCPtr(FrontEnd obj) {
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
                SphinxBaseJNI.delete_FrontEnd(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public FrontEnd() {
        this(SphinxBaseJNI.new_FrontEnd(), true);
    }

    public int outputSize() {
        return SphinxBaseJNI.FrontEnd_outputSize(this.swigCPtr, this);
    }

    public int processUtt(String spch, long nsamps, SWIGTYPE_p_p_p_mfcc_t cep_block) {
        return SphinxBaseJNI.FrontEnd_processUtt(this.swigCPtr, this, spch, nsamps, SWIGTYPE_p_p_p_mfcc_t.getCPtr(cep_block));
    }
}
