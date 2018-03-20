package io.github.patrickconley.arbutus.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

    private static final String LIBRARY_PATH = "library path";

    private final SharedPreferences preferences;

    public Settings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getLibraryPath() {
        return preferences.getString(LIBRARY_PATH, null);
    }

}
