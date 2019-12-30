package io.github.patrickconley.arbutus.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import io.github.patrickconley.arbutus.R;
import io.github.patrickconley.arbutus.settings.view.SettingsActivity;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setSupportActionBar((Toolbar) findViewById(R.id.player_toolbar));

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    public void onClickPrev(View view) {
        // stub
    }

    public void onClickNext(View view) {
        // stub
    }

    public void onClickPlayPause(View view) {
        // stub
    }

    public void onClickSeek(View view) {
        // stub
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player, menu);
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

}
