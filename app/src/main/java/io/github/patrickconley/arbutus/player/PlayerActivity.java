package io.github.patrickconley.arbutus.player;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


}
