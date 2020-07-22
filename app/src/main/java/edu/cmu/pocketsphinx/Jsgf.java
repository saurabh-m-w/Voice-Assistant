package edu.cmu.pocketsphinx;

public class Jsgf implements Iterable<JsgfRule> {
    protected transient boolean swigCMemOwn;
    private transient long swigCPtr;

    protected Jsgf(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    protected static long getCPtr(Jsgf obj) {
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
                SphinxBaseJNI.delete_Jsgf(this.swigCPtr);
            }
            this.swigCPtr = 0;
        }
    }

    public JsgfIterator iterator() {
        long cPtr = SphinxBaseJNI.Jsgf_iterator(this.swigCPtr, this);
        if (cPtr == 0) {
            return null;
        }
        return new JsgfIterator(cPtr, true);
    }

    public Jsgf(String path) {
        this(SphinxBaseJNI.new_Jsgf(path), true);
    }

    public String getName() {
        return SphinxBaseJNI.Jsgf_getName(this.swigCPtr, this);
    }

    public JsgfRule getRule(String name) {
        long cPtr = SphinxBaseJNI.Jsgf_getRule(this.swigCPtr, this, name);
        if (cPtr == 0) {
            return null;
        }
        return new JsgfRule(cPtr, false);
    }

    public FsgModel buildFsg(JsgfRule rule, LogMath logmath, float lw) {
        long cPtr = SphinxBaseJNI.Jsgf_buildFsg(this.swigCPtr, this, JsgfRule.getCPtr(rule), rule, LogMath.getCPtr(logmath), logmath, lw);
        if (cPtr == 0) {
            return null;
        }
        return new FsgModel(cPtr, false);
    }
}
