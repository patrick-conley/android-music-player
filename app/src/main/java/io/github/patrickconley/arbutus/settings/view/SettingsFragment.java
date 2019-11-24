package io.github.patrickconley.arbutus.settings.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import io.github.patrickconley.arbutus.R;
import io.github.patrickconley.arbutus.settings.Settings;
import io.github.patrickconley.arbutus.settings.listener.ScanNowPreferenceClickListener;

import static java.util.Objects.requireNonNull;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onResume() {
        super.onResume();
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        getLibraryPathPreference().setSummaryProvider(new LibraryPathPreferenceSummaryProvider());

        getScanNowPreference().setOnPreferenceClickListener(new ScanNowPreferenceClickListener());
        onSharedPreferenceChanged(getSharedPreferences(), Settings.LIBRARY_PATH.getKey());
    }

    @Override
    public void onPause() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.w(getClass().getName(), "onSharedPreferenceChange");

        if (key.equals(Settings.LIBRARY_PATH.getKey())) {
            getScanNowPreference().setEnabled(
                    sharedPreferences.getString(Settings.LIBRARY_PATH.getKey(), null) != null);
        }

    }

    private SharedPreferences getSharedPreferences() {
        return getPreferenceManager().getSharedPreferences();
    }

    private Preference getLibraryPathPreference() {
        return requireNonNull(findPreference(Settings.LIBRARY_PATH.getKey()));
    }

    private Preference getScanNowPreference() {
        return requireNonNull(findPreference(Settings.SCAN_NOW.getKey()));
    }

}
