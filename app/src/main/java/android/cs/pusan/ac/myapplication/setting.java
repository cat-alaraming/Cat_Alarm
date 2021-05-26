package android.cs.pusan.ac.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;


public class setting extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_map);

//        SharedPreferences prefs;
//        prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
//            public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
//                Log.d("tag","클릭된 Preference의 key는 "+key);
//            }
//        });

    }
}
