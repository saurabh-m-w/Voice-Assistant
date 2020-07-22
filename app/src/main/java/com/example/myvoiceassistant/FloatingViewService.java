package com.example.myvoiceassistant;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.text.format.DateUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import es.dmoral.toasty.Toasty;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FloatingViewService extends Service implements edu.cmu.pocketsphinx.RecognitionListener {

    public edu.cmu.pocketsphinx.SpeechRecognizer recognizer;
    String comando="";
    private WindowManager mWindowManager;
    private View mFloatingView;
    private View collapsedView;
    private View expandedView;
    ImageView imageView;
    public TextToSpeech myTTS;
    public SpeechRecognizer mySpeech;
    Boolean isMoving = false;
    private GestureDetector gestureDetector;






    public FloatingViewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        intializeTextToSpeech3();
        intializeSpeech();

        gestureDetector = new GestureDetector(this, new SingleTapConfirm());
        //getting the widget layout from xml using layout inflater
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null);

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                //WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);


        //getting the collapsed and expanded view from the floating view
        collapsedView = mFloatingView.findViewById(R.id.layoutCollapsed);
        imageView = mFloatingView.findViewById(R.id.collapsed_iv);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.clockwise);
        imageView.startAnimation(animation);


        //adding click listener to close button and expanded view
        mFloatingView.findViewById(R.id.buttonClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        //adding an touchlistener to make drag movement of the floating widget
        mFloatingView.findViewById(R.id.relativeLayoutParent).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                    if (gestureDetector.onTouchEvent(event)) {
                        // single tap
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK);
                        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something...");
                        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                        //startActivityForResult(intent,5);
                        mySpeech.startListening(intent);
                        return true;
                    } else {
                        // your code for move and drag
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                initialX = params.x;
                                initialY = params.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                return true;


                            case MotionEvent.ACTION_UP:
                                //when the drag is ended switching the state of the widget
                                //collapsedView.setVisibility(View.GONE);
                                //expandedView.setVisibility(View.VISIBLE);

                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                                mWindowManager.updateViewLayout(mFloatingView, params);
                                return true;

                            case MotionEvent.ACTION_MOVE:
                                //this code is helping the widget to move around the screen with fingers
                                params.x = initialX + (int) (event.getRawX() - initialTouchX);
                                params.y = initialY + (int) (event.getRawY() - initialTouchY);
                                mWindowManager.updateViewLayout(mFloatingView, params);
                                return true;
                        }
                    }

                    // your code for move and drag

                return false;
            }
        });

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
                            Toast.makeText(getApplicationContext(),"Device not supporting Hotword Detection",Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(),"say jarvis",Toast.LENGTH_SHORT).show();
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
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
        myTTS.shutdown();
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(Exception exc) {

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
                            //Toast.makeText(getApplicationContext(), "hotword detected", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK);
                            //intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something...");
                            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);

                            mySpeech.startListening(intent);
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

    /*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.buttonClose:
                //closing the widget
                stopSelf();
                break;

        }
    }*/
    private class SingleTapConfirm extends SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }


    private void intializeSpeech()
    {
        if(SpeechRecognizer.isRecognitionAvailable(this))
        {
            mySpeech = SpeechRecognizer.createSpeechRecognizer(this);
            mySpeech.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {

                }

                @Override
                public void onError(int error) {

                }

                @Override
                public void onResults(Bundle bundle){
                    List<String> results = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                    );
                    processResult(results.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void processResult(String s) {
        s = s.toLowerCase();
        if (s.indexOf("what") != -1) {
            if (s.indexOf("your name") != -1) {
                speak1("My name is peri!!");
            }
            if (s.indexOf("time") != -1) {
                Date now = new Date();
                String time = DateUtils.formatDateTime(this, now.getTime(), DateUtils.FORMAT_SHOW_TIME);
                speak1("The Time is " + time);
            }
            if (s.indexOf("date") != -1) {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                intializeTextToSpeech2(date);
            }
            if (s.indexOf("weather") != -1) {
                startActivity(new Intent(getApplicationContext(), WeatherActivity.class));
            }

        }
        if (s.indexOf("battery") != -1) {
            BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
            int bl = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            speak1("Remaining battery life is" + bl + "percent");
        }
        if(s.indexOf("open")!=-1){

            List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
            int i = 0;
            while (true)
            {
                if (i >= packs.size())
                {
                    break;
                }
                PackageInfo p = (PackageInfo) packs.get(i);
                if (p.versionName != null) {
                    String appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
                    if (s.contains(appname)) {
                        intializeTextToSpeech2("opening" + appname);
                        Intent intent = new Intent(getPackageManager().getLaunchIntentForPackage(p.packageName));
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                        getApplicationContext().startActivity(intent);
                        //getApplicationContext().startActivity(getPackageManager().getLaunchIntentForPackage(p.packageName));
                        break;
                    } else if (s.contains(appname.toLowerCase())) {
                        intializeTextToSpeech2("opening" + appname);
                        Intent intent = new Intent(getPackageManager().getLaunchIntentForPackage(p.packageName));
                        intent.addFlags(FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP);
                        getApplicationContext().startActivity(intent);
                        //getApplicationContext().startActivity(getPackageManager().getLaunchIntentForPackage(p.packageName));
                        break;
                    }
                }
                i++;
            }
        }
        if(s.indexOf("call")!=-1)
        {
            s = s.substring(5);
            String num = matchContact(s);
            if(num == null) {
                Toast.makeText(getApplicationContext(),"Contact Not Found",Toast.LENGTH_SHORT).show();
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        intializeTextToSpeech2("contact not found");

                    }
                }, 1000);
            }
            else
            {
                getApplicationContext().startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + num)));
            }
        }
        if(s.indexOf("search")!=-1)
        {
            String str = s.substring(7);
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            //String keyword= "your query here";
            intent.putExtra(SearchManager.QUERY, str);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            this.startActivity(intent);
        }

        if(s.indexOf("turn on bluetooth")!=-1)
        {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                intializeTextToSpeech2("Bluethooth turned on");
            }
            else
            {
                intializeTextToSpeech2("bluethooth already turned on");
            }
        }
        if(s.indexOf("turn off bluetooth")!=-1)
        {
            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.disable();
                intializeTextToSpeech2("bluetooth turned off");
            }
            else
            {
                intializeTextToSpeech2("bluetooth already turned off");
            }
        }

        if(s.indexOf("turn on flashlight")!=-1)
        {

            Camera cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.startPreview();
            intializeTextToSpeech2("flashlight turned on");
        }
        if(s.indexOf("turn off flashlight")!=-1)
        {
            Camera cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            cam.stopPreview();
            cam.release();
            intializeTextToSpeech2("flashlight turned off");
        }
        if(s.indexOf("turn off wi-fi")!=-1)
        {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                intializeTextToSpeech2("WiFi turned off");
            } else {
                intializeTextToSpeech2("wifi already turned off");
            }
        }
        if(s.indexOf("turn on wi-fi")!=-1)
        {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                intializeTextToSpeech2("WiFi already turned on");
            } else {
                wifiManager.setWifiEnabled(true);
                intializeTextToSpeech2("wifi turned on");
            }
        }
        if(s.indexOf("today's date")!=-1)
        {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            intializeTextToSpeech2(date);
            Toasty.info(this, "Today's date:"+date,Toast.LENGTH_SHORT, true).show();
        }
        if(s.indexOf("set brightness medium")!=-1){
            Context context = getApplicationContext();

            // Check whether has the write settings permission or not.
            boolean settingsCanWrite = hasWriteSettingsPermission(context);

            // If do not have then open the Can modify system settings panel.
            if(!settingsCanWrite) {
                changeWriteSettingsPermission(context);
            }else {
                changeScreenBrightness(context, 255);
            }
        }
        if(s.indexOf("set brightness low")!=-1){
            Context context = getApplicationContext();

            // Check whether has the write settings permission or not.
            boolean settingsCanWrite = hasWriteSettingsPermission(context);

            // If do not have then open the Can modify system settings panel.
            if(!settingsCanWrite) {
                changeWriteSettingsPermission(context);
            }else {
                changeScreenBrightness(context, 1);
            }
        }
        if(s.indexOf("set brightness full")!=-1){
            Context context = getApplicationContext();

            // Check whether has the write settings permission or not.
            boolean settingsCanWrite = hasWriteSettingsPermission(context);

            // If do not have then open the Can modify system settings panel.
            if(!settingsCanWrite) {
                changeWriteSettingsPermission(context);
            }else {
                changeScreenBrightness(context, 2500);
            }
        }
        if(s.indexOf("take a note")!=-1){
            String note = s.substring(17);
            savenote("voiceassistantnote1.txt",note);
        }
        if(s.indexOf("any note")!=-1){
           String note = Open("voiceassistantnote1.txt");
           intializeTextToSpeech2(note);
        }

    }
    public String Open(String fileName) {
        String content = "";
        if (FileExists(fileName)) {
            try {
                InputStream in = openFileInput(fileName);
                if ( in != null) {
                    InputStreamReader tmp = new InputStreamReader( in );
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;
                    StringBuilder buf = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        buf.append(str + "\n");
                    } in .close();
                    content = buf.toString();
                }
                else {
                    intializeTextToSpeech2("note is empty");
                }
            } catch (java.io.FileNotFoundException e) {} catch (Throwable t) {
                Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content;
    }
    public boolean FileExists(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }
    public void savenote(String fileName,String note) {
        try {
            OutputStreamWriter out =
                    new OutputStreamWriter(openFileOutput(fileName, 0));
            out.write(note);
            out.close();
            Toasty.success(this, "Note saved!", Toast.LENGTH_SHORT).show();
            intializeTextToSpeech2("Note saved");
        } catch (Throwable t) {
            Toasty.error(this, "Exception: " + t.toString(),Toast.LENGTH_LONG).show();
        }
    }
    private boolean hasWriteSettingsPermission(Context context)
    {
        boolean ret = true;
        // Get the result from below code.
        ret = Settings.System.canWrite(context);
        return ret;
    }
    private void changeWriteSettingsPermission(Context context)
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        //context.startActivity(intent);
        startActivity(intent);
    }
    private void changeScreenBrightness(Context context, int screenBrightnessValue)
    {
        // Change the screen brightness change mode to manual.
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        // Apply the screen brightness value to the system, this will change the value in Settings ---> Display ---> Brightness level.
        // It will also change the screen brightness for the device.
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightnessValue);
    }
    public String matchContact(String str2) {
        //String str2 = str.substring(5);
        Cursor query = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null, null);
        if (query != null) {
            query.moveToFirst();
            while (!query.isAfterLast())
            {
                String string = query.getString(query.getColumnIndex("display_name"));
                String string2 = query.getString(query.getColumnIndex("_id"));
                if(str2.equalsIgnoreCase(string))
                {
                    ContentResolver contentResolver = getContentResolver();
                    Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                    String sb5 = "contact_id = " + string2;
                    Cursor query2 = contentResolver.query(uri, null, sb5, null, null);
                    if (query2.moveToNext())
                    {
                        String string4 = query2.getString(query2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        query2.close();
                        return string4;
                    }
                }
                query.moveToNext();
            }
            query.close();
        }
        return null;
    }
    public void speak1(String msg) {
        if(Build.VERSION.SDK_INT>=21)
        {
            myTTS.speak(msg, TextToSpeech.QUEUE_FLUSH,null,null);
        }else
        {
            myTTS.speak(msg,TextToSpeech.QUEUE_FLUSH,null);
        }
    }

    public void intializeTextToSpeech2(final String s)
    {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0) {

                    Toast.makeText(getApplicationContext(), "No Tts Engine", Toast.LENGTH_SHORT).show();
                    //finish();;
                }
                else
                {
                    myTTS.setLanguage(Locale.UK) ;
                    speak1(s);
                }

            }
        });
    }

    public void intializeTextToSpeech3() {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0) {

                    Toast.makeText(getApplicationContext(), "No Tts Engine", Toast.LENGTH_SHORT).show();
                    //finish();;
                }
                else
                {
                    myTTS.setLanguage(Locale.UK) ;
                    //speak1("Main Activity Started, swipe left for qr code scanner and swipe right to capture image");
                }

            }
        });
    }
}
