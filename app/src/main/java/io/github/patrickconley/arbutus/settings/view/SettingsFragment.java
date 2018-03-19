package io.github.patrickconley.arbutus.settings.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import io.github.patrickconley.arbutus.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSelectLibraryPath(MenuItem item) {

    }
}
