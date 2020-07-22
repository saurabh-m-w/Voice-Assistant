package com.example.myvoiceassistant;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import es.dmoral.toasty.Toasty;


public class MainActivity extends AppCompatActivity{

    float x1,x2,y1,y2;
    //public Bitmap imageBitmap;
    private TextToSpeech myTTS;
    static public SpeechRecognizer mySpeech;
    public static String msgcontact;
    Toolbar toolbar;
    Handler handler;
    String CITY = "pune,maharashtra";
    public String API = "732d05f51afcd33b4b7b3da56f2df06d";

    FusedLocationProviderClient mFusedLocationClient;
    private LocationManager locationManager;
    private String provider;
    private MyLocationListener mylistener;
    private Criteria criteria;
    protected Context context;
    public static double latitude,longitude,latitude2,longitude2;

    public ProgressDialog progressDialog;
    private Button scanbutton;
    public static TextView resulttextView;
    Button imgscan;
    Button textdetect;
    TextView imgtext;
    ImageView imageView=null;
    ImageView imageView2;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int AUDIO_PERMISSION = 2;
    static final int CALL_PHONE = 3;
    private static final String TAG = "TAG";
    private static final int DRAW_OVER_OTHER_APP_PERMISSION = 2048;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(myToolbar);

        SharedPreferences preferences = getSharedPreferences("MyPref",0);

       /* if(preferences.getBoolean("beep",true)==true){
            startService(new Intent(MainActivity.this,hotword.class));
        }
        else if(preferences.getBoolean("beep",true)==false){
            stopService(new Intent(MainActivity.this,hotword.class));
        }*/


        intializeSpeech();
        intializeTextToSpeech();
        Boolean ans = weHavePermission();
        if (!ans) {
            requestforPermissionFirst();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        imageView2 = (ImageView) findViewById(R.id.fab);
        //scanbutton = (Button)findViewById(R.id.scanner);
        resulttextView = (TextView)findViewById(R.id.resulttextView);
        //imgscan = (Button)findViewById(R.id.imagescan);
        //textdetect = (Button)findViewById(R.id.detecttext);
        imgtext = (TextView)findViewById(R.id.imagetext);
        imageView = (ImageView)findViewById(R.id.imageView);







       /* myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                 if(myTTS.getEngines().size()==0) {

                    Toast.makeText(MainActivity.this, "No Tts Engine", Toast.LENGTH_SHORT).show();
                    finish();;
                }
                else
                {
                    myTTS.setLanguage(Locale.UK) ;
                    speak1("Hello I am Ready");
                }

            }
        });*/




            imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.UK);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something...");
                //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
                startActivityForResult(intent,5);
                //mySpeech.startListening(intent);


            }
        });



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



    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode==2)
            {
                intializeTextToSpeech2("qr code scanned");

                String msg1=data.getStringExtra("MESSAGE");
                resulttextView.setText(msg1);

            }
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK)
            {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
            }
           if(requestCode == 5 && resultCode == RESULT_OK && data!=null)
            {
                ArrayList<String> arrayList;
                processResult((arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)).get(0));
                //processResult(arrayList.get(0).toString());
            }
            if(requestCode == 6 && resultCode == RESULT_OK && data!=null)
            {
                ArrayList<String> arrayList;
                msgresult((arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)).get(0));
                //processResult(arrayList.get(0).toString());
            }
        if(requestCode == 7 && resultCode == RESULT_OK && data!=null)
        {
            ArrayList<String> arrayList;
            whatsappresult((arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)).get(0));
            //processResult(arrayList.get(0).toString());
        }

    }

    /*
    public void detectTextFromImage()
    {
        Bitmap image=((BitmapDrawable)imageView.getDrawable()).getBitmap();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational())
        {
            intializeTextToSpeech2("Text not found");
        }
        else
        {
            Frame frame = new Frame.Builder().setBitmap(image).build();

            SparseArray<TextBlock> items = textRecognizer.detect(frame);
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<items.size();++i)
            {
                TextBlock myitem = items.valueAt(i);
                sb.append(myitem.getValue());
                sb.append("\n");
            }

            imgtext.setText(sb.toString());
            intializeTextToSpeech2(imgtext.toString());
        }
    }*/


    private void detectTextFromImage()
    {
        Bitmap image=((BitmapDrawable)imageView.getDrawable()).getBitmap();
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(image);
        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        //Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);
        firebaseVisionTextRecognizer.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextfromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTextfromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if(blockList.size()==0)
        {
            Toast.makeText(MainActivity.this,"No Text Found In Image",Toast.LENGTH_SHORT).show();
            intializeTextToSpeech2("No Text Found In Image");
        }
        else
        {
            for(FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks())
            {
                final String text = block.getText();
                imgtext.setText(text);
                intializeTextToSpeech2(text);
            }


        }
    }




