package com.example.myvoiceassistant;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import es.dmoral.toasty.Toasty;

public class hotword extends Service implements RecognitionListener {

    public SpeechRecognizer recognizer;
    String comando="";

    public hotword() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toasty.info(getApplicationContext(),"Preparing the recognizer ",Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void run() {
                new AsyncTask<Void,Void,Exception>(){

                    @Override
                    protected Exception doInBackground(Void... voids) {
                        try {
                            setupRecognizer(new Assets( getApplicationContext()).syncAssets());
                            return null;
                        } catch (Exception e) {
                            return e;
                        }
                    }

                    @Override
                    protected void onPostExecute(Exception e) {
                        super.onPostExecute(e);
                        if (e != null) {
                            //Toast.makeText(getApplicationContext(),"Device not supporting Hotword Detection",Toast.LENGTH_SHORT).show();
                        } else {
                            Toasty.success(getApplicationContext(),"say \"jarvis\"",Toast.LENGTH_SHORT).show();
                            FireRecognition();
                        }

                    }
                } .execute(new Void[0]);
            }
        },1000);
    }
    public void FireRecognition() {
        try {
            recognizer.startListening("digits");
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "error "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void setupRecognizer(File assetsDir) {
        File modelsDir = new File(assetsDir, "models");
        try {
            recognizer = SpeechRecognizerSetup.defaultSetup().setAcousticModel(new File(modelsDir, "hmm/en-us-semi")).setDictionary(new File(modelsDir, "dict/cmu07a.dic")).setRawLogDir(assetsDir).setKeywordThreshold(1.0E-40f).getRecognizer();
            recognizer.addListener(this);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "models not found "+e.toString(), Toast.LENGTH_SHORT).show();
        }
        recognizer.addGrammarSearch("digits", new File(modelsDir, "grammar/digits.gram"));
    }
    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(Exception exc) {
        Toast.makeText(getApplicationContext(), "hotword detction not working on this device", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            comando = hypothesis.getHypstr();
            if (comando.indexOf("jarvis")!=-1 || comando.indexOf("juice")!=-1|| comando.indexOf("hello")!=-1) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if ( comando.contains("juice") ||  comando.contains("jarvis")|| comando.contains("hello"))
                        {
                            Toast.makeText(getApplicationContext(), "hotword detected", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK);
                            //intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something...");
                            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);

                            MainActivity.mySpeech.startListening(intent);
                            comando = "";
                            recognizer.stop();
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    FireRecognition();
                                }
                            }, 2000);
                        }
                    }
                }, 500);
            } else {
                timer();
            }
        }

    }

    public void timer() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                FireRecognition();
            }
        }, 50);
    }

    @Override
    public void onResult(Hypothesis hypothesis) {

    }

    @Override
    public void onTimeout() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
