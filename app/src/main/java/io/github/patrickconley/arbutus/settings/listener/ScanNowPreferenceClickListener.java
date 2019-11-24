package io.github.patrickconley.arbutus.settings.listener;

import androidx.preference.Preference;

import io.github.patrickconley.arbutus.scanner.view.LibraryScannerService;
import io.github.patrickconley.arbutus.settings.Settings;

public final class ScanNowPreferenceClickListener
        implements Preference.OnPreferenceClickListener {

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String libraryPath =
                preference.getSharedPreferences().getString(Settings.LIBRARY_PATH.getKey(), null);
        LibraryScannerService.startActionScanLibrary(preference.getContext(), libraryPath);

        return true;
    }
}
