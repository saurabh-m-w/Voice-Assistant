package com.example.myvoiceassistant;

import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.Locale;

public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    float x1,x2,y1,y2;
    private TextToSpeech myTTS;
    ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intializeTextToSpeech();
        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
    }



    @Override
    public void handleResult(Result result) {
        //String msg1 = result.getText();
        //speak(msg1);
        //MainActivity.resulttextView.setText(result.getText());
        //onBackPressed();

        String msg1 = result.getText();
        Intent intent=new Intent();
        intent.putExtra("MESSAGE",msg1);
        setResult(2,intent);
        finish();//finishing activity
    }


    private void intializeTextToSpeech() {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0) {

                    Toast.makeText(ScanCodeActivity.this, "No Tts Engine", Toast.LENGTH_SHORT).show();
                    finish();;
                }
                else
                {
                    myTTS.setLanguage(Locale.UK) ;
                    speak1("QR code Scanner Started");
                }

            }
        });
    }


    public void speak1(String msg) {
        if(Build.VERSION.SDK_INT>=21)
        {
            myTTS.speak(msg,TextToSpeech.QUEUE_FLUSH,null,null);
        }else
        {
            myTTS.speak(msg,TextToSpeech.QUEUE_FLUSH,null);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        myTTS.shutdown();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();

        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(x1 < x2){
                    Intent i = new Intent(ScanCodeActivity.this, MainActivity.class);
                    startActivity(i);
                }else if(x1 > x2){
                    Intent i = new Intent(ScanCodeActivity.this, MainActivity.class);
                    startActivity(i);
                }
                break;
        }
        return false;
    }
}
