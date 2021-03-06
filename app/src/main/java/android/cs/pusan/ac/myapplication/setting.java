package android.cs.pusan.ac.myapplication;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;


public class setting extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.setting_map);

        CheckBoxPreference checkbox = (CheckBoxPreference) findPreference("small_marker");
        checkbox.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (checkbox.isChecked()) {
                    SharedPreferences pref = getSharedPreferences("Setting", 0);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putBoolean("checking", true);
                    edit.commit();
                } else {
                    SharedPreferences pref = getSharedPreferences("Setting", 0);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putBoolean("checking", false);
                    edit.commit();
                }
                return true;
            }
        });

        CheckBoxPreference check_music = (CheckBoxPreference) findPreference("background_music");
        check_music.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (check_music.isChecked()) {
                    SharedPreferences pref = getSharedPreferences("Setting_music", 0);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putBoolean("checking_music", true);
                    edit.commit();
                } else {
                    SharedPreferences pref = getSharedPreferences("Setting_music", 0);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putBoolean("checking_music", false);
                    edit.commit();
                }
                return true;
            }
        });
    }
}
