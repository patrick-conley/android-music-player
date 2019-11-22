package io.github.patrickconley.arbutus.settings.listener;

import android.content.Context;
import android.preference.Preference;

import io.github.patrickconley.arbutus.scanner.view.LibraryScannerService;

public final class ScanLibraryPreferenceClickListener
        implements Preference.OnPreferenceClickListener {

    private final Context context;
    private final String libraryPath;

    public ScanLibraryPreferenceClickListener(Context context, String libraryPath) {
        this.context = context;
        this.libraryPath = libraryPath;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        LibraryScannerService
                .startActionScanLibrary(context, libraryPath);
        return true;
    }
}
