package io.github.patrickconley.arbutus.settings.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import io.github.patrickconley.arbutus.R;
import io.github.patrickconley.arbutus.scanner.view.LibraryScannerService;
import io.github.patrickconley.arbutus.settings.Settings;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
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
        findPreference(key).setSummary(sharedPreferences.getString(key,
                getString(R.string.setting_library_path_default_summary)));
    }

    private void initScanLibrary(final SharedPreferences sharedPreferences) {
        Preference preference = findPreference(Settings.SCAN_NOW.getKey());
        if (sharedPreferences.getString(Settings.LIBRARY_PATH.getKey(), null) == null) {
            preference.setEnabled(false);
        } else {
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    LibraryScannerService.startActionFoo(SettingsFragment.this.getActivity(),
                            sharedPreferences.getString(Settings.LIBRARY_PATH.getKey(), null));
                    return true;
                }
            });
        }

    }

}
