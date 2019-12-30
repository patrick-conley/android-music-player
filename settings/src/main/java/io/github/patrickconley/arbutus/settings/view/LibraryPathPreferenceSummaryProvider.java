package io.github.patrickconley.arbutus.settings.view;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import io.github.patrickconley.arbutus.settings.R;

class LibraryPathPreferenceSummaryProvider implements Preference.SummaryProvider<Preference> {

    @Override
    public CharSequence provideSummary(@NonNull Preference preference) {
        return preference.getSharedPreferences().getString(preference.getKey(),
                                                           preference.getContext().getString(
                                                                   R.string.setting_library_path_default_summary));
    }
}
