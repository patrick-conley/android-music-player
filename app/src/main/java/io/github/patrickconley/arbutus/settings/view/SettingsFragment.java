package io.github.patrickconley.arbutus.settings.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;

import io.github.patrickconley.arbutus.R;
import io.github.patrickconley.arbutus.settings.Settings;
import io.github.patrickconley.arbutus.settings.listener.ScanLibraryPreferenceClickListener;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

        // Set value-dependent summaries
        initLibraryPath(preferences);
        initScanLibrary(preferences);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences()
                             .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(Settings.LIBRARY_PATH.getKey())) {
            initScanLibrary(sharedPreferences);
            initLibraryPath(sharedPreferences);
        }
    }

    private void initLibraryPath(SharedPreferences sharedPreferences) {
        String key = Settings.LIBRARY_PATH.getKey();
        findPreference(key).setSummary(sharedPreferences.getString(key, getString(
                R.string.setting_library_path_default_summary)));
    }

    private void initScanLibrary(final SharedPreferences sharedPreferences) {
        Preference preference = findPreference(Settings.SCAN_NOW.getKey());
        final String libraryPath =
                sharedPreferences.getString(Settings.LIBRARY_PATH.getKey(), null);
        if (libraryPath == null) {
            preference.setEnabled(false);
        } else {
            preference.setOnPreferenceClickListener(
                    new ScanLibraryPreferenceClickListener(getActivity(), libraryPath));
        }

    }

}
