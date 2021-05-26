package android.cs.pusan.ac.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class setting extends PreferenceActivity {

    static boolean small_marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_map);

        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
                if(key.equals("small_marker")){
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("s_marker",true);
                } else{
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("s_marker",false);
                }
            }
        });
    }
}
