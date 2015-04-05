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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		// Configure the library path preference item. Input is a single line of
		// possibly non-English text, which must point to a valid, readable
		// directory.
		EditTextPreference musicFolderPathPref = (EditTextPreference) getPreferenceScreen()
				.findPreference(SettingsHelper.KEY_MUSIC_FOLDER);

		musicFolderPathPref.getEditText().setInputType(
				InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		musicFolderPathPref
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
