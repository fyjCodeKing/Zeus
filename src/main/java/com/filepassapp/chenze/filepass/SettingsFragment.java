package com.filepassapp.chenze.filepass;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        updateSummary();
    }
    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) 
    {
      updateSummary();
    }
    
    public void updateSummary()
    {
        EditTextPreference preference; 
        
        preference = (EditTextPreference) getPreferenceScreen().findPreference("shared_resource_address");
        preference.setSummary(preference.getText());
        
        
        preference = (EditTextPreference) getPreferenceScreen().findPreference("domain_name");
        preference.setSummary(preference.getText());       
        
        preference = (EditTextPreference) getPreferenceScreen().findPreference("user_name");
        preference.setSummary(preference.getText());       
  
    }


}