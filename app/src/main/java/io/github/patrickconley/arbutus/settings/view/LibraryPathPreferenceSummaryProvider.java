package io.github.patrickconley.arbutus.settings.view;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import io.github.patrickconley.arbutus.R;

class LibraryPathPreferenceSummaryProvider implements Preference.SummaryProvider<Preference> {

    @Override
    public CharSequence provideSummary(@NonNull Preference preference) {
        Log.w(getClass().getName(), "provideSummary");
        return preference.getSharedPreferences().getString(preference.getKey(),
                                                           preference.getContext().getString(
                                                                   R.string.setting_library_path_default_summary));
    }
}
