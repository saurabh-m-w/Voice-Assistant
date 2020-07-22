package edu.cmu.pocketsphinx;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class SpeechRecognizer {
    private static final float BUFFER_SIZE_SECONDS = 0.4f;
    protected static final String TAG = SpeechRecognizer.class.getSimpleName();
    /* access modifiers changed from: private */
    public int bufferSize;
    /* access modifiers changed from: private */
    public final Decoder decoder;
    /* access modifiers changed from: private */
    public final Collection<RecognitionListener> listeners = new HashSet();
    /* access modifiers changed from: private */
    public final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Thread recognizerThread;
    /* access modifiers changed from: private */
    public final AudioRecord recorder;
    /* access modifiers changed from: private */
    public final int sampleRate;

    private class InSpeechChangeEvent extends RecognitionEvent {
        private final boolean state;

        InSpeechChangeEvent(boolean state2) {
            super();
            this.state = state2;
        }

        /* access modifiers changed from: protected */
        public void execute(RecognitionListener listener) {
            if (this.state) {
                listener.onBeginningOfSpeech();
            } else {
                listener.onEndOfSpeech();
            }
        }
    }

    private class OnErrorEvent extends RecognitionEvent {
        private final Exception exception;

        OnErrorEvent(Exception exception2) {
            super();
            this.exception = exception2;
        }

        /* access modifiers changed from: protected */
        public void execute(RecognitionListener listener) {
            listener.onError(this.exception);
        }
    }

    private abstract class RecognitionEvent implements Runnable {
        /* access modifiers changed from: protected */
        public abstract void execute(RecognitionListener recognitionListener);

        private RecognitionEvent() {
        }

        public void run() {
            for (RecognitionListener listener : (RecognitionListener[]) SpeechRecognizer.this.listeners.toArray(new RecognitionListener[0])) {
                execute(listener);
            }
        }
    }

    private final class RecognizerThread extends Thread {
        private static final int NO_TIMEOUT = -1;
        private int remainingSamples;
        private int timeoutSamples;

        public RecognizerThread(int timeout) {
            if (timeout != -1) {
                this.timeoutSamples = (SpeechRecognizer.this.sampleRate * timeout) / 1000;
            } else {
                this.timeoutSamples = -1;
            }
            this.remainingSamples = this.timeoutSamples;
        }

        public RecognizerThread(SpeechRecognizer speechRecognizer) {
            this(-1);
        }

        public void run() {
            SpeechRecognizer.this.recorder.startRecording();
            if (SpeechRecognizer.this.recorder.getRecordingState() == 1) {
                SpeechRecognizer.this.recorder.stop();
                SpeechRecognizer.this.mainHandler.post(new OnErrorEvent(new IOException("Failed to start recording. Microphone might be already in use.")));
                return;
            }
            Log.d(SpeechRecognizer.TAG, "Starting decoding");
            SpeechRecognizer.this.decoder.startUtt();
            short[] buffer = new short[SpeechRecognizer.this.bufferSize];
            boolean inSpeech = SpeechRecognizer.this.decoder.getInSpeech();
            SpeechRecognizer.this.recorder.read(buffer, 0, buffer.length);
            while (!interrupted() && (this.timeoutSamples == -1 || this.remainingSamples > 0)) {
                int nread = SpeechRecognizer.this.recorder.read(buffer, 0, buffer.length);
                if (-1 == nread) {
                    throw new RuntimeException("error reading audio buffer");
                }
                if (nread > 0) {
                    SpeechRecognizer.this.decoder.processRaw(buffer, (long) nread, false, false);
                    if (SpeechRecognizer.this.decoder.getInSpeech() != inSpeech) {
                        inSpeech = SpeechRecognizer.this.decoder.getInSpeech();
                        SpeechRecognizer.this.mainHandler.post(new InSpeechChangeEvent(inSpeech));
                    }
                    if (inSpeech) {
                        this.remainingSamples = this.timeoutSamples;
                    }
                    SpeechRecognizer.this.mainHandler.post(new ResultEvent(SpeechRecognizer.this.decoder.hyp(), false));
                }
                if (this.timeoutSamples != -1) {
                    this.remainingSamples -= nread;
                }
            }
            SpeechRecognizer.this.recorder.stop();
            SpeechRecognizer.this.decoder.endUtt();
            SpeechRecognizer.this.mainHandler.removeCallbacksAndMessages(null);
            if (this.timeoutSamples != -1 && this.remainingSamples <= 0) {
                SpeechRecognizer.this.mainHandler.post(new TimeoutEvent());
            }
        }
    }

    private class ResultEvent extends RecognitionEvent {
        private final boolean finalResult;
        protected final Hypothesis hypothesis;

        ResultEvent(Hypothesis hypothesis2, boolean finalResult2) {
            super();
            this.hypothesis = hypothesis2;
            this.finalResult = finalResult2;
        }

        /* access modifiers changed from: protected */
        public void execute(RecognitionListener listener) {
            if (this.finalResult) {
                listener.onResult(this.hypothesis);
            } else {
                listener.onPartialResult(this.hypothesis);
            }
        }
    }

    private class TimeoutEvent extends RecognitionEvent {
        private TimeoutEvent() {
            super();
        }

        /* access modifiers changed from: protected */
        public void execute(RecognitionListener listener) {
            listener.onTimeout();
        }
    }

    protected SpeechRecognizer(Config config) throws IOException {
        this.decoder = new Decoder(config);
        this.sampleRate = (int) this.decoder.getConfig().getFloat("-samprate");
        this.bufferSize = Math.round(((float) this.sampleRate) * BUFFER_SIZE_SECONDS);
        this.recorder = new AudioRecord(6, this.sampleRate, 16, 2, this.bufferSize * 2);
        if (this.recorder.getState() == 0) {
            this.recorder.release();
            throw new IOException("Failed to initialize recorder. Microphone might be already in use.");
        }
    }

    public void addListener(RecognitionListener listener) {
        synchronized (this.listeners) {
            this.listeners.add(listener);
        }
    }

    public void removeListener(RecognitionListener listener) {
        synchronized (this.listeners) {
            this.listeners.remove(listener);
        }
    }

    public boolean startListening(String searchName) {
        if (this.recognizerThread != null) {
            return false;
        }
        Log.i(TAG, String.format("Start recognition \"%s\"", new Object[]{searchName}));
        this.decoder.setSearch(searchName);
        this.recognizerThread = new RecognizerThread(this);
        this.recognizerThread.start();
        return true;
    }

    public boolean startListening(String searchName, int timeout) {
        if (this.recognizerThread != null) {
            return false;
        }
        Log.i(TAG, String.format("Start recognition \"%s\"", new Object[]{searchName}));
        this.decoder.setSearch(searchName);
        this.recognizerThread = new RecognizerThread(timeout);
        this.recognizerThread.start();
        return true;
    }

    private boolean stopRecognizerThread() {
        if (this.recognizerThread == null) {
            return false;
        }
        try {
            this.recognizerThread.interrupt();
            this.recognizerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.recognizerThread = null;
        return true;
    }

    public boolean stop() {
        boolean result = stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Stop recognition");
            this.mainHandler.post(new ResultEvent(this.decoder.hyp(), true));
        }
        return result;
    }

    public boolean cancel() {
        boolean result = stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Cancel recognition");
        }
        return result;
    }

    public Decoder getDecoder() {
        return this.decoder;
    }

    public void shutdown() {
        this.recorder.release();
    }

    public String getSearchName() {
        return this.decoder.getSearch();
    }

    public void addFsgSearch(String searchName, FsgModel fsgModel) {
        this.decoder.setFsg(searchName, fsgModel);
    }

    public void addGrammarSearch(String name, File file) {
        Log.i(TAG, String.format("Load JSGF %s", new Object[]{file}));
        this.decoder.setJsgfFile(name, file.getPath());
    }

    public void addGrammarSearch(String name, String jsgfString) {
        this.decoder.setJsgfString(name, jsgfString);
    }

    public void addNgramSearch(String name, File file) {
        Log.i(TAG, String.format("Load N-gram model %s", new Object[]{file}));
        this.decoder.setLmFile(name, file.getPath());
    }

    public void addKeyphraseSearch(String name, String phrase) {
        this.decoder.setKeyphrase(name, phrase);
    }

    public void addKeywordSearch(String name, File file) {
        this.decoder.setKws(name, file.getPath());
    }

    public void addAllphoneSearch(String name, File file) {
        this.decoder.setAllphoneFile(name, file.getPath());
    }
}
