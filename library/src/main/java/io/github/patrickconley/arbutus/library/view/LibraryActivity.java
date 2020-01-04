package io.github.patrickconley.arbutus.library.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import io.github.patrickconley.arbutus.datastorage.library.model.LibraryEntryText;
import io.github.patrickconley.arbutus.library.R;
import io.github.patrickconley.arbutus.settings.view.SettingsActivity;

public class LibraryActivity extends AppCompatActivity
        implements LibraryEntryFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        setSupportActionBar((Toolbar) findViewById(R.id.library_toolbar));

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.library, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListFragmentInteraction(LibraryEntryText item) {
        // TODO: something
    }
}
