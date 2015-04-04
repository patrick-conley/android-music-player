package pconley.vamp.preferences;

import java.io.File;

import pconley.vamp.R;
import pconley.vamp.util.FileUtils;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.InputType;

public class SettingsFragment extends PreferenceFragment {

	public static final String KEY_LIBRARY_PATH = "library path";
	public static final String KEY_DEBUG = "debug mode";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// Configure the library path preference item. Input is a single line of
		// possibly non-English text, which must point to a valid, readable
		// directory.
		EditTextPreference libraryPathPref = (EditTextPreference) getPreferenceScreen()
				.findPreference(KEY_LIBRARY_PATH);

		libraryPathPref.getEditText().setInputType(
				InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		libraryPathPref
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {

						return FileUtils.validateDirectory(new File(
								(String) newValue), getActivity());
					}
				});
	}
}
