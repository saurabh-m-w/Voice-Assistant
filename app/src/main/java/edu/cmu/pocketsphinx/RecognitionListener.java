package edu.cmu.pocketsphinx;

public interface RecognitionListener {
    void onBeginningOfSpeech();

    void onEndOfSpeech();

    void onError(Exception exc);

    void onPartialResult(Hypothesis hypothesis);

    void onResult(Hypothesis hypothesis);

    void onTimeout();
}