/*
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
    }*/

    @SuppressLint("MissingPermission")
    private void processResult(String s)
    {
        s = s.toLowerCase();
        if(s.indexOf("what") !=-1)
        {
            if(s.indexOf("your name")!=-1){
                speak1("My name is peri!!");
            }
            if(s.indexOf("time")!=-1)
            {
                Date now = new Date();
                String time = DateUtils.formatDateTime(this,now.getTime(),DateUtils.FORMAT_SHOW_TIME);
                speak1("The Time is "+time);
            }
            if(s.indexOf("date")!=-1)
            {
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                intializeTextToSpeech2(date);
                Toasty.info(MainActivity.this, "Today's date:"+date,Toast.LENGTH_SHORT, true).show();
            }
            if(s.indexOf("weather")!=-1)
            {
                getLastLocation();
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
                        intent.putExtra("lat",latitude);
                        intent.putExtra("long",longitude);
                        startActivity(intent);

                    }
                }, 2000);


            }

        }
        if(s.indexOf("battery")!=-1){
            BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
            int bl = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            speak1("Remaining battery life is"+ bl +"percent");
        }
        if(s.indexOf("open text messages")!=-1)
        {
            intializeTextToSpeech2("opening text messages");
            startActivity(new Intent(MainActivity.this,SmsActivity.class));
        }
        if(s.indexOf("scan qr code")!=-1)
        {
            Intent intent3 = new Intent(MainActivity.this,ScanCodeActivity.class);
            startActivityForResult(intent3,2);
        }
        if(s.indexOf("scan image")!=-1)
        {
            dispatchTakePictureIntent();
            imgtext.setText("");
        }
        if(s.indexOf("capture image")!=-1)
        {
            dispatchTakePictureIntent();
            imgtext.setText("");
        }
        if(s.indexOf("text in image")!=-1)
        {
            detectTextFromImage();
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
                        startActivity(getPackageManager().getLaunchIntentForPackage(p.packageName));
                        break;
                    } else if (s.contains(appname.toLowerCase())) {
                        intializeTextToSpeech2("opening" + appname);
                        startActivity(getPackageManager().getLaunchIntentForPackage(p.packageName));
                        break;
                    }
                }
                i++;
            }
        }

        if(s.indexOf("Exit")!=-1)
        {
            finish();
            System.exit(0);
        }

        if(s.indexOf("call")!=-1)
        {
             s = s.substring(8);
             if(s==null)
             {
                 intializeTextToSpeech2("didn't catch that,please try again");
             }
             else {
                 String num = matchContact(s);
                 if (num == null) {
                     //Toast.makeText(getApplicationContext(),"Contact Not Found",Toast.LENGTH_SHORT).show();
                     Toasty.warning(MainActivity.this, "Contact Not Found", Toast.LENGTH_SHORT, true).show();
                     new Timer().schedule(new TimerTask() {

                         @Override
                         public void run() {
                             intializeTextToSpeech2("contact not found");

                         }
                     }, 1000);
                 } else {
                     startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel: " + num)));
                 }
             }
        }
        if(s.indexOf("search")!=-1)
        {

            String str = s.substring(7);
            if(str.length()==0){
                intializeTextToSpeech2("didn't catch that,please try again");
            }
            else {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                //String keyword= "your query here";
                intent.putExtra(SearchManager.QUERY, str);
                startActivity(intent);
            }
        }
        if(s.indexOf("find")!=-1)
        {
            String s2 = s.substring(5);
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://www.google.com/maps/place/" + s2));
                startActivity(intent);
            }
            catch (Exception e){
                intializeTextToSpeech2("didn't catch that,please try again");
            }

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
                intializeTextToSpeech2("bluethooth turned off");
                Toasty.success(MainActivity.this, "Bluethooth turned off",Toast.LENGTH_SHORT, true).show();
            }
            else
            {
                intializeTextToSpeech2("bluethooth already turned off");
                Toasty.success(MainActivity.this, "Bluthooth already turned off",Toast.LENGTH_SHORT, true).show();
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
            Toasty.success(MainActivity.this, "flashlight turned on",Toast.LENGTH_SHORT, true).show();
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
            Toasty.success(MainActivity.this, "flashlight turned off",Toast.LENGTH_SHORT, true).show();
        }
        if(s.indexOf("turn off wi-fi")!=-1)
        {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
                intializeTextToSpeech2("WiFi turned off");
                Toasty.success(MainActivity.this, "WiFi turned off",Toast.LENGTH_SHORT, true).show();

            } else {
                intializeTextToSpeech2("wifi already turned off");
                Toasty.success(MainActivity.this, "WiFi already turned off",Toast.LENGTH_SHORT, true).show();
            }
        }
        if(s.indexOf("turn on wi-fi")!=-1)
        {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                intializeTextToSpeech2("WiFi already turned on");
                Toasty.success(MainActivity.this, "WiFi already turned on",Toast.LENGTH_SHORT, true).show();
            } else {
                wifiManager.setWifiEnabled(true);
                intializeTextToSpeech2("wifi turned on");
                Toasty.success(MainActivity.this, "wifi turned on",Toast.LENGTH_SHORT, true).show();
            }
        }
        if(s.indexOf("today's date")!=-1)
        {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Toasty.info(MainActivity.this, "Today's date:"+date,Toast.LENGTH_SHORT, true).show();
            intializeTextToSpeech2(date);
        }
        if(s.indexOf("where am i")!=-1)
        {

                getLastLocation();
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        try {
                        Geocoder geo = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geo.getFromLocation(latitude, longitude, 1);
                        if (addresses.isEmpty()) {
                            imgtext.setText("Waiting for Location");
                        }
                        else {
                            if (addresses.size() > 0) {
                                imgtext.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
                                intializeTextToSpeech2(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() +", " + addresses.get(0).getAdminArea());
                            }
                        }
                    }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                }
                }, 2000);
        }
        if(s.indexOf("send text message")!=-1) {
            final String s2 = s.substring(21);

            if(s2==null){
                return;
            }

            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    msgcontact = matchContact2(s2);

                }
            }, 500);

            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    if (msgcontact == null) {
                        intializeTextToSpeech2("contact not found");
                    } else {
                        msgcall();
                    }

                }
            }, 4000);
        }
        if(s.indexOf("send whatsapp message")!=-1) {
            final String s2 = s.substring(25);
            if(s2==null){
                return;
            }

            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    msgcontact = matchContact2(s2);

                }
            }, 500);

            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    if (msgcontact == null) {
                        intializeTextToSpeech2("contact not found");
                    } else {
                        whatsappmsgcall();
                    }

                }
            }, 4000);
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
            String note = s.substring(12);
            savenote("voiceassistantnote1.txt",note);
        }
        if(s.indexOf("any note")!=-1){
            final String note = Open("voiceassistantnote1.txt");
            imgtext.setText(note);
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    intializeTextToSpeech2(note);
                }
            }, 1000);

        }
        if(s.indexOf("get direction")!=-1){
            s = s.substring(17);
            if(s==null){
                intializeTextToSpeech2("Din't catch that,try again");
            }else {

                getLocationFromAddress(MainActivity.this, s);
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Intent i = new Intent(MainActivity.this, MapActivity.class);
                        i.putExtra("lat",latitude2);
                        i.putExtra("long",longitude2);
                        startActivity(i);
                    }
                }, 2000);
            }
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
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    intializeTextToSpeech2("Note saved");
                }
            }, 1000);

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

    public void whatsappmsgcall()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.UK);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something...");
        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        startActivityForResult(intent,7);
        //mySpeech.startListening(intent);
    }
    public void whatsappresult(String msg)
    {
        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(String.format("https://api.whatsapp.com/send?phone=%s&text=%s","91"+msgcontact, msg))));
    }
    public void msgcall()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.UK);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Something...");
        //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        startActivityForResult(intent,6);
        //mySpeech.startListening(intent);
    }
    public void msgresult(String msg) {

        if (msg.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Empty Notice!!", Toast.LENGTH_SHORT).show();
        } else if (msgcontact.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Phone No is Not Saved", Toast.LENGTH_SHORT).show();
        } else {
            try {
                //Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(msgcontact, null, msg, null, null);

                //Toast.makeText(getApplicationContext(), "Message Sent Successfully to "+msgcontact, Toast.LENGTH_SHORT).show();
                Toasty.success(MainActivity.this, "Message Sent Successfully to "+msgcontact,Toast.LENGTH_SHORT, true).show();
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        intializeTextToSpeech2("Message Sent Successfully to"+msgcontact);

                    }
                }, 1500);
            } catch (Exception e) {
                intializeTextToSpeech2("SMS faild, please try again later!");
                Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    //imgtext.setText("Latitude: "+location.getLatitude()+"Longitude: "+location.getLongitude());
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }
    public void getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;


        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
            }
            Address location = address.get(0);
            latitude2=location.getLatitude();
            longitude2=location.getLongitude();


        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            //imgtext.setText("Latitude: "+mLastLocation.getLatitude()+"Longitude: "+mLastLocation.getLongitude());
        }
    };
    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                44
        );
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 44) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }


    public String matchContact2(String str2) {
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

                        intializeTextToSpeech2("what message you want to send to"+string);
                        return string4;
                    }
                }
                query.moveToNext();
            }
            query.close();
        }
        return null;
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



    private void intializeTextToSpeech() {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0) {

                    Toast.makeText(MainActivity.this, "No Tts Engine", Toast.LENGTH_SHORT).show();
                    finish();;
                }
                else
                {
                    myTTS.setLanguage(Locale.UK) ;
                    //speak1("Main Activity Started");
                    abc();
                }

            }
        });
    }
    public void abc()
    {
        Calendar rightNow = Calendar.getInstance();
        Date now = new Date();
        int th = rightNow.get(Calendar.HOUR_OF_DAY);
        int tm = rightNow.get(Calendar.MINUTE);
        String time = DateUtils.formatDateTime(this,now.getTime(),DateUtils.FORMAT_SHOW_TIME);
            if (th < 12)
            {
                intializeTextToSpeech2("good morning its" + time);
            }
            if (th > 12 && th < 16) {
                intializeTextToSpeech2("good afternoon its" + time);
            }
            if (th > 15) {
                intializeTextToSpeech2("good evening its" + time);
            }

    }

    private void intializeTextToSpeech2(final String s)
    {
        myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0) {

                    Toast.makeText(MainActivity.this, "No Tts Engine", Toast.LENGTH_SHORT).show();
                    finish();;
                }
                else
                {
                    myTTS.setLanguage(Locale.UK) ;
                    speak1(s);
                }

            }
        });
    }

    private void intializeTextToSpeech3() {
            myTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(myTTS.getEngines().size()==0) {

                    Toast.makeText(MainActivity.this, "No Tts Engine", Toast.LENGTH_SHORT).show();
                    finish();;
                }
                else
                {
                    myTTS.setLanguage(Locale.UK) ;
                    //speak1("Main Activity Started, swipe left for qr code scanner and swipe right to capture image");
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
    protected void onPause()
    {
        super.onPause();
        //startService(new Intent(MainActivity.this, FloatingViewService.class));
        //myTTS.shutdown();
    }
   @Override
   protected void onResume() {
       super.onResume();
       intializeTextToSpeech3();
       SharedPreferences preferences = getSharedPreferences("MyPref",0);

       if(preferences.getBoolean("ani",true)==true){
           Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.clockwise);
           imageView2.startAnimation(animation);
       }
       else if(preferences.getBoolean("ani",true)==false){

       }

       stopService(new Intent(MainActivity.this, FloatingViewService.class));
   }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myTTS.shutdown();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_items,menu);

        MenuItem item = menu.findItem(R.id.hotswitch);

        Switch mySwitch = item.getActionView().findViewById(R.id.switch_id);
        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    startService(new Intent(MainActivity.this,hotword.class));
                }
                else if(!isChecked){
                    stopService(new Intent(MainActivity.this,hotword.class));
                    Toasty.info(getApplicationContext(),"Hotword detection Stopped").show();
                }
            }
        });



        return true;
    }
    private void askPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION);
    }

    //@SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id)
        {

            case R.id.item2:
                System.exit(0);
                return true;
            case R.id.item3:
            {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    MainActivity.this.startService(new Intent(MainActivity.this, FloatingViewService.class));
                    finish();
                } else if (Settings.canDrawOverlays(this)) {
                    MainActivity.this.startService(new Intent(MainActivity.this, FloatingViewService.class));
                    finish();
                }   else {
                    askPermission();
                    Toast.makeText(this, "You need System Alert Window Permission to do this", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
            case R.id.item4:
            {
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                return true;
            }
            case R.id.helpitem:{
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private void copyResourceFile(int resourceID, String filename) throws IOException {
        Resources resources = getResources();
        try (InputStream is = new BufferedInputStream(resources.openRawResource(resourceID), 256); OutputStream os = new BufferedOutputStream(openFileOutput(filename, Context.MODE_PRIVATE), 256)) {
            int r;
            while ((r = is.read()) != -1) {
                os.write(r);
            }
            os.flush();
        }
    }

    /*public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();

                if(x1 < x2)
                {
                    Intent i = new Intent(MainActivity.this, ScanCodeActivity.class);
                    startActivityForResult(i,2);
                }
                else if(x1 > x2)
                {
                    dispatchTakePictureIntent();
                    imgtext.setText("");
                }
            break;
        }
        return false;
    }*/


    //Android Runtime Permission
    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestforPermissionFirst() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE))
                || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS))
                || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS))||(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                ||(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
        {
            requestForResultContactsPermission();
        } else {
            requestForResultContactsPermission();
        }
    }

    private void requestForResultContactsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.CAMERA,Manifest.permission.READ_CONTACTS,Manifest.permission.READ_SMS,Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
    }



    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // Initialize the location fields



            Toast.makeText(MainActivity.this,  ""+location.getLatitude()+location.getLongitude(),
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(MainActivity.this, provider + "'s status changed to "+status +"!",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " enabled!",
                    Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider " + provider + " disabled!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

