package com.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Add the preference screen from the res/xml folder which contains options in the settings menu
        addPreferencesFromResource(com.popularmovies.R.xml.preferences);

        //Set a listener to know the choice to sort the movies selected by user
        Preference.OnPreferenceChangeListener sortMoviesListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toast.makeText(SettingsActivity.this, newValue.toString(), Toast.LENGTH_SHORT).show();

                //Reload container_main menu after selecting the sort option
                Intent main = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(main);
                return true;
            }
        };

        //Get the "sortby" preference and attach the listener to it.
        Preference sortByPreference = findPreference("sort");
        sortByPreference.setOnPreferenceChangeListener(sortMoviesListener);



    }
}
