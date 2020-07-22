package com.example.myvoiceassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/* renamed from: com.pa.gs.ai.MainActivity2Activity */
public class SettingsActivity extends AppCompatActivity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        androidx.appcompat.widget.Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(myToolbar);
        CheckBox beep = (CheckBox) findViewById(R.id.beep);
        CheckBox ani = (CheckBox) findViewById(R.id.animation);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        beep.setChecked(pref.getBoolean("beep", true));
        ani.setChecked(pref.getBoolean("ani", true));
        getWindow().setSoftInputMode(2);
        Editor editor = pref.edit();
        editor.putInt("system", 2);
        editor.apply();

    }

    public void back(View view) {
        onBackPressed();
    }

    public void save(View view) {
        CheckBox beep = (CheckBox) findViewById(R.id.beep);
        CheckBox ani = (CheckBox) findViewById(R.id.animation);
        Editor editor = getApplicationContext().getSharedPreferences("MyPref", 0).edit();
        editor.putBoolean("beep", beep.isChecked());
        boolean anim = ani.isChecked();
        editor.putBoolean("ani", anim);
        ani.setChecked(anim);
        editor.apply();
        Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
    }

    public void ch_voice(View view) {
        Toast.makeText(this, "Select UK - Male voice", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        intent.setAction("com.android.settings.TTS_SETTINGS");
        startActivity(intent);
    }



    public void onResume() {
        Editor editor = getApplicationContext().getSharedPreferences("MyPref", 0).edit();
        editor.putInt("system", 2);
        editor.apply();
        super.onResume();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        Editor editor = getApplicationContext().getSharedPreferences("MyPref", 0).edit();
        editor.putInt("system", 0);
        editor.apply();
        super.onPause();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        Editor editor = pref.edit();
        if (pref.getInt("system", 2) == 2) {
            editor.putInt("system", 0);
        }
        editor.apply();
        super.onDestroy();
    }

}
